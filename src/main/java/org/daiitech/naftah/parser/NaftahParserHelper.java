package org.daiitech.naftah.parser;

import static org.daiitech.naftah.Naftah.DEBUG_PROPERTY;
import static org.daiitech.naftah.Naftah.STANDARD_EXTENSIONS;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.ResourceUtils.getProperties;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.*;

import com.ibm.icu.text.Normalizer2;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;

/**
 * @author Chakib Daii
 */
public class NaftahParserHelper {
  public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("PLACEHOLDER\\((.*?)\\)");
  public static final Properties TOKENS_SYMBOLS =
      getProperties(getJarDirectory() + "/tokens-symbols.properties");

  public static final String NULL = "<فارغ>";

  public static final String QUALIFIED_CALL_REGEX = "^([^:]+)(:[^:]+)*::[^:]+$";
  public static final Normalizer2 NORMALIZER = Normalizer2.getNFKCInstance();

  // Cache to store computed subtrees per node
  private static final Map<ParseTree, List<ParseTree>> SUB_TREE_CACHE = new IdentityHashMap<>();

  public static <T extends Tree> boolean hasChild(T child) {
    return child != null;
  }

  public static <T, T1 extends Tree> boolean hasChildOfType(T child, Class<T1> type) {
    return child != null && type.isAssignableFrom(child.getClass());
  }

  public static <T, T1 extends Tree> boolean hasChildOfType(List<T> children, Class<T1> type) {
    return !ObjectUtils.isEmpty(children)
        && children.stream().anyMatch(child -> hasChildOfType(child, type));
  }

  public static <T extends Tree> boolean hasChildOrSubChildOfType(ParseTree ctx, Class<T> type) {
    var children = getAllChildren(ctx);
    return !ObjectUtils.isEmpty(children)
        && children.stream().anyMatch(child -> hasChildOfType(child, type));
  }

  public static <T extends Tree> boolean hasAnyExecutedChildOrSubChildOfType(
      ParseTree ctx, Class<T> type, ParseTreeProperty<Boolean> executedParseTreeProperty) {
    return getAllChildrenOfType(ctx, type).stream()
        .anyMatch(
            child ->
                Optional.ofNullable(executedParseTreeProperty)
                    .map(parseTreeProperty -> parseTreeProperty.get(child))
                    .orElse(false));
  }

  public static <T extends Tree> List<ParseTree> getAllChildrenOfType(
      ParseTree ctx, Class<T> type) {
    var children = getAllChildren(ctx);
    return !ObjectUtils.isEmpty(children)
        ? children.stream().filter(child -> hasChildOfType(child, type)).toList()
        : List.of();
  }

  // Collects all nodes in the subtree rooted at 'ctx'
  public static List<ParseTree> getAllChildren(ParseTree ctx) {
    // If cached, return from cache
    if (SUB_TREE_CACHE.containsKey(ctx)) {
      return SUB_TREE_CACHE.get(ctx);
    }

    List<ParseTree> nodes = new ArrayList<>();
    collect(ctx, nodes);
    SUB_TREE_CACHE.put(ctx, nodes); // Cache the result
    return nodes;
  }

  private static void collect(ParseTree node, List<ParseTree> out) {
    out.add(node); // Include the node itself
    for (int i = 0; i < node.getChildCount(); i++) {
      collect(node.getChild(i), out);
    }
  }

  public static Object visit(
      org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
      ParseTree tree) {
    return naftahParserBaseVisitor.visit(tree);
  }

  public static void prepareDeclaredFunction(
      org.daiitech.naftah.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
      DeclaredFunction function) {
    if (function.getParameters() == null && hasChild(function.getParametersContext()))
      function.setParameters(
          (List<DeclaredParameter>)
              visit(naftahParserBaseVisitor, function.getParametersContext()));
    if (function.getReturnType() == null && hasChild(function.getReturnTypeContext()))
      function.setReturnType(visit(naftahParserBaseVisitor, function.getReturnTypeContext()));
  }

