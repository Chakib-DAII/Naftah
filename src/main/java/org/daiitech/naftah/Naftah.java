package org.daiitech.naftah;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.utils.JulLoggerConfig;
import org.jline.reader.EOFError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import picocli.CommandLine;

import static java.util.logging.Logger.getLogger;

import static org.daiitech.naftah.NaftahSystem.setupErrorStream;
import static org.daiitech.naftah.NaftahSystem.setupLocale;
import static org.daiitech.naftah.NaftahSystem.setupOutputStream;
import static org.daiitech.naftah.NaftahSystem.setupRefreshTerminalWidthAndHeight;
import static org.daiitech.naftah.NaftahSystem.setupTerminalWidthAndHeight;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.getNaftahValueToString;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.isSimpleOrBuiltinOrCollectionOrMapOfSimpleType;
import static org.daiitech.naftah.builtin.utils.ObjectUtils.replaceAllNulls;
import static org.daiitech.naftah.parser.DefaultContext.bootstrap;
import static org.daiitech.naftah.parser.NaftahParserHelper.doRun;
import static org.daiitech.naftah.parser.NaftahParserHelper.getCharStream;
import static org.daiitech.naftah.parser.NaftahParserHelper.prepareRun;
import static org.daiitech.naftah.utils.OS.OS_NAME_PROPERTY;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static org.daiitech.naftah.utils.repl.REPLHelper.MULTILINE_IS_ACTIVE;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_MULTILINE_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.getLineReader;
import static org.daiitech.naftah.utils.repl.REPLHelper.getTerminal;
import static org.daiitech.naftah.utils.repl.REPLHelper.println;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupHistoryConfig;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupTerminalCapabilities;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.IVersionProvider;
import static picocli.CommandLine.InitializationException;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParameterException;
import static picocli.CommandLine.ParseResult;
import static picocli.CommandLine.Unmatched;
import static picocli.CommandLine.printHelpIfRequested;

/**
 * Main entry point of the Naftah programming language.
 * <p>
 * Naftah is an interpreted JVM language.
 * This class handles command line argument parsing and execution of
 * different commands such as running scripts, initializing classpath scanning,
 * and starting an interactive shell (REPL).
 * </p>
 *
 * <p>
 * Supports debug mode, encoding options, and classpath scanning configuration.
 * </p>
 *
 * <p>
 * The supported file extensions for Naftah scripts are:
 * {@code ".naftah", ".nfth", ".na", ".nsh"}.
 * </p>
 *
 * @author Chakib Daii
 */
public final class Naftah {

	/**
	 * System property key for the Java version.
	 */
	public static final String JAVA_VERSION_PROPERTY = "java.version";

	/**
	 * System property key for the Java Virtual Machine vendor.
	 */
	public static final String JAVA_VM_VENDOR_PROPERTY = "java.vm.vendor";

	/**
	 * Property to enable scanning the Java classpath for Naftah types.
	 */
	public static final String SCAN_CLASSPATH_PROPERTY = "naftah.scanClassPath";

	/**
	 * Property to force scanning the Java classpath.
	 */
	public static final String FORCE_CLASSPATH_PROPERTY = "naftah.forceClassPathScan";

	/**
	 * Property to enable debug mode.
	 */
	public static final String DEBUG_PROPERTY = "naftah.debug";

	/**
	 * Property set when inside the REPL (shell).
	 */
	public static final String INSIDE_REPL_PROPERTY = "naftah.repl";

	/**
	 * Property set when inside the init command.
	 */
	public static final String INSIDE_INIT_PROPERTY = "naftah.init";

	/**
	 * Property set when inside the run command.
	 */
	public static final String INSIDE_RUN_PROPERTY = "naftah.run";

	/**
	 * Property key for file encoding.
	 */
	public static final String FILE_ENCODING_PROPERTY = "file.encoding";

	/**
	 * Property to enable Vector API optimizations.
	 */
	public static final String VECTOR_API_PROPERTY = "naftah.vector.api.active";

	/**
	 * The recognized standard file extensions for Naftah scripts.
	 */
	public static final String[] STANDARD_EXTENSIONS = {".naftah", ".nfth", ".na", ".nsh"};

	static {
		setupTerminalWidthAndHeight(NaftahSystem::getTerminalWidthAndHeight);
	}

	// Script arguments
	private List<String> args;

	// Indicates if the script source is a file
	private boolean isScriptFile;

	// The script source: either filename or raw content
	private String script;

	// Whether debug mode is enabled (affects error stack traces)
	private boolean debug = false;

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private Naftah() {
		throw new NaftahBugError("استخدام غير مسموح به.");
	}

