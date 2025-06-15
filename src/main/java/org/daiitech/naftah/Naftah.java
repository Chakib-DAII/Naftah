package org.daiitech.naftah;

import static org.daiitech.naftah.core.NaftahSystem.setupOutput;
import static org.daiitech.naftah.core.builtin.utils.ObjectUtils.isSimpleOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.utils.DefaultContext.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.daiitech.naftah.core.NaftahSystem;
import org.daiitech.naftah.core.parser.DefaultNaftahParserVisitor;
import org.daiitech.naftah.core.parser.NaftahLexer;
import org.daiitech.naftah.core.parser.NaftahParser;
import org.daiitech.naftah.core.utils.JulLoggerConfig;
import picocli.CommandLine;

/**
 * @author Chakib Daii
 *     <p>main of Naftah programming language as an interpreted JVM language
 */
public final class Naftah {

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
    bootstrap();
    initLogger();
    setupOutput();
    // TODO: update this logic to be more sophisticated and handle args and commands
    // Create an input stream from the Naftah code
    CharStream input =
        CharStreams.fromPath(searchForNaftahScriptFile(args[0]).toPath(), StandardCharsets.UTF_8);

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
  }

  private static void initLogger() {
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

  public static class VersionProvider implements CommandLine.IVersionProvider {
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

  @CommandLine.Command(
      name = "naftah",
      customSynopsis = "naftah [options] [filename] [args]",
      description = "The Naftah command line processor.",
      sortOptions = false,
      versionProvider = VersionProvider.class)
  private static final class NaftahCommand {

    // IMPLEMENTATION NOTE:
    // classpath must be the first argument, so that the `startNaftah(.bat)` script
    // can extract it and the JVM can be started with the classpath already correctly set.
    // This saves us from having to fork a new JVM process with the classpath set from the processed
    // arguments.
    @CommandLine.Option(
        names = {"-cp", "-classpath", "--classpath"},
        paramLabel = "<path>",
        description = "Specify where to find the class files - must be first argument")
    private String classpath;

    @CommandLine.Option(
        names = {"-D", "--define"},
        paramLabel = "<property=value>",
        description = "Define a system property")
    private Map<String, String> systemProperties = new LinkedHashMap<String, String>();

    @CommandLine.Option(
        names = "--disableopt",
        paramLabel = "optlist",
        split = ",",
        description = {
          "Disables one or all optimization elements; optlist can be a comma separated list with the elements: ",
          "all (disables all optimizations), ",
          "int (disable any int based optimizations)"
        })
    private List<String> disableopt = new ArrayList<String>();

    @CommandLine.Option(
        names = {"-d", "--debug"},
        description = "Debug mode will print out full stack traces")
    private boolean debug;

    @CommandLine.Option(
        names = {"-c", "--encoding"},
        paramLabel = "<charset>",
        description = "Specify the encoding of the files")
    private String encoding;

    @CommandLine.Option(
        names = {"-e"},
        paramLabel = "<script>",
        description = "Specify a command line script")
    private String script;

    @CommandLine.Option(
        names = {"-i"},
        arity = "0..1",
        paramLabel = "<extension>",
        description = "Modify files in place; create backup if extension is given (e.g. '.bak')")
    private String extension;

    @CommandLine.Option(
        names = {"-n"},
        description = "Process files line by line using implicit 'line' variable")
    private boolean lineByLine;

    @CommandLine.Option(
        names = {"-p"},
        description = "Process files line by line and print result (see also -n)")
    private boolean lineByLinePrint;

    @CommandLine.Option(
        names = {"-pa", "--parameters"},
        description = "Generate metadata for reflection on method parameter names (jdk8+ only)")
    private boolean parameterMetadata;

    @CommandLine.Option(
        names = {"-pr", "--enable-preview"},
        description = "Enable preview Java features (jdk12+ only)")
    private boolean previewFeatures;

    @CommandLine.Option(
        names = "-l",
        arity = "0..1",
        paramLabel = "<port>",
        description = "Listen on a port and process inbound lines (default: 1960)")
    private String port;

    @CommandLine.Option(
        names = {"-a", "--autosplit"},
        arity = "0..1",
        paramLabel = "<splitPattern>",
        description =
            "Split lines using splitPattern (default '\\s') using implicit 'split' variable")
    private String splitPattern;

    @CommandLine.Option(
        names = {"--configscript"},
        paramLabel = "<script>",
        description = "A script for tweaking the configuration options")
    private String configscript;

    @CommandLine.Option(
        names = {"-b", "--basescript"},
        paramLabel = "<class>",
        description = "Base class name for scripts (must derive from Script)")
    private String scriptBaseClass;

    @CommandLine.Option(
        names = {"-h", "--help"},
        usageHelp = true,
        description = "Show this help message and exit")
    private boolean helpRequested;

    @CommandLine.Option(
        names = {"-v", "--version"},
        versionHelp = true,
        description = "Print version information and exit")
    private boolean versionRequested;

    @CommandLine.Option(
        names = {"--compile-static"},
        description = "Use CompileStatic")
    private boolean compileStatic;

    @CommandLine.Option(
        names = {"--type-checked"},
        description = "Use TypeChecked")
    private boolean typeChecked;

    @CommandLine.Unmatched List<String> arguments = new ArrayList<>();

    /**
     * Process the users request.
     *
     * @param parser the parsed command line. Used when the user input was invalid.
     * @throws CommandLine.ParameterException if the user input was invalid
     */
    boolean process(CommandLine parser) throws CommandLine.ParameterException, IOException {
      for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
        System.setProperty(entry.getKey(), entry.getValue());
      }

      final Naftah main = new Naftah();

      main.debug = debug;

      main.isScriptFile = script == null;
      if (main.isScriptFile) {
        if (arguments.isEmpty()) {
          throw new CommandLine.ParameterException(
              parser, "error: neither -e or filename provided");
        }
        main.script = arguments.remove(0);
        if (main.script.endsWith(".java")) {
          throw new CommandLine.ParameterException(
              parser, "error: cannot compile file with .java extension: " + main.script);
        }
      } else {
        main.script = script;
      }

      main.args = arguments;

      return main.run();
    }
  }

  static void processArgs(String[] args, final PrintStream out, final PrintStream err) {
    NaftahCommand naftahCommand = new NaftahCommand();

    CommandLine parser =
        new CommandLine(naftahCommand)
            .setOut(new PrintWriter(out))
            .setErr(new PrintWriter(err))
            .setUnmatchedArgumentsAllowed(true)
            .setStopAtUnmatched(true);

    try {
      CommandLine.ParseResult result = parser.parseArgs(args);

      if (CommandLine.printHelpIfRequested(result)) {
        return;
      }

      // TODO: pass printstream(s) down through process
      if (!naftahCommand.process(parser)) {
        // If we fail, then exit with an error so scripting frameworks can catch it.
        System.exit(1);
      }

    } catch (CommandLine.ParameterException ex) { // command line arguments could not be parsed
      err.println(ex.getMessage());
      ex.getCommandLine().usage(err);
    } catch (IOException ioe) {
      err.println("error: " + ioe.getMessage());
    }
  }

  private boolean run() {
    try {
      processOnce();
      return true;
    } catch (Throwable e) {
      System.err.println("Caught: " + e);
      if (debug) {
        e.printStackTrace();
      }
      return false;
    }
  }

  private void processOnce() throws IOException, URISyntaxException {
    //        NaftahShell naftah = new NaftahShell(Thread.currentThread().getContextClassLoader(),
    // conf);
    //        setupContextClassLoader(naftah);
    //        naftah.run(getScriptSource(isScriptFile, script), args);
  }
}