  public static Map<String, Object> prepareDeclaredFunctionArguments(
      List<DeclaredParameter> parameters, List<Pair<String, Object>> arguments) {
    if (parameters.size() < arguments.size())
      throw new NaftahBugError("عدد الوسائط الممررة '%s' يتجاوز عدد المعاملات '%s' المحددة.".formatted(arguments, parameters));

    // how many params don't have defaults
    List<DeclaredParameter> requiredParams =
        parameters.stream().filter(p -> p.getDefaultValue() == null).toList();

    // arguments that have names
    Map<Integer, Pair<String, Object>> namedArguments =
        IntStream.range(0, arguments.size())
            .mapToObj(
                i -> {
                  var current = arguments.get(i);
                  return current.a != null ? Map.entry(i, current) : null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<String, Object> finalArguments = new HashMap<>();
    if (namedArguments.isEmpty()) {
      if (arguments.size() < requiredParams.size())
        throw new NaftahBugError("عدد الوسائط الممررة '%s' أقل من عدد المعاملات '%s' المحددة.".formatted(arguments, parameters));
      // process non named args
      finalArguments =
          IntStream.range(0, arguments.size())
              .mapToObj(
                  i -> {
                    var argument = arguments.get(i);
                    var param =
                        requiredParams.size() >= i ? parameters.get(i) : requiredParams.get(i);
                    return Map.entry(param.getName(), argument.b);
                  })
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } else {
      Set<String> usedNames = new HashSet<>();
      // arguments that have no names
      Map<Integer, Pair<String, Object>> positionalArguments =
          IntStream.range(0, arguments.size())
              .mapToObj(
                  i -> {
                    var current = arguments.get(i);
                    return current.a == null ? Map.entry(i, current) : null;
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      // Assign positional arguments
      for (var entry : positionalArguments.entrySet()) {
        String paramName = parameters.get(entry.getKey()).getName();
        if (namedArguments.containsKey(entry.getKey())) {
          throw new NaftahBugError("تم تحديد الوسيط '%s' موقعياً وبالاسم في آنٍ واحد.".formatted(paramName));
        }
        finalArguments.put(paramName, entry.getValue().b);
        usedNames.add(paramName);
      }

      // Assign named arguments
      for (var entry : namedArguments.entrySet()) {
        DeclaredParameter param = parameters.get(entry.getKey());

        if (param != null) {
          String paramName = param.getName();

          if (usedNames.contains(paramName)) {
            throw new NaftahBugError("تم تمرير الوسيط '%s' أكثر من مرة.".formatted(paramName));
          }

          finalArguments.put(paramName, entry.getValue().b);
          usedNames.add(paramName);

        } else {
          throw new NaftahBugError("الوسيط '%s' لا يتوافق مع أي من المعاملات المحددة." + entry.getValue().a);
        }
      }

      // Assign default values
      for (DeclaredParameter param : parameters) {
        if (!finalArguments.containsKey(param.getName())) {
          if (param.getDefaultValue() != null) {
            finalArguments.put(param.getName(), param.getDefaultValue());
          } else {
            throw new NaftahBugError("الوسيط '%s' لم يتم مطابقته مع أي من المعاملات.".formatted(param.getName()));
          }
        }
      }
    }

    return finalArguments;
  }

  public static String getQualifiedName(
      org.daiitech.naftah.parser.NaftahParser.QualifiedNameContext ctx) {
    AtomicReference<StringBuffer> result = new AtomicReference<>(new StringBuffer());

    for (int i = 0; i < ctx.ID().size(); i++) {
      result.get().append(ctx.ID(i));
      if (i != ctx.ID().size() - 1) {
        // if not the last
        result.get().append(ctx.COLON(i));
      }
    }
    return result.get().toString();
  }

  public static org.daiitech.naftah.parser.NaftahParser getParser(
      CommonTokenStream commonTokenStream, ANTLRErrorListener errorListener) {
    return getParser(commonTokenStream, List.of(errorListener));
  }

  public static org.daiitech.naftah.parser.NaftahParser getParser(
      CommonTokenStream commonTokenStream, List<ANTLRErrorListener> errorListeners) {
    // Create a parser
    org.daiitech.naftah.parser.NaftahParser parser =
        new org.daiitech.naftah.parser.NaftahParser(commonTokenStream);
    parser.removeErrorListeners();
    errorListeners.forEach(parser::addErrorListener);

    // Use the BailErrorStrategy
    parser.setErrorHandler(new BailErrorStrategy());

    return parser;
  }

  public static CommonTokenStream getCommonTokenStream(CharStream charStream) {
    return getCommonTokenStream(charStream, List.of()).b;
  }

  public static CommonTokenStream getCommonTokenStream(
      CharStream charStream, ANTLRErrorListener errorListener) {
    return getCommonTokenStream(charStream, List.of(errorListener)).b;
  }

  public static Pair<org.daiitech.naftah.parser.NaftahLexer, CommonTokenStream>
      getCommonTokenStream(CharStream charStream, List<ANTLRErrorListener> errorListeners) {
    // Create a lexer and token stream
    org.daiitech.naftah.parser.NaftahLexer lexer =
        new org.daiitech.naftah.parser.NaftahLexer(charStream);
    lexer.removeErrorListeners();
    errorListeners.forEach(lexer::addErrorListener);
    return new Pair<>(lexer, new CommonTokenStream(lexer));
  }

  public static CharStream getCharStream(boolean isScriptFile, String script) throws Exception {
    CharStream charStream;
    if (isScriptFile) {
      // Search for path
      Path filePath = searchForNaftahScriptFile(script).toPath();
      //  Read file into a String
      // TODO: this is not needed in windows after rechecking.
      //      String content = Files.readString(filePath, StandardCharsets.UTF_8);
      //      charStream = CharStreams.fromString(POSSIBLE_SHAPING_FUNCTION.apply(content));
      // TODO: it works like this in windows (maybe Posix systems still need extra fixes, like
      // above)
      charStream = CharStreams.fromPath(filePath, StandardCharsets.UTF_8);
    } else {
      // TODO: this is not needed in windows after rechecking.
      //      charStream = CharStreams.fromString(POSSIBLE_SHAPING_FUNCTION.apply(script));
      // TODO: it works like this in windows (maybe Posix systems still need extra fixes, like
      // above)
      script = NORMALIZER.normalize(script);
      if (Boolean.getBoolean(DEBUG_PROPERTY)) getRawHexBytes(script);
      charStream = CharStreams.fromString(script);
    }
    return charStream;
  }

  /**
   * Search for the script file, doesn't bother if it is named precisely.
   *
   * <p>Tries in this order: - actual supplied name - name.naftah - name.nfth - name.na - name.nsh
   *
   * @since 0.0.1
   */
  public static File searchForNaftahScriptFile(String input) {
    String scriptFileName = input.trim();
    File scriptFile = new File(scriptFileName);
    int i = 0;
    while (i < STANDARD_EXTENSIONS.length && !scriptFile.exists()) {
      scriptFile = new File(scriptFileName + STANDARD_EXTENSIONS[i]);
      i++;
    }
    // if we still haven't found the file, point back to the originally specified filename
    if (!scriptFile.exists()) {
      scriptFile = new File(scriptFileName);
    }
    return scriptFile;
  }

  public static void resolvePlaceholders(Properties props) {
    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);
      String resolved = resolveValue(value, props);
      props.setProperty(key, resolved);
    }
  }

  private static String resolveValue(String value, Properties props) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
    StringBuilder result = new StringBuilder();
    boolean found = false;
    while (matcher.find()) {
      found = true;
      String placeholderKey = matcher.group(1);
      String replacement = props.getProperty(placeholderKey, "").split(",")[0].replaceAll("'", "");
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }

    matcher.appendTail(result);
    var resultValue = result.toString();
    return found ? "'" + resultValue.replaceAll(" ", "") + "'" : resultValue;
  }
}
