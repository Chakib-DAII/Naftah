package org.daiitech.naftah;

import static java.util.logging.Logger.*;
import static org.daiitech.naftah.NaftahSystem.*;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrBuiltinOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;
import static org.daiitech.naftah.parser.NaftahParserHelper.*;
import static org.daiitech.naftah.utils.OS.OS_NAME_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.*;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static org.daiitech.naftah.utils.repl.REPLHelper.*;
import static picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.parser.DefaultNaftahParserVisitor;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.utils.JulLoggerConfig;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import picocli.CommandLine;

/**
 * @author Chakib Daii
 *     <p>main of Naftah programming language as an interpreted JVM language
 */
public final class Naftah {

  public static final String JAVA_VERSION_PROPERTY = "java.version";
  public static final String JAVA_VM_VENDOR_PROPERTY = "java.vm.vendor";
  public static final String SCAN_CLASSPATH_PROPERTY = "naftah.scanClassPath";
  public static final String FORCE_CLASSPATH_PROPERTY = "naftah.forceClassPathScan";
  public static final String DEBUG_PROPERTY = "naftah.debug";
  public static final String INSIDE_REPL_PROPERTY = "naftah.repl";
  public static final String INSIDE_INIT_PROPERTY = "naftah.init";
  public static final String INSIDE_RUN_PROPERTY = "naftah.run";
  public static final String FILE_ENCODING_PROPERTY = "file.encoding";

  public static final String[] STANDARD_EXTENSIONS = {".naftah", ".nfth", ".na", ".nsh"};

  static {
    setupTerminalWidthAndHeight(NaftahSystem::getTerminalWidthAndHeight);
  }

  // arguments to the script
  private List<String> args;

  // is this a file on disk
  private boolean isScriptFile;

  // filename or content of script
  private String script;

  // do you want full stack traces in script exceptions?
  private boolean debug = false;

  private Naftah() {}

  public static void main(String[] args) {
    processArgs(args);
  }

