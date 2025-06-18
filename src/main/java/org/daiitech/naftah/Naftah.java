package org.daiitech.naftah;

import static java.util.logging.Logger.*;
import static org.daiitech.naftah.NaftahSystem.setupErrorStream;
import static org.daiitech.naftah.NaftahSystem.setupOutputStream;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.*;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.parser.DefaultNaftahParserVisitor;
import org.daiitech.naftah.parser.NaftahLexer;
import org.daiitech.naftah.parser.NaftahParser;
import org.daiitech.naftah.utils.JulLoggerConfig;
import picocli.CommandLine;

/**
 * @author Chakib Daii
 *     <p>main of Naftah programming language as an interpreted JVM language
 */
public final class Naftah {

  public static final String SCAN_CLASSPATH_PROPERTY = "scanClassPath";
  public static final String FILE_ENCODING_PROPERTY = "file.encoding";

  private static final String[] STANDARD_EXTENSIONS = {".naftah", ".nfth", ".na", ".nsh"};

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

  public static CharStream getCharStream(boolean isScriptFile, String script) throws IOException {
    CharStream charStream;
    if (isScriptFile) {
      charStream = CharStreams.fromPath(searchForNaftahScriptFile(script).toPath(), StandardCharsets.UTF_8);
    } else {
      charStream =  CharStreams.fromString(script);
    }
    return charStream;
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
      name = "naftah",
      customSynopsis = "naftah [options] [filename] [args]",
      description = "The Naftah command line processor.",
      sortOptions = false,
      versionProvider = VersionProvider.class)
  private static final class NaftahCommand {

    // IMPLEMENTATION NOTE:
    // classpath must be the first argument, so that the `naftah(.bat)` script
    // can extract it and the JVM can be started with the classpath already correctly set.
    // This saves us from having to fork a new JVM process with the classpath set from the processed
    // arguments.
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
     * @param parser the parsed command line. Used when the user input was invalid.
     * @throws ParameterException if the user input was invalid
     */
    boolean process(CommandLine parser) throws ParameterException, IOException {
      // append to classpath
      if (Objects.nonNull(classpath)) {
        final String actualClasspath = System.getProperty(CLASS_PATH_PROPERTY);
        System.setProperty(CLASS_PATH_PROPERTY, actualClasspath + File.pathSeparator + classpath);
      }

      // append system properties
      for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
        System.setProperty(entry.getKey(), entry.getValue());
      }

      if(Objects.nonNull(encoding)) {
      System.setProperty(FILE_ENCODING_PROPERTY, encoding);
      }

      if(scanClasspath) {
        System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
      }

      final Naftah main = new Naftah();

      main.debug = debug;

      main.isScriptFile = script == null;
      if (main.isScriptFile) {
        if (arguments.isEmpty()) {
          throw new ParameterException(
              parser, "error: neither -e or filename provided");
        }
        main.script = arguments.remove(0);
      } else {
        main.script = script;
      }

      main.args = arguments;

      return main.run();
    }
  }

  static void processArgs(String[] args) {
    setupOutputStream();
    setupErrorStream();
    NaftahCommand naftahCommand = new NaftahCommand();

    CommandLine parser =
        new CommandLine(naftahCommand)
            .setOut(new PrintWriter(System.out))
            .setErr(new PrintWriter(System.err))
            .setUnmatchedArgumentsAllowed(true)
            .setStopAtUnmatched(true);

    try {
      ParseResult result = parser.parseArgs(args);

      if (printHelpIfRequested(result)) {
        return;
      }

      if (!naftahCommand.process(parser)) {
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

  private boolean run() {
    try {
      runLang();
      return true;
    } catch (Throwable e) {
      System.err.println("Caught: " + e);
      if (debug) {
        e.printStackTrace();
      }
      return false;
    }
  }

  private void runLang() throws IOException {
    bootstrap();
    initLogger(debug);
    // TODO: update this logic to be more sophisticated and handle args and commands
    // Create an input stream from the Naftah code
    CharStream input = getCharStream(isScriptFile, script);


    // Create a lexer and token stream
    NaftahLexer lexer = new NaftahLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Create a parser
    NaftahParser parser = new NaftahParser(tokens);

    // Parse the input and get the parse tree
    ParseTree tree = parser.program();

    // Create a visitor and visit the parse tree
    DefaultNaftahParserVisitor visitor = new DefaultNaftahParserVisitor();
    var result = visitor.visit(tree);

    if (isSimpleOrCollectionOrMapOfSimpleType(result)) System.out.println(result);

    System.out.println();
    System.exit(0);
  }
}