	/**
	 * Main method of the Naftah interpreter.
	 * Parses command line arguments and dispatches commands.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		processArgs(args);
	}

	/**
	 * Initializes the Java Util Logging configuration.
	 * If debug is true, logging levels are set to verbose.
	 *
	 * @param debug true to enable debug logging, false otherwise
	 */
	private static void initLogger(boolean debug) {
		try {
			String loggingConfigFile = System.getProperty("java.util.logging.config.file");

			// Initialize logging from external file
			if (Objects.nonNull(loggingConfigFile)) {
				JulLoggerConfig.initialize(loggingConfigFile);
			}
			else {
				JulLoggerConfig.initializeFromResources("logging.properties");
			}
		}
		catch (IOException e) {
			try {
				// fallback to default logging
				JulLoggerConfig.initializeFromResources("logging.properties");
			}
			catch (IOException ex) {
				throw new NaftahBugError(ex);
			}
		}
		finally {
			if (debug) {
				// Adjust ConsoleHandler (if it exists)
				Logger rootLogger = getLogger("");
				for (Handler h : rootLogger.getHandlers()) {
					if (h instanceof ConsoleHandler consoleHandler) {
						consoleHandler.setLevel(Level.FINEST);
					}
					else if (h instanceof FileHandler fileHandler) {
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
	 * Processes the command line arguments and dispatches the appropriate command.
	 *
	 * @param args the raw command line arguments
	 */
	private static void processArgs(String[] args) {
		setupOutputStream();
		setupErrorStream();
		setupLocale();

		NaftahCommand naftahCommand = new NaftahCommand();

		CommandLine parser = new CommandLine(naftahCommand).addSubcommand(new NaftahCommand.RunCommand()).addSubcommand(new NaftahCommand.InitCommand()).addSubcommand(new NaftahCommand.ShellCommand()).setSubcommandsCaseInsensitive(true).setOut(new PrintWriter(System.out)).setErr(new PrintWriter(System.err)).setUnmatchedArgumentsAllowed(true).setStopAtUnmatched(true);

		try {
			ParseResult result = parser.parseArgs(args);
			// TODO: pad output
			if (printHelpIfRequested(result)) {
				return;
			}

			if (ObjectUtils.isEmpty(result.subcommands())) {
				throw new InitializationException("خطأ: لم يتم تقديم أمر (run/shell/init)");
			}

			var matchedSubCommandResult = result.subcommands().get(result.subcommands().size() - 1);

			if (!naftahCommand.process(matchedSubCommandResult)) {
				// If we fail, then exit with an error so scripting frameworks can catch it.
				System.exit(1);
			}
			else {
				System.exit(0);
			}

		}
		catch (ParameterException ex) { // command line arguments could not be parsed
			printPaddedErrorMessageToString(ex);
			// TODO: pad output
			ex.getCommandLine().usage(System.err);
		}
		catch (IOException ioe) {
			printPaddedErrorMessageToString(ioe);
		}
	}

	/**
	 * Prints the string representation of the given object with padding applied.
	 *
	 * @param o the object to convert to a string and print with padding
	 */
	private static void printPaddedToString(Object o) {
		padText(getNaftahValueToString(o), true);
	}

	/**
	 * Prints the error message of the given throwable with padding applied.
	 * If the throwable is not an instance of {@link NaftahBugError},
	 * it will be wrapped inside a {@code NaftahBugError}.
	 * The error message is formatted in Arabic as "تم التقاط الخطأ: 'message'".
	 *
	 * @param t the throwable whose error message will be printed with padding
	 */
	private static void printPaddedErrorMessageToString(Throwable t) {
		Throwable throwable = t;
		if (!(throwable instanceof NaftahBugError)) {
			throwable = new NaftahBugError(throwable);
		}
		padText(Objects.nonNull(throwable.getMessage()) ? String.format("تم التقاط الخطأ: '%s'", replaceAllNulls(throwable.getMessage())) : throwable.toString(), true);
	}

	/**
	 * Runs the given NaftahCommand.
	 *
	 * @param naftahCommand the command to run
	 * @return true if successful, false otherwise
	 */
	private boolean run(NaftahCommand naftahCommand) {
		try {
			naftahCommand.run(this, !(naftahCommand instanceof NaftahCommand.InitCommand));
			return true;
		}
		catch (ParseCancellationException e) {
			System.exit(1); // stop program
		}
		catch (Throwable t) {
			printPaddedErrorMessageToString(t);
			if (debug) {
				t.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Provides version information for the Naftah command line.
	 */
	public static class VersionProvider implements IVersionProvider {
		@Override
		public String[] getVersion() {
			return new String[]{"""
					Naftah Version (إصدار نفطة): %s
					JVM (آلة جافا الافتراضية): %s
					Vendor (المُصنّع): %s
					OS (نظام التشغيل): %s
					""".formatted(NaftahSystem.getVersion(), System.getProperty(JAVA_VERSION_PROPERTY), System.getProperty(JAVA_VM_VENDOR_PROPERTY), System.getProperty(OS_NAME_PROPERTY))};
		}
	}

	/**
	 * The base command class for the Naftah CLI, implemented with picocli.
	 * Supports subcommands: run, init, and shell.
	 */
	@Command(name = NaftahCommand.NAME, customSynopsis = "naftah [run/shell/init] [options] [filename] [args]", description = {"The Naftah command line processor.", "معالج الأوامر الخاص بـلغة البرمجة نفطة"}, sortOptions = false, versionProvider = VersionProvider.class)
	private static class NaftahCommand {
		/**
		 * The main command name.
		 */
		private static final String NAME = "naftah";
		@Option(names = {"-D", "--define"}, paramLabel = "<property=value>", description = {"Define a system property", "تعريف خاصية نظام"})
		private final Map<String, String> systemProperties = new LinkedHashMap<>();
		@Unmatched
		List<String> arguments = new ArrayList<>();
		@Option(names = {"-cp", "-classpath", "--classpath"}, paramLabel = "<path>", description = {"Specify where to find the class files - must be first argument", "حدّد مكان ملفات الفئات (class files) — يجب أن يكون هو الوسيط الأول"})
		private String classpath;
		@Option(names = {"-d", "--debug"}, description = {"Debug mode will print out full stack traces", "في وضع التصحيح، سيتم طباعة تتبع الأخطاء الكامل."})
		private boolean debug;
		@Option(names = {"-c", "--encoding"}, paramLabel = "<charset>", description = {"Specify the encoding of the files", "تحديد ترميز الملفات"})
		private String encoding;
		@Option(names = {"-scp", "--scan-classpath"}, paramLabel = "<charset>", description = {"Specify if the classpath classes should be reused as nafta types", "حدد ما إذا كان يجب إعادة استخدام فئات المسار (classpath) كأنواع في نفطح."})
		private boolean scanClasspath;
		@Option(names = {"-f", "--force-scan-classpath"}, paramLabel = "<charset>", description = {"Force scanning the classpath when (-scp, --scan-classpath) is provided.", "فرض فحص مسار الأصناف (classpath) عند توفير الخيار (-scp, --scan-classpath)."})
		private boolean forceScanClasspath;
		@Option(names = {"-e"}, paramLabel = "<script>", description = {"Specify a command line script", "تحديد سكربت لسطر الأوامر"})
		private String script;
		@Option(names = {"-h", "--help"}, usageHelp = true, description = {"Show this help message and exit", "عرض رسالة المساعدة هذه ثم الخروج"})
		private boolean helpRequested;
		@Option(names = {"-v", "--version"}, versionHelp = true, description = {"Print version information and exit", "طباعة معلومات الإصدار والخروج"})
		private boolean versionRequested;
		@Option(names = {"-vec", "--vector"}, description = {"Enable Vector API optimizations for performance", "تمكين تحسينات واجهة برمجة التطبيقات المتجهة لتحسين الأداء"})
		private boolean useVectorApi;

		/**
		 * Runs the command.
		 *
		 * @param main           the main Naftah instance
		 * @param bootstrapAsync whether to bootstrap asynchronously
		 * @throws Exception if any error occurs
		 */
		protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
			if (Boolean.getBoolean(DEBUG_PROPERTY)) {
				Thread.sleep(5000);
			}
			bootstrap(bootstrapAsync);
		}

		/**
		 * Processes the parsed command line arguments and configures the environment.
		 *
		 * @param parseResult the parsed command line result
		 * @return true if processing succeeded; false otherwise
		 * @throws ParameterException if the command line is invalid
		 * @throws IOException        if an I/O error occurs
		 */
		private boolean process(ParseResult parseResult) throws ParameterException, IOException {
			var matchedCommand = (NaftahCommand) parseResult.commandSpec().userObject();
			// append to classpath
			if (Objects.nonNull(matchedCommand.classpath)) {
				final String actualClasspath = System.getProperty(CLASS_PATH_PROPERTY);
				System.setProperty(CLASS_PATH_PROPERTY, actualClasspath + File.pathSeparator + matchedCommand.classpath);
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
						throw new ParameterException(parseResult.commandSpec().commandLine(), "خطأ: لم يتم تقديم الخيار -e ولا اسم الملف.");
					}
					main.script = matchedCommand.arguments.remove(0);
				}
				else {
					main.script = matchedCommand.script;
				}
			}

			if (matchedCommand.useVectorApi) {
				System.setProperty(VECTOR_API_PROPERTY, Boolean.toString(true));
			}

			main.args = matchedCommand.arguments;

			return main.run(matchedCommand);
		}

		/**
		 * The 'run' subcommand that interprets a Naftah script.
		 */
		@Command(name = RunCommand.NAME, customSynopsis = "naftah run [options] [filename] [args]", description = {"The Naftah run command. it starts the language interpreter (interpretes a naftah script).", "أمر تشغيل نفطة. يقوم بتشغيل مفسر اللغة (يُفسر سكربت بلغة نفطح)."}, sortOptions = false)
		private static final class RunCommand extends NaftahCommand {
			private static final String NAME = "run";

			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_RUN_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
				initLogger(main.debug);

				// Create an input stream from the Naftah code
				CharStream input = getCharStream(main.isScriptFile, main.script);

				var parser = prepareRun(input, NaftahErrorListener.INSTANCE);
				var result = doRun(parser);

				if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) {
					printPaddedToString(result);
				}

				System.out.println();
			}
		}

		/**
		 * The 'init' subcommand that prepares Java classpath classes for Naftah reuse.
		 */
		@Command(name = InitCommand.NAME, customSynopsis = "naftah init [options] [filename] [args]", description = {"The Naftah init command. it prepares the classpath classes (java classpath) and process them to reuse inside naftah script.", "أمر بدء نفطة. يقوم بتحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطة."}, sortOptions = false)
		private static final class InitCommand extends NaftahCommand {
			private static final String NAME = "init";

			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_INIT_PROPERTY, Boolean.toString(true));
				System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
			}
		}

		/**
		 * The 'shell' subcommand that starts the interactive Naftah REPL.
		 */
		@Command(name = ShellCommand.NAME, customSynopsis = "naftah shell [options] [filename] [args]", description = {"The Naftah shell command. it starts a REPL (Read-Eval-Print Loop), an interactive programming environment where you can enter single lines of naftah code", "يبدأ أمر نفطة شال. يبدأ بيئة تفاعلية للبرمجة (REPL - قراءة-تقييم-طباعة)، حيث يمكنك إدخال أسطر مفردة من كود نفطح وتنفيذها فورًا."}, sortOptions = false)
		private static final class ShellCommand extends NaftahCommand {
			private static final String NAME = "shell";

			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_REPL_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
				Terminal terminal = getTerminal();

				setupTerminalCapabilities(terminal);

				setupRefreshTerminalWidthAndHeight(terminal);

				LineReader reader = getLineReader(terminal);

				setupHistoryConfig(reader);

				StringBuilder fullLine = new StringBuilder();

				while (true) {
					try {
						String line = MULTILINE_IS_ACTIVE ? reader.readLine(null, RTL_MULTILINE_PROMPT, (MaskingCallback) null, null) : reader.readLine(null, RTL_PROMPT, (MaskingCallback) null, null);

						if (!MULTILINE_IS_ACTIVE && line.isBlank()) {
							continue;
						}

						checkManagementCommands(line);

						if (!line.isBlank()) {
							fullLine.append(line);
						}

						var input = getCharStream(false, fullLine.toString());

						if (MULTILINE_IS_ACTIVE) {
							reader.getHistory().add(fullLine.toString());
							MULTILINE_IS_ACTIVE = false;
						}

						fullLine.delete(0, fullLine.length());

						var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

						var result = doRun(parser);

						if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result)) {
							printPaddedToString(result);
						}
						System.out.println();

					}
					catch (UserInterruptException | EndOfFileException e) {
						String closingMsg = "تم الخروج من التطبيق.";
						padText(closingMsg, true);
						break;
					}
					catch (IndexOutOfBoundsException | EOFError ignored) {
						String currentLine = reader.getBuffer().atChar(reader.getBuffer().length() - 1) == '\n' ? reader.getBuffer().substring(0, reader.getBuffer().length() - 2) : reader.getBuffer().substring(0, reader.getBuffer().length() - 1);
						fullLine.append(currentLine);
						MULTILINE_IS_ACTIVE = true;
						println(reader);
					}
					catch (Throwable t) {
						printPaddedErrorMessageToString(t);
					}
					finally {
						// Save history explicitly (though it's usually done automatically)
						reader.getHistory().save();
					}
				}
			}

			/**
			 * Checks for management commands like '/exit' or '/خروج' and handles them accordingly.
			 *
			 * @param line the input line to check
			 */
			private void checkManagementCommands(String line) {
				/*
				 * TODO: add support for /reset : reset repl /list : list all valid code
				 * snippets /drop : drops a variable /vars : list variables /functions : list
				 * created functions /save : save the valid codes snippets into a file /history
				 * : shows full history /help : shows help of commands
				 */

				if (List.of("/exit", "/خروج").contains(line.trim())) {
					throw new UserInterruptException(line);
				}
			}
		}
	}
}