  private static void initLogger(boolean debug) {
    try {
      String loggingConfigFile = System.getProperty("java.util.logging.config.file");

      // Initialize logging from external file
      if (Objects.nonNull(loggingConfigFile)) JulLoggerConfig.initialize(loggingConfigFile);
      else JulLoggerConfig.initializeFromResources("logging.properties");
    } catch (IOException e) {
      try {
        // fallback to default logging
        JulLoggerConfig.initializeFromResources("logging.properties");
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    } finally {
      if (debug) {
        // Adjust ConsoleHandler (if it exists)
        Logger rootLogger = getLogger("");
        for (Handler h : rootLogger.getHandlers()) {
          if (h instanceof ConsoleHandler consoleHandler) {
            consoleHandler.setLevel(Level.FINEST);
          } else if (h instanceof FileHandler fileHandler) {
            fileHandler.setLevel(Level.FINE);
          }
        }

        // Adjust individual loggers
        Logger.getLogger("DefaultNaftahParserVisitor").setLevel(Level.FINEST);
        Logger.getLogger("org.daiitech.naftah").setLevel(Level.FINE);
      }
    }
  }

  public static class VersionProvider implements IVersionProvider {
    @Override
    public String[] getVersion() {
      return new String[] {
        """
                  Naftah Version (إصدار نفطة): %s
                  JVM (آلة جافا الافتراضية): %s
                  Vendor (المُصنّع): %s
                  OS (نظام التشغيل): %s
                  """
            .formatted(
                NaftahSystem.getVersion(),
                System.getProperty(JAVA_VERSION_PROPERTY),
                System.getProperty(JAVA_VM_VENDOR_PROPERTY),
                System.getProperty(OS_NAME_PROPERTY))
      };
    }
  }

  @Command(
      name = NaftahCommand.NAME,
      customSynopsis = "naftah [run/shell/init] [options] [filename] [args]",
      description = {
        "The Naftah command line processor.",
        "معالج الأوامر الخاص بـلغة البرمجة نفطة"
      },
      sortOptions = false,
      versionProvider = VersionProvider.class)
  private static class NaftahCommand {
    private static final String NAME = "naftah";

    protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
      if (Boolean.getBoolean(DEBUG_PROPERTY)) Thread.sleep(5000);
      bootstrap(bootstrapAsync);
    }

    private static NaftahParser prepareRun(CharStream input) {
      return prepareRun(input, List.of());
    }

    private static NaftahParser prepareRun(CharStream input, ANTLRErrorListener errorListener) {
      return prepareRun(input, List.of(errorListener));
    }

    private static NaftahParser prepareRun(
        CharStream input, List<ANTLRErrorListener> errorListeners) {
      // Create a lexer and token stream
      var lexerCommonTokenStreamPair = getCommonTokenStream(input, errorListeners);

      CommonTokenStream tokens = lexerCommonTokenStreamPair.b;

      if (Boolean.getBoolean(DEBUG_PROPERTY)) {
        tokens.fill();
        System.out.println("Tokens:");
        for (Token token : tokens.getTokens()) {
          System.out.printf(
              "Token: %-20s Text: %s%n",
              lexerCommonTokenStreamPair.a.getVocabulary().getSymbolicName(token.getType()),
              token.getText());
        }
      }

      // Create a parser
      return getParser(tokens, errorListeners);
    }

    private static Object doRun(NaftahParser parser) {

      // Parse the input and get the parse tree
      ParseTree tree = parser.program();

      // Create a visitor and visit the parse tree
      DefaultNaftahParserVisitor visitor = new DefaultNaftahParserVisitor();
      return visitor.visit(tree);
    }

    @Command(
        name = RunCommand.NAME,
        customSynopsis = "naftah run [options] [filename] [args]",
        description = {
          "The Naftah run command. it starts the language interpreter (interpretes a naftah script).",
          "أمر تشغيل نفطة. يقوم بتشغيل مفسر اللغة (يُفسر سكربت بلغة نفطح)."
        },
        sortOptions = false)
    private static final class RunCommand extends NaftahCommand {
      private static final String NAME = "run";

      @Override
      protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
        System.setProperty(INSIDE_RUN_PROPERTY, Boolean.toString(true));
        super.run(main, bootstrapAsync);
        initLogger(main.debug);

        // Create an input stream from the Naftah code
        CharStream input = getCharStream(main.isScriptFile, main.script);

        var parser = NaftahCommand.prepareRun(input);
        var result = NaftahCommand.doRun(parser);

        if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) System.out.println(result);

        System.out.println();
      }
    }

    @Command(
        name = InitCommand.NAME,
        customSynopsis = "naftah init [options] [filename] [args]",
        description = {
          "The Naftah init command. it prepares the classpath classes (java classpath) and process them to reuse inside naftah script.",
          "أمر بدء نفطة. يقوم بتحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطة."
        },
        sortOptions = false)
    private static final class InitCommand extends NaftahCommand {
      private static final String NAME = "init";

      @Override
      protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
        System.setProperty(INSIDE_INIT_PROPERTY, Boolean.toString(true));
        System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
        super.run(main, bootstrapAsync);
      }
    }

    @Command(
        name = ShellCommand.NAME,
        customSynopsis = "naftah shell [options] [filename] [args]",
        description = {
          "The Naftah shell command. it starts a REPL (Read-Eval-Print Loop), an interactive programming environment where you can enter single lines of naftah code",
          "يبدأ أمر نفطة شال. يبدأ بيئة تفاعلية للبرمجة (REPL - قراءة-تقييم-طباعة)، حيث يمكنك إدخال أسطر مفردة من كود نفطح وتنفيذها فورًا."
        },
        sortOptions = false)
    private static final class ShellCommand extends NaftahCommand {
      private static final String NAME = "shell";

      @Override
      protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
        System.setProperty(INSIDE_REPL_PROPERTY, Boolean.toString(true));
        super.run(main, bootstrapAsync);
        Terminal terminal = getTerminal();

        setupRefreshTerminalWidthAndHeight(terminal);

        LineReader reader = getLineReader(terminal);

        setupHistoryConfig(reader);

        StringBuilder fullLine = new StringBuilder();

        while (true) {
          try {
            String line = MULTILINE_IS_ACTIVE ? reader.readLine(null, RTL_MULTILINE_PROMPT, (MaskingCallback)null, null) :
                    reader.readLine(null, RTL_PROMPT, (MaskingCallback)null, null);

            if (line.isBlank()) continue;

            if (List.of("exit", "خروج").contains(line.trim()))
              throw new UserInterruptException(line);

            fullLine.append(line);

            var input = getCharStream(false, fullLine.toString());

            if (MULTILINE_IS_ACTIVE) {
              reader.getHistory().add(fullLine.toString());
              MULTILINE_IS_ACTIVE = false;
            }

            fullLine.delete(0, fullLine.length());

            var parser = NaftahCommand.prepareRun(input);

            var result = NaftahCommand.doRun(parser);

            if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result))
              System.out.println(fillRightWithSpaces(result.toString()));
            System.out.println();

          } catch (UserInterruptException | EndOfFileException e) {
            String closingMsg = "تم الخروج من التطبيق.";
            System.out.println(
                //                fillRightWithSpaces(shouldReshape() ? shape(closingMsg) :
                // closingMsg));
                fillRightWithSpaces(closingMsg));
            break;
          } catch (IndexOutOfBoundsException ignored) {
            String currentLine =
                reader.getBuffer().atChar(reader.getBuffer().length() - 1) == '\n'
                    ? reader.getBuffer().substring(0, reader.getBuffer().length() - 2)
                    : reader.getBuffer().substring(0, reader.getBuffer().length() - 1);
            fullLine.append(currentLine);
            MULTILINE_IS_ACTIVE = true;
            println(reader);
          } catch (Throwable e) {
            // ignored
            // TODO: improve logging (in arabic)
            System.err.println("Caught: " + e);
            System.out.println();
          } finally {
            // Save history explicitly (though it's usually done automatically)
            reader.getHistory().save();
          }
        }
      }
    }

    @Option(
        names = {"-cp", "-classpath", "--classpath"},
        paramLabel = "<path>",
        description = {
          "Specify where to find the class files - must be first argument",
          "حدّد مكان ملفات الفئات (class files) — يجب أن يكون هو الوسيط الأول"
        })
    private String classpath;

    @Option(
        names = {"-D", "--define"},
        paramLabel = "<property=value>",
        description = {"Define a system property", "تعريف خاصية نظام"})
    private Map<String, String> systemProperties = new LinkedHashMap<>();

    @Option(
        names = {"-d", "--debug"},
        description = {
          "Debug mode will print out full stack traces",
          "في وضع التصحيح، سيتم طباعة تتبع الأخطاء الكامل."
        })
    private boolean debug;

    @Option(
        names = {"-c", "--encoding"},
        paramLabel = "<charset>",
        description = {"Specify the encoding of the files", "تحديد ترميز الملفات"})
    private String encoding;

    @Option(
        names = {"-scp", "--scan-classpath"},
        paramLabel = "<charset>",
        description = {
          "Specify if the classpath classes should be reused as nafta types",
          "حدد ما إذا كان يجب إعادة استخدام فئات المسار (classpath) كأنواع في نفطح."
        })
    private boolean scanClasspath;

    @Option(
        names = {"-f", "--force-scan-classpath"},
        paramLabel = "<charset>",
        description = {
          "Force scanning the classpath when (-scp, --scan-classpath) is provided.",
          "فرض فحص مسار الأصناف (classpath) عند توفير الخيار (-scp, --scan-classpath)."
        })
    private boolean forceScanClasspath;

    @Option(
        names = {"-e"},
        paramLabel = "<script>",
        description = {"Specify a command line script", "تحديد سكربت لسطر الأوامر"})
    private String script;

    @Option(
        names = {"-h", "--help"},
        usageHelp = true,
        description = {"Show this help message and exit", "عرض رسالة المساعدة هذه ثم الخروج"})
    private boolean helpRequested;

    @Option(
        names = {"-v", "--version"},
        versionHelp = true,
        description = {"Print version information and exit", "طباعة معلومات الإصدار والخروج"})
    private boolean versionRequested;

    @Unmatched List<String> arguments = new ArrayList<>();

    /**
     * Process the users request.
     *
     * @param parseResult the parsed result command line.
     * @throws ParameterException if the user input was invalid
     */
    private boolean process(ParseResult parseResult) throws ParameterException, IOException {
      var matchedCommand = (NaftahCommand) parseResult.commandSpec().userObject();
      // append to classpath
      if (Objects.nonNull(matchedCommand.classpath)) {
        final String actualClasspath = System.getProperty(CLASS_PATH_PROPERTY);
        System.setProperty(
            CLASS_PATH_PROPERTY, actualClasspath + File.pathSeparator + matchedCommand.classpath);
      }

      // append system properties
      for (Map.Entry<String, String> entry : matchedCommand.systemProperties.entrySet()) {
        System.setProperty(entry.getKey(), entry.getValue());
      }

      if (Objects.nonNull(matchedCommand.encoding)) {
        System.setProperty(FILE_ENCODING_PROPERTY, matchedCommand.encoding);
      }

      if (matchedCommand.scanClasspath) {
        System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
      }

      if (matchedCommand.forceScanClasspath) {
        System.setProperty(FORCE_CLASSPATH_PROPERTY, Boolean.toString(true));
      }

      final Naftah main = new Naftah();

      if (matchedCommand.debug) {
        main.debug = true;
        System.setProperty(DEBUG_PROPERTY, Boolean.toString(true));
      }

      if (matchedCommand instanceof RunCommand) {
        main.isScriptFile = matchedCommand.script == null;
        if (main.isScriptFile) {
          if (matchedCommand.arguments.isEmpty()) {
            throw new ParameterException(
                parseResult.commandSpec().commandLine(), "error: neither -e or filename provided");
          }
          main.script = matchedCommand.arguments.remove(0);
        } else {
          main.script = matchedCommand.script;
        }
      }

      main.args = matchedCommand.arguments;

      return main.run(matchedCommand);
    }
  }

  private static void processArgs(String[] args) {
    setupOutputStream();
    setupErrorStream();

    NaftahCommand naftahCommand = new NaftahCommand();

    CommandLine parser =
        new CommandLine(naftahCommand)
            .addSubcommand(new NaftahCommand.RunCommand())
            .addSubcommand(new NaftahCommand.InitCommand())
            .addSubcommand(new NaftahCommand.ShellCommand())
            .setSubcommandsCaseInsensitive(true)
            .setOut(new PrintWriter(System.out))
            .setErr(new PrintWriter(System.err))
            .setUnmatchedArgumentsAllowed(true)
            .setStopAtUnmatched(true);

    try {
      ParseResult result = parser.parseArgs(args);
      if (printHelpIfRequested(result)) {
        return;
      }

      if (ObjectUtils.isEmpty(result.subcommands()))
        throw new InitializationException("error: no command provided: run/shell/init");

      var matchedSubCommandResult = result.subcommands().get(result.subcommands().size() - 1);

      if (!naftahCommand.process(matchedSubCommandResult)) {
        // If we fail, then exit with an error so scripting frameworks can catch it.
        System.exit(1);
      } else System.exit(0);

    } catch (ParameterException ex) { // command line arguments could not be parsed
      System.err.println(ex.getMessage());
      ex.getCommandLine().usage(System.err);
    } catch (IOException ioe) {
      System.err.println("error: " + ioe.getMessage());
    }
  }

  private boolean run(NaftahCommand naftahCommand) {
    try {
      naftahCommand.run(this, !(naftahCommand instanceof NaftahCommand.InitCommand));
      return true;
    } catch (Throwable e) {
      System.err.println("Caught: " + e);
      if (debug) {
        e.printStackTrace();
      }
      return false;
    }
  }
}
