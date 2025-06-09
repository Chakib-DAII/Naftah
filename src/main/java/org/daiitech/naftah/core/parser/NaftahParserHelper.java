package org.daiitech.naftah.core.parser;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.Tree;
import org.daiitech.naftah.core.builtin.lang.DeclaredFunction;
import org.daiitech.naftah.core.builtin.lang.DeclaredParameter;
import org.daiitech.naftah.core.builtin.utils.ObjectUtils;

/**
 * @author Chakib Daii
 */
public class NaftahParserHelper {

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
      org.daiitech.naftah.core.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
      ParseTree tree) {
    return naftahParserBaseVisitor.visit(tree);
  }

  public static void prepareDeclaredFunction(
      org.daiitech.naftah.core.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
      DeclaredFunction function) {
    if (function.getParameters() == null && hasChild(function.getParametersContext()))
      function.setParameters(
          (List<DeclaredParameter>)
              visit(naftahParserBaseVisitor, function.getParametersContext()));
    if (function.getReturnType() == null && hasChild(function.getReturnTypeContext()))
      function.setReturnType(visit(naftahParserBaseVisitor, function.getReturnTypeContext()));
  }

  public static Map<String, Object> prepareDeclaredFunctionArguments(
      org.daiitech.naftah.core.parser.NaftahParserBaseVisitor<?> naftahParserBaseVisitor,
      List<DeclaredParameter> parameters,
      List<Pair<String, Object>> arguments) {
    if (parameters.size() < arguments.size()) throw new RuntimeException("Too many arguments");

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

    Map<String, Object> finalArguments = null;
    if (namedArguments.isEmpty()) {
      if (arguments.size() < requiredParams.size()) throw new RuntimeException("Too few arguments");
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
      finalArguments = new HashMap<>();
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
          throw new RuntimeException(
              "Argument '" + paramName + "' specified both positionally and by name");
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
            throw new RuntimeException("Duplicate argument: " + paramName);
          }

          finalArguments.put(paramName, entry.getValue().b);
          usedNames.add(paramName);

        } else {
          throw new RuntimeException("Unknown parameter name: " + entry.getValue().a);
        }
      }

      // Assign default values
      for (DeclaredParameter param : parameters) {
        if (!finalArguments.containsKey(param.getName())) {
          if (param.getDefaultValue() != null) {
            finalArguments.put(param.getName(), param.getDefaultValue());
          } else {
            throw new RuntimeException("Missing required argument: " + param.getName());
          }
        }
      }
    }

    return finalArguments;
  }

  public static String getQualifiedName(
      org.daiitech.naftah.core.parser.NaftahParser.QualifiedNameContext ctx) {
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
}
