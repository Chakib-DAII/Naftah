package org.daiitech.naftah;

import static java.util.logging.Logger.*;
import static org.daiitech.naftah.NaftahSystem.*;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;
import static org.daiitech.naftah.parser.NaftahParserHelper.*;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.parser.DefaultNaftahParserVisitor;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.parser.SyntaxHighlighter;
import org.daiitech.naftah.utils.JulLoggerConfig;
import org.daiitech.naftah.utils.jline.CompositeHighlighter;
import org.jline.reader.*;
import picocli.CommandLine;

import org.jline.terminal.Terminal;
/**
 * @author Chakib Daii
 *     <p>main of Naftah programming language as an interpreted JVM language
 */
public final class Naftah {

  public static final String SCAN_CLASSPATH_PROPERTY = "scanClassPath";
  public static final String INSIDE_SHELL_PROPERTY = "insideShell";
  public static final String FILE_ENCODING_PROPERTY = "file.encoding";
  public static final String TERMINAL_WIDTH_PROPERTY = "terminal.width";
  public static final String TERMINAL_HEIGHT_PROPERTY = "terminal.hight";

  public static final String[] STANDARD_EXTENSIONS = {".naftah", ".nfth", ".na", ".nsh"};

  static {
    int[] terminalWidthAndHeight = getTerminalWidthAndHeight();
    System.setProperty(TERMINAL_WIDTH_PROPERTY, Integer.toString(terminalWidthAndHeight[0]));
    System.setProperty(TERMINAL_HEIGHT_PROPERTY, Integer.toString(terminalWidthAndHeight[1]));
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

  public static void main(String[] args) throws IOException {
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
        "Naftah Version: "
            + NaftahSystem.getVersion()
            + " JVM: "
            + System.getProperty("java.version")
            + " Vendor: "
            + System.getProperty("java.vm.vendor")
            + " OS: "
            + System.getProperty("os.name")
      };
    }
  }

  @Command(
      name = NaftahCommand.NAME,
      customSynopsis = "naftah [run/shell/init] [options] [filename] [args]",
      description = "The Naftah command line processor.",
      sortOptions = false,
      versionProvider = VersionProvider.class)
  private static class NaftahCommand {
    private static final String NAME = "naftah";

    protected void run(Naftah main) throws IOException {
      bootstrap();
    }

    private static NaftahParser prepareRun(CharStream input) {
      return prepareRun(input, List.of());
    }

    private static NaftahParser prepareRun(CharStream input, ANTLRErrorListener errorListener) {
      return prepareRun(input, List.of(errorListener));
    }
    private static NaftahParser prepareRun(CharStream input, List<ANTLRErrorListener> errorListeners) {
      // Create a lexer and token stream
      CommonTokenStream tokens = getCommonTokenStream(input, errorListeners);

      // Create a parser
      return getParser(tokens, errorListeners);
    }
    private static Object doRun( NaftahParser parser) {

      // Parse the input and get the parse tree
      ParseTree tree = parser.program();

      // Create a visitor and visit the parse tree
      DefaultNaftahParserVisitor visitor = new DefaultNaftahParserVisitor();
      return visitor.visit(tree);
    }

    // IMPLEMENTATION NOTE:
    // classpath must be the first argument, so that the `naftah(.bat)` script
    // can extract it and the JVM can be started with the classpath already correctly set.
    // This saves us from having to fork a new JVM process with the classpath set from the processed
    // arguments.

    @Command(
            name = RunCommand.NAME,
            customSynopsis = "run [options] [filename] [args]",
            description = "The Naftah command line processor.",
            sortOptions = false)
    private static final class RunCommand extends NaftahCommand {
      private static final String NAME = "run";

      @Override
      protected void run(Naftah main) throws IOException {
        super.run(main);
        initLogger(main.debug);

        // Create an input stream from the Naftah code
        CharStream input = getCharStream(main.isScriptFile, main.script);

        var parser =  NaftahCommand.prepareRun(input, new ConsoleErrorListener());
        var result =  NaftahCommand.doRun(parser);

        if (isSimpleOrCollectionOrMapOfSimpleType(result)) System.out.println(result);

        System.out.println();
        System.exit(0);
      }
    }

    @Command(
            name = InitCommand.NAME,
            customSynopsis = "init [options] [filename] [args]",
            description = "The Naftah command line processor.",
            sortOptions = false)
    private static final class InitCommand extends NaftahCommand {
      private static final String NAME = "init";

      @Override
      protected void run(Naftah main) throws IOException {
        System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
        super.run(main);
      }
    }

    @Command(
            name = ShellCommand.NAME,
            customSynopsis = "shell [options] [filename] [args]",
            description = "The Naftah command line processor.",
            sortOptions = false)
    private static final class ShellCommand extends NaftahCommand{
      private static final String NAME = "shell";

      private static LineReader getLineReader(Terminal terminal) {
        LineReader baseReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        Highlighter originalHighlighter = baseReader.getHighlighter();

        return LineReaderBuilder.builder()
                .terminal(terminal)
                .highlighter(new SyntaxHighlighter(originalHighlighter))
                .build();
      }

      @Override
      protected void run(Naftah main) throws IOException {
        System.setProperty(INSIDE_SHELL_PROPERTY, Boolean.toString(true));
        super.run(main);
        Terminal terminal = getTerminal();

        LineReader reader = getLineReader(terminal);

        while (true) {
          try {
            String rtlPrompt = "<>"; // Right-to-left mark before prompt
            String line = reader.readLine(rtlPrompt);

            if (line.isBlank()) continue;

            if (line.trim().equals("exit")) break;

            var input = getCharStream(false, line);

            var parser =  NaftahCommand.prepareRun(input);

            Object result = NaftahCommand.doRun(parser);

            if (isSimpleOrCollectionOrMapOfSimpleType(result)) System.out.println(result);

          } catch (UserInterruptException | EndOfFileException e) {
          System.out.println("\nتم الخروج من التطبيق.");
          break;
        } catch (Throwable ignored) {
            // ignored
          }
        }
      }
    }

    @Option(
        names = {"-cp", "-classpath", "--classpath"},
        paramLabel = "<path>",
        description = "Specify where to find the class files - must be first argument")
    private String classpath;

    @Option(
        names = {"-D", "--define"},
        paramLabel = "<property=value>",
        description = "Define a system property")
    private Map<String, String> systemProperties = new LinkedHashMap<>();

    @Option(
        names = {"-d", "--debug"},
        description = "Debug mode will print out full stack traces")
    private boolean debug;

    @Option(
        names = {"-c", "--encoding"},
        paramLabel = "<charset>",
        description = "Specify the encoding of the files")
    private String encoding;

    @Option(
            names = {"-scp", "--scan-classpath"},
            paramLabel = "<charset>",
            description = "Specify if the classpath classes should be reused as nafta types")
    private boolean scanClasspath;


    @Option(
        names = {"-e"},
        paramLabel = "<script>",
        description = "Specify a command line script")
    private String script;

    @Option(
        names = {"-h", "--help"},
        usageHelp = true,
        description = "Show this help message and exit")
    private boolean helpRequested;

    @Option(
        names = {"-v", "--version"},
        versionHelp = true,
        description = "Print version information and exit")
    private boolean versionRequested;

    @Unmatched List<String> arguments = new ArrayList<>();

    /**
     * Process the users request.
     *
     * @param parseResult the parsed result command line.
     * @throws ParameterException if the user input was invalid
     */
    boolean process(ParseResult parseResult) throws ParameterException, IOException {
      var matchedCommand = (NaftahCommand)parseResult.commandSpec().userObject();
      // append to classpath
      if (Objects.nonNull(matchedCommand.classpath)) {
        final String actualClasspath = System.getProperty(CLASS_PATH_PROPERTY);
        System.setProperty(CLASS_PATH_PROPERTY, actualClasspath + File.pathSeparator + matchedCommand.classpath);
      }

      // append system properties
      for (Map.Entry<String, String> entry : matchedCommand.systemProperties.entrySet()) {
        System.setProperty(entry.getKey(), entry.getValue());
      }

      if(Objects.nonNull(matchedCommand.encoding)) {
      System.setProperty(FILE_ENCODING_PROPERTY, matchedCommand.encoding);
      }

      if(matchedCommand.scanClasspath) {
        System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
      }

      final Naftah main = new Naftah();

      main.debug = matchedCommand.debug;

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

  static void processArgs(String[] args) {
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

      var matchedSubCommandResult = result.subcommands().get(result.subcommands().size() -1);

      if (!naftahCommand.process(matchedSubCommandResult)) {
        // If we fail, then exit with an error so scripting frameworks can catch it.
        System.exit(1);
      }

    } catch (ParameterException ex) { // command line arguments could not be parsed
      System.err.println(ex.getMessage());
      ex.getCommandLine().usage(System.err);
    } catch (IOException ioe) {
      System.err.println("error: " + ioe.getMessage());
    }
  }

  private boolean run(NaftahCommand naftahCommand) {
    try {
      setupOutputStream();
      setupErrorStream();
      naftahCommand.run(this);
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
