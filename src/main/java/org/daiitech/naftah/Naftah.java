package org.daiitech.naftah;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.daiitech.naftah.builtin.lang.BuiltinFunction;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.utils.ResourceUtils;
import org.daiitech.naftah.utils.arabic.ArabicUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;
import org.jline.reader.EOFError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.MaskingCallback;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import picocli.CommandLine;

import static java.util.logging.Logger.getLogger;

import static org.daiitech.naftah.NaftahSystem.TERMINAL_HEIGHT_PROPERTY;
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
import static org.daiitech.naftah.utils.JulLoggerConfig.JAVA_LOGGING_FILE_PROPERTY;
import static org.daiitech.naftah.utils.JulLoggerConfig.LOGGING_FILE;
import static org.daiitech.naftah.utils.JulLoggerConfig.initialize;
import static org.daiitech.naftah.utils.JulLoggerConfig.initializeFromResources;
import static org.daiitech.naftah.utils.OS.OS_NAME_PROPERTY;
import static org.daiitech.naftah.utils.ResourceUtils.getJarDirectory;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.ARABIC;
import static org.daiitech.naftah.utils.arabic.ArabicUtils.padText;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_CALL_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_NAME_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.classToDetailedString;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static org.daiitech.naftah.utils.repl.REPLHelper.MULTILINE_IS_ACTIVE;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_MULTILINE_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.getLineReader;
import static org.daiitech.naftah.utils.repl.REPLHelper.getMarkdownAsString;
import static org.daiitech.naftah.utils.repl.REPLHelper.getTerminal;
import static org.daiitech.naftah.utils.repl.REPLHelper.println;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupHistoryConfig;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupKeyBindingsConfig;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupTerminalCapabilities;
import static org.daiitech.naftah.utils.repl.REPLHelper.shouldQuit;

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
	 * Property set when inside the man command.
	 */
	public static final String INSIDE_MAN_PROPERTY = "naftah.man";
	/**
	 * Controls whether class and function definitions should be loaded on startup.
	 */
	public static final String LOAD_CLASSES_AND_FUNCTIONS_PROPERTY = "naftah.man.load.active";
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
	 * Property to enable Arabic formatter in Naftah.
	 */
	public static final String ARABIC_NUMBER_FORMATTER_PROPERTY = "naftah.arabic.formatter.active";
	/**
	 * Property to enable Arabic-Indic digit formatting in Naftah.
	 */
	public static final String ARABIC_INDIC_PROPERTY = "naftah.arabic.indic.active";
	/**
	 * Property to enable caching of multiline text processing in Naftah.
	 */
	public static final String MULTILINE_CACHE_PROPERTY = "naftah.cache.multiline.active";
	/**
	 * Property to enable caching of string interpolation text processing in Naftah.
	 */
	public static final String INTERPOLATION_CACHE_PROPERTY = "naftah.cache.interpolation.active";
	/**
	 * Property to specify the path to a Naftah configuration file.
	 */
	public static final String CONFIG_FILE_PROPERTY = "naftah.config.file";
	/**
	 * Property to specify which builtin function set to use in Naftah.
	 */
	public static final String BUILTIN_CLASSES_PROPERTY = "naftah.builtinClasses";
	/**
	 * Property to specify which package of builtin function set to use in Naftah.
	 */
	public static final String BUILTIN_PACKAGES_PROPERTY = "naftah.builtinPackages";
	/**
	 * Configuration key representing the classes of builtin function set.
	 */
	public static final String BUILTIN_CLASSES = "builtinClasses";
	/**
	 * Configuration key representing the packages of builtin function set.
	 */
	public static final String BUILTIN_PACKAGES = "builtinPackages";
	/**
	 * Default filename for the Naftah configuration file.
	 */
	public static final String CONFIG_FILE = "naftah.properties";
	/**
	 * The recognized standard file extensions for Naftah scripts.
	 */
	public static final String[] STANDARD_EXTENSIONS = {".naftah", ".nfth", ".na", ".nsh"};
	/**
	 * Logger instance for logging Naftah program.
	 */
	private static Logger LOGGER;

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
	 */
	private Naftah() {
	}

	/**
	 * Main method of the Naftah interpreter.
	 * Parses command line arguments and dispatches commands.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty(ReleaseInfo.NAFTAH_VERSION_PROPERTY, ReleaseInfo.getVersion());
		System.setProperty(ReleaseInfo.NAFTAH_VERSION_DATE_PROPERTY, ReleaseInfo.getBuildDate());
		System.setProperty(ReleaseInfo.NAFTAH_VERSION_TIME_PROPERTY, ReleaseInfo.getBuildTime());
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
			String loggingConfigFile = System.getProperty(JAVA_LOGGING_FILE_PROPERTY);

			// Initialize logging from external file
			if (Objects.nonNull(loggingConfigFile)) {
				initialize(loggingConfigFile);
			}
			else {
				initializeFromResources(LOGGING_FILE);
			}
		}
		catch (IOException e) {
			try {
				// fallback to default logging
				initializeFromResources(LOGGING_FILE);
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
				getLogger("DefaultNaftahParserVisitor").setLevel(Level.FINEST);
				LOGGER = getLogger("org.daiitech.naftah");
				LOGGER.setLevel(Level.FINE);
			}
		}
	}

	/**
	 * Processes the given configuration properties and updates corresponding system properties
	 * for built-in function classes and packages.
	 *
	 * <p>If the provided {@code properties} include entries for
	 * {@link #BUILTIN_CLASSES} or {@link #BUILTIN_PACKAGES}, their values are appended
	 * to the existing system properties {@link #BUILTIN_CLASSES_PROPERTY} and
	 * {@link #BUILTIN_PACKAGES_PROPERTY}, respectively. This behavior ensures that
	 * built-in configurations are cumulative—existing values are preserved and new ones are appended.</p>
	 *
	 * @param properties the configuration properties to process; may be {@code null}
	 */
	private static void processConfig(Properties properties) {
		if (Objects.nonNull(properties)) {
			if (properties.containsKey(BUILTIN_CLASSES)) {
				// builtin classes config is cumulative
				var current = System.getProperty(BUILTIN_CLASSES_PROPERTY);
				System
						.setProperty(   BUILTIN_CLASSES_PROPERTY,
										(Objects.nonNull(current) ? current + ", " : "") + properties
												.get(BUILTIN_CLASSES));
			}

			if (properties.containsKey(BUILTIN_PACKAGES)) {
				// builtin packages config is cumulative
				var current = System.getProperty(BUILTIN_PACKAGES_PROPERTY);
				System
						.setProperty(   BUILTIN_PACKAGES_PROPERTY,
										(Objects.nonNull(current) ? current + ", " : "") + properties
												.get(BUILTIN_PACKAGES));
			}
		}
	}

	/**
	 * Initializes Naftah configuration by loading default and optional external property files.
	 *
	 * <p>The initialization process follows these steps:</p>
	 * <ol>
	 * <li>Load default configuration properties from the resource file {@link #CONFIG_FILE}.</li>
	 * <li>Apply built-in configuration values via {@link #processConfig(Properties)}.</li>
	 * <li>If a system property {@link #CONFIG_FILE_PROPERTY} is defined, attempt to load
	 * external configuration from the specified file. External values override defaults.</li>
	 * <li>If loading fails (e.g., due to {@link NaftahBugError}), fall back to default properties.</li>
	 * </ol>
	 *
	 * <p>All successfully processed properties update system properties, including
	 * {@link #BUILTIN_CLASSES_PROPERTY} and {@link #BUILTIN_PACKAGES_PROPERTY}.</p>
	 */
	public static void initConfig() {
		Properties properties = null;
		try {
			String configFile = System.getProperty(CONFIG_FILE_PROPERTY);

			// load default config
			properties = ResourceUtils.getPropertiesFromResources(CONFIG_FILE);
			processConfig(properties);
			properties = null;

			if (Objects.nonNull(configFile)) {
				// external config are priority if exists
				properties = ResourceUtils.getProperties(configFile);
			}
		}
		catch (NaftahBugError e) {
			// fallback to default config
			properties = ResourceUtils.getPropertiesFromResources(CONFIG_FILE);
		}
		finally {
			processConfig(properties);
		}
	}

	/**
	 * Processes the command line arguments and dispatches the appropriate command.
	 *
	 * @param args the raw command line arguments
	 */
	public static void processArgs(String[] args) {
		setupOutputStream();
		setupErrorStream();
		setupLocale();

		NaftahCommand naftahCommand = new NaftahCommand();

		CommandLine parser = new CommandLine(naftahCommand)
				.addSubcommand(new NaftahCommand.RunCommand())
				.addSubcommand(new NaftahCommand.InitCommand())
				.addSubcommand(new NaftahCommand.ShellCommand())
				.addSubcommand(new NaftahCommand.ManualCommand())
				.setSubcommandsCaseInsensitive(true)
				.setOut(new PrintWriter(System.out))
				.setErr(new PrintWriter(System.err))
				.setUnmatchedArgumentsAllowed(true)
				.setStopAtUnmatched(true);

		try {
			ParseResult result = parser.parseArgs(args);
			// TODO: pad output
			if (printHelpIfRequested(result)) {
				return;
			}

			if (ObjectUtils.isEmpty(result.subcommands())) {
				throw new InitializationException("خطأ: لم يتم تقديم أمر (run/shell/init/man)");
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
		padText(Objects.nonNull(throwable.getMessage()) ?
				String.format("تم التقاط الخطأ: '%s'", replaceAllNulls(throwable.getMessage())) :
				throwable.toString(), true);
	}

	/**
	 * Runs the given NaftahCommand.
	 *
	 * @param naftahCommand the command to run
	 * @return true if successful, false otherwise
	 */
	private boolean run(NaftahCommand naftahCommand) {
		try {
			naftahCommand
					.run(   this,
							!(naftahCommand instanceof NaftahCommand.InitCommand || naftahCommand instanceof NaftahCommand.ManualCommand));
			return true;
		}
		catch (ParseCancellationException e) {
			System.exit(1); // stop program
		}
		catch (Throwable t) {
			printPaddedErrorMessageToString(t);
			if (debug) {
				if (Objects.nonNull(LOGGER)) {
					LOGGER.fine(Arrays.toString(t.getStackTrace()));
				}
				else {
					padText(Arrays.toString(t.getStackTrace()), true);
				}
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
								"""
					.formatted( NaftahSystem.getVersion(),
								System.getProperty(JAVA_VERSION_PROPERTY),
								System.getProperty(JAVA_VM_VENDOR_PROPERTY),
								System.getProperty(OS_NAME_PROPERTY))};
		}
	}

	/**
	 * The base command class for the Naftah CLI, implemented with picocli.
	 * Supports subcommands: run, init, and shell.
	 */
	@Command(   name = NaftahCommand.NAME,
				customSynopsis = "naftah [run/shell/init] [options] [filename] [args]",
				description = {"The Naftah command line processor.", "معالج الأوامر الخاص بـلغة البرمجة نفطة"},
				sortOptions = false,
				versionProvider = VersionProvider.class)
	private static class NaftahCommand {
		/**
		 * The main command name.
		 */
		private static final String NAME = "naftah";

		@Option(names = {"-D", "--define"},
				paramLabel = "<property=value>",
				description = {"Define a system property", "تعريف خاصية نظام"})
		private final Map<String, String> systemProperties = new LinkedHashMap<>();

		@Unmatched
		List<String> arguments = new ArrayList<>();

		@Option(
				names = "--enable-cache",
				split = ",",
				description = {
								"""
								Enable specific caches (disabled by default). M for multiline and I for string interpolation.
								""",
								"""
								تمكين أنواع محددة من الكاش (وهي معطلة بشكل افتراضي). M للنصوص متعددة الأسطر و I للاستيفاء النصي.
								"""
				}
		)
		List<String> enabledCaches = new ArrayList<>();

		@Option(names = {"-cp", "-classpath", "--classpath"},
				paramLabel = "<path>",
				description = { "Specify where to find the class files - must be first argument",
								"حدّد مكان ملفات الفئات (class files) — يجب أن يكون هو الوسيط الأول"})
		private String classpath;

		@Option(names = {"-d", "--debug"},
				description = { "Debug mode will print out full stack traces",
								"في وضع التصحيح، سيتم طباعة تتبع الأخطاء الكامل."})
		private boolean debug;

		@Option(names = {"-c", "--encoding"},
				paramLabel = "<charset>",
				description = {"Specify the encoding of the files", "تحديد ترميز الملفات"})
		private String encoding;

		@Option(names = {"-scp", "--scan-classpath"},
				paramLabel = "<charset>",
				description = { "Specify if the classpath classes should be reused as nafta types",
								"حدد ما إذا كان يجب إعادة استخدام فئات المسار (classpath) كأنواع في نفطح."})
		private boolean scanClasspath;

		@Option(names = {"-f", "--force-scan-classpath"},
				paramLabel = "<charset>",
				description = { "Force scanning the classpath when (-scp, --scan-classpath) is provided.",
								"فرض فحص مسار الأصناف (classpath) عند توفير الخيار (-scp, --scan-classpath)."})
		private boolean forceScanClasspath;

		@Option(names = {"-e"},
				paramLabel = "<script>",
				description = {"Specify a command line script", "تحديد سكربت لسطر الأوامر"})
		private String script;

		@Option(names = {"-h", "--help"},
				usageHelp = true,
				description = {"Show this help message and exit", "عرض رسالة المساعدة هذه ثم الخروج"})
		private boolean helpRequested;

		@Option(names = {"-v", "--version"},
				versionHelp = true,
				description = {"Print version information and exit", "طباعة معلومات الإصدار والخروج"})
		private boolean versionRequested;

		@Option(names = {"-vec", "--vector"},
				description = { "Enable Vector API optimizations for performance",
								"تمكين تحسينات واجهة برمجة التطبيقات المتجهة لتحسين الأداء"})
		private boolean useVectorApi;

		@Option(names = {"-ar_f", "--arabic_formatting"},
				description = {
								"Use Arabic numerals and formatting symbols (e.g., decimal separator, digit shapes).",
								"استخدام الأرقام العربية ورموز التنسيق (مثل الفاصلة العشرية وأشكال الأرقام)."
				})
		private boolean useArabicFormatter;

		@Option(names = {"-ar_ind", "--arabic_indic"},
				description = {
								"Display numbers using Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩)",
								"عرض الأرقام باستخدام الأرقام الهندية-العربية (٠١٢٣٤٥٦٧٨٩)"
				})
		private boolean useArabicIndic;

		@Option(names = {"-load_clf", "--load_classes_and_functions"},
				description = {
								"",
								""
				})
		private boolean loadClassesAndFunctions;

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
			initConfig();
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
				System
						.setProperty(   CLASS_PATH_PROPERTY,
										actualClasspath + File.pathSeparator + matchedCommand.classpath);
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
						throw new ParameterException(   parseResult.commandSpec().commandLine(),
														"خطأ: لم يتم تقديم الخيار -e ولا اسم الملف.");
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

			if (matchedCommand.useArabicFormatter) {
				System.setProperty(ARABIC_NUMBER_FORMATTER_PROPERTY, Boolean.toString(true));
			}

			if (matchedCommand.useArabicIndic) {
				System.setProperty(ARABIC_INDIC_PROPERTY, Boolean.toString(true));
			}

			if (!matchedCommand.enabledCaches.isEmpty()) {
				System
						.setProperty(   MULTILINE_CACHE_PROPERTY,
										Boolean.toString(matchedCommand.enabledCaches.contains("M")));
				System
						.setProperty(   INTERPOLATION_CACHE_PROPERTY,
										Boolean.toString(matchedCommand.enabledCaches.contains("I")));
			}

			if (matchedCommand.loadClassesAndFunctions) {
				System.setProperty(LOAD_CLASSES_AND_FUNCTIONS_PROPERTY, Boolean.toString(true));
			}

			main.args = matchedCommand.arguments;

			return main.run(matchedCommand);
		}

		/**
		 * The 'run' subcommand that interprets a Naftah script.
		 */
		@Command(   name = RunCommand.NAME,
					customSynopsis = "naftah run [options] [filename] [args]",
					description = {
									"The Naftah run command. it starts the language interpreter (interpretes a naftah script).",
									"أمر تشغيل نفطة. يقوم بتشغيل مفسر اللغة (يُفسر سكربت بلغة نفطح)."},
					sortOptions = false)
		private static final class RunCommand extends NaftahCommand {
			private static final String NAME = "run";

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_RUN_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
				initLogger(main.debug);

				// Create an input stream from the Naftah code
				CharStream input = getCharStream(main.isScriptFile, main.script);

				var parser = prepareRun(input, NaftahErrorListener.INSTANCE);
				var result = doRun(parser, main.args);

				if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result) && !None.isNone(result)) {
					printPaddedToString(result);
				}

				System.out.println();
			}
		}

		/**
		 * The 'init' subcommand that prepares Java classpath classes for Naftah reuse.
		 */
		@Command(   name = InitCommand.NAME,
					customSynopsis = "naftah init [options] [filename] [args]",
					description = { """
									The Naftah init command. it prepares the classpath classes (java classpath) and process them to reuse inside naftah script.""",
									"""
									أمر بدء نفطة. يقوم بتحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطة."""},
					sortOptions = false)
		private static final class InitCommand extends NaftahCommand {
			private static final String NAME = "init";

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_INIT_PROPERTY, Boolean.toString(true));
				System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
			}
		}


		/**
		 * The 'man' subcommand that loads and displays documentation topics related to Naftah usage.
		 */
		@Command(   name = ManualCommand.NAME,
					customSynopsis = "naftah man [options] [filename] [args]",
					description = {
									"""
									The Naftah manual command. It loads and displays documentation topics related to Naftah usage.""",
									"""
									أمر 'man' في نفطة. يعرض صفحات المساعدة والمواضيع الخاصة باستخدام نفطة."""
					},
					sortOptions = false)
		private static final class ManualCommand extends NaftahCommand {
			private static final String NAME = "man";
			private static final int PAGE_SIZE = 5;
			private final Path manualDir = Paths.get(getJarDirectory().getParent() + "/manual");
			private final List<String> classes = new CopyOnWriteArrayList<>();
			private final List<String> accessibleClasses = new CopyOnWriteArrayList<>();
			private final List<String> instantiableClasses = new CopyOnWriteArrayList<>();
			private final List<String> builtinFunctions = new CopyOnWriteArrayList<>();
			private final List<String> jvmFunctions = new CopyOnWriteArrayList<>();

			private LineReader reader;
			private Map<String, Path> topics;

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_MAN_PROPERTY, Boolean.toString(true));
				System.setProperty(SCAN_CLASSPATH_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);
				if (Boolean.getBoolean(LOAD_CLASSES_AND_FUNCTIONS_PROPERTY)) {
					loadClassesAndFunctions();
				}

				topics = loadAvailableTopics();

				Terminal terminal = getTerminal();

				setupTerminalCapabilities(terminal);

				setupRefreshTerminalWidthAndHeight(terminal);

				reader = getLineReader(terminal, topics.keySet());

				setupKeyBindingsConfig(reader);

				String line = null;

				while (true) {
					try {
						if (!main.args.isEmpty()) {
							line = String.join("", main.args).trim();
							main.args.clear();
						}
						else {
							if (Objects.isNull(line)) {
								padText("""
										مرحبًا بك في الواجهة التفاعلية للكتيبات التقنية لنفطه.

										إذا كنت تستخدم الأداة لأول مرة أو لا تعرف الأوامر المتاحة،
										اكتب الأمر التالي لعرض قائمة الأوامر والتعليمات:

										مساعدة أو usage

										""", true);
							}
							line = reader.readLine(null, RTL_PROMPT, (MaskingCallback) null, null).trim();
						}

						if (line.isBlank()) {
							continue;
						}

						var matchedManagementCommand = checkManagementCommands(line);

						if (!matchedManagementCommand) {
							if (topics.containsKey(line)) {
								showManualTopic(line);
							}
							else {
								String arabicQualifiedNameOrBuiltinFunction = null;
								String[] lineParts;
								if (line.contains(".") && !line.contains(QUALIFIED_NAME_SEPARATOR)) {
									if ((lineParts = line.split(QUALIFIED_CALL_SEPARATOR)).length == 2) {
										arabicQualifiedNameOrBuiltinFunction = ClassUtils
												.getQualifiedCall(ClassUtils
														.getQualifiedName(
																			lineParts[0]), lineParts[1]);

									}
									else if (lineParts.length == 1) {
										arabicQualifiedNameOrBuiltinFunction = ClassUtils.getQualifiedName(line);
									}
								}
								else {
									var builtinFunctionOpt = Optional
											.ofNullable(DefaultContext
													.getBuiltinFunctions()
													.get(line));

									if (builtinFunctionOpt.isPresent()) {
										var builtinFunctions = builtinFunctionOpt.get();
										if (builtinFunctions.size() == 1) {
											arabicQualifiedNameOrBuiltinFunction = builtinFunctions
													.get(0)
													.toDetailedString();
										}
										else {
											arabicQualifiedNameOrBuiltinFunction = IntStream
													.range( 0,
															builtinFunctions.size())
													.mapToObj(index -> """
																		%s
																		----------------------------------------------
																		%s
																		"""
															.formatted(
																		index + 1,
																		builtinFunctions
																				.get(index)
																				.toDetailedString()))
													.collect(Collectors.joining());
										}
									}
								}

								if (arabicQualifiedNameOrBuiltinFunction != null) {
									padText(arabicQualifiedNameOrBuiltinFunction, true);
								}
								else {
									padText("لم يتم العثور على دليل للموضوع.", true);
								}
							}
						}

						System.out.println();
					}
					catch (UserInterruptException | EndOfFileException e) {
						String closingMsg = "تم الخروج من التطبيق.";
						padText(closingMsg, true);
						break;
					}
					catch (Throwable t) {
						printPaddedErrorMessageToString(t);
					}
				}
			}

			/**
			 * Checks if the given input line matches any predefined management commands.
			 * <p>
			 * Recognized commands include help, listing topics, displaying Java classes,
			 * accessible classes, instantiable classes, builtin functions, JVM functions,
			 * and exiting the program. Commands can be provided in either English or Arabic.
			 * </p>
			 *
			 * @param line the input command line to check
			 * @return {@code true} if the input matches a known command and the corresponding
			 *         action has been executed; {@code false} otherwise
			 * @throws UserInterruptException if the input command is an exit command ("exit" or "خروج"),
			 *                                which interrupts the user session and exits the program
			 */
			private boolean checkManagementCommands(String line) {
				var matched = false;
				String command = line.trim().toLowerCase(ARABIC);

				if (List.of("usage", "مساعدة").contains(command)) {
					matched = true;
					padText(
							"""
							\t- المواضيع أو list -> المواضيع المتوفرة.
							\t- <اسم الموضوع> -> فتح دليل الموضوع.
							\t- الأصناف أو classes -> الأصناف المتوفرة في Java مع أسمائها المؤهلة بالعربية.
							\t- الأصناف-المتاحة أو accessible-classes -> الأصناف المتاحة في Java مع أسمائها المؤهلة بالعربية.
							\t- الأصناف-القابلة-للتهيئة أو الأصناف-القابلة-للصنع أو instantiable-classes -> الأصناف القابلة للتهيئة في Java مع أسمائها المؤهلة بالعربية.
							\t- الدوال-المدمجة أو builtin-functions -> الدوال المدمجة في نظام نفطة.
							\t- دوال-جافا أو jvm-functions -> دوال JVM المتوفرة مع استدعاءاتها المؤهلة بالعربية.
							\t- <الاسم المؤهل لصنف Java> -> تحويل الاسم إلى الصيغة العربية (نفطة).
							\t- مساعدة أو usage -> عرض هذه التعليمات.
							\t- خروج أو exit -> إنهاء البرنامج.
							""",
							true);
				}
				else if (List.of("list", "المواضيع").contains(command)) {
					matched = true;
					padText("المواضيع المتوفرة:", true);
					topics
							.keySet()
							.forEach(topic -> padText("\t- " + ArabicUtils
									.transliterateToArabicScriptDefault(false, topic)[0] + " - " + topic, true));
				}
				else if (List.of("classes", "الأصناف").contains(command)) {
					matched = true;
					padText("الأصناف المتوفرة في Java مع أسمائها المؤهلة بالعربية:", true);
					printPaginated(classes);
				}
				else if (List.of("accessible-classes", "الأصناف-المتاحة").contains(command)) {
					matched = true;
					padText("الأصناف المتاحة في Java مع أسمائها المؤهلة بالعربية:", true);
					printPaginated(accessibleClasses);
				}
				else if (List
						.of("instantiable-classes", "الأصناف-القابلة-للصنع", "الأصناف-القابلة-للتهيئة")
						.contains(command)) {
							matched = true;
							padText("الأصناف القابلة للتهيئة في Java مع أسمائها المؤهلة بالعربية:", true);
							printPaginated(instantiableClasses);
						}
				else if (List.of("builtin-functions", "الدوال-المدمجة").contains(command)) {
					matched = true;
					padText("الدوال المدمجة في نظام نفطة:", true);
					printPaginated(builtinFunctions);
				}
				else if (List.of("jvm-functions", "دوال-جافا").contains(command)) {
					matched = true;
					padText("دوال JVM المتوفرة مع استدعاءاتها المؤهلة بالعربية:", true);
					printPaginated(jvmFunctions);
				}
				else if (List.of("exit", "خروج").contains(command)) {
					throw new UserInterruptException(line);
				}

				return matched;
			}

			/**
			 * Loads detailed metadata for classes and functions from the {@link DefaultContext}
			 * in parallel using a fixed thread pool executor.
			 * <p>
			 * This method asynchronously populates multiple collections with formatted, readable
			 * representations of:
			 * <ul>
			 * <li>All classes available in the context</li>
			 * <li>Accessible classes (e.g., public classes)</li>
			 * <li>Instantiable classes (i.e., non-abstract, non-interface)</li>
			 * <li>Built-in functions</li>
			 * <li>JVM functions</li>
			 * </ul>
			 *
			 * <p>Each task formats the content using {@code classToDetailedString(...)} or
			 * {@code toDetailedString()} methods, and surrounds each entry with visual dividers
			 * for terminal output.
			 *
			 * <p>If a function group contains more than one overload or variant, each one is
			 * numbered and printed with its own separator.
			 *
			 * <p>The method uses a fixed thread pool of 5 threads for concurrent execution
			 * and ensures proper shutdown of the executor.
			 *
			 * @implNote This method does not block for task completion or handle exceptions
			 *           from submitted tasks. It's designed for background population of in-memory lists.
			 */
			private void loadClassesAndFunctions() {
				ExecutorService executor = Executors.newFixedThreadPool(5);

				try {
					Runnable classesLoaderTask = () -> loadDetailedClasses(DefaultContext.getClasses(), classes);
					executor.submit(classesLoaderTask);

					Runnable accessibleClassesLoaderTask = () -> loadDetailedClasses(   DefaultContext
																								.getAccessibleClasses(),
																						accessibleClasses);
					executor.submit(accessibleClassesLoaderTask);

					Runnable instantiableClassesLoaderTask = () -> loadDetailedClasses( DefaultContext
																								.getInstantiableClasses(),
																						instantiableClasses);
					executor.submit(instantiableClassesLoaderTask);

					Runnable builtinFunctionsLoaderTask = () -> DefaultContext
							.getBuiltinFunctions()
							.entrySet()
							.stream()
							.map(this::loadBuiltinFunction)
							.filter(Objects::nonNull)
							.forEach(builtinFunctions::add);
					executor.submit(builtinFunctionsLoaderTask);

					Runnable jvmFunctionsLoaderTask = () -> DefaultContext
							.getJvmFunctions()
							.entrySet()
							.stream()
							.map(this::loadJvmFunction)
							.filter(Objects::nonNull)
							.forEach(jvmFunctions::add);
					executor.submit(jvmFunctionsLoaderTask);
				}
				finally {
					executor.shutdown();
				}
			}

			/**
			 * Builds a detailed textual representation of a single built-in function entry.
			 * <p>
			 * The output includes the function group name (key) and a formatted list of one or
			 * more {@link BuiltinFunction} details separated by visual dividers for clarity.
			 * </p>
			 *
			 * @param builtinFunction a map entry containing the function group name as key and
			 *                        a list of {@link BuiltinFunction} instances as value
			 * @return a formatted string containing a detailed description of the built-in functions
			 */
			private String loadBuiltinFunction(Map.Entry<String, List<BuiltinFunction>> builtinFunction) {
				return """
						---------------------------------------------------
						%s
						%s
						---------------------------------------------------
						"""
						.formatted( builtinFunction.getKey(),

									builtinFunction
											.getValue()
											.size() == 1 ?
													builtinFunction
															.getValue()
															.get(0)
															.toDetailedString() :
													IntStream
															.range( 0,
																	builtinFunction
																			.getValue()
																			.size())
															.mapToObj(index -> """
																				%s
																				----------------------------------------------
																				%s
																				"""
																	.formatted(
																				index + 1,
																				builtinFunction
																						.getValue()
																						.get(index)
																						.toDetailedString()))
															.collect(Collectors.joining())
						);
			}

			/**
			 * Builds a detailed textual representation of a single JVM function entry.
			 * <p>
			 * The output includes the function group name (key) and a formatted list of one or
			 * more {@link JvmFunction} details separated by visual dividers for readability.
			 * </p>
			 *
			 * @param jvmFunction a map entry containing the function group name as key and
			 *                    a list of {@link JvmFunction} instances as value
			 * @return a formatted string containing a detailed description of the JVM functions
			 */
			private String loadJvmFunction(Map.Entry<String, List<JvmFunction>> jvmFunction) {
				return """
						---------------------------------------------------
						%s
						%s
						---------------------------------------------------
						"""
						.formatted( jvmFunction.getKey(),
									jvmFunction
											.getValue()
											.size() == 1 ?
													jvmFunction
															.getValue()
															.get(0)
															.toDetailedString() :
													IntStream
															.range( 0,
																	jvmFunction
																			.getValue()
																			.size())
															.mapToObj(index -> """
																				%s
																				----------------------------------------------
																				%s
																				"""
																	.formatted(
																				index + 1,
																				jvmFunction
																						.getValue()
																						.get(index)
																						.toDetailedString()))
															.collect(Collectors.joining())
						);
			}

			/**
			 * Determines the total number of elements in a target list, ensuring the count
			 * is at least as large as the corresponding collection from {@link DefaultContext}.
			 * <p>
			 * This helps synchronize manual or temporary lists with the runtime context
			 * to avoid index mismatches when accessing elements.
			 * </p>
			 *
			 * @param target the list whose size should be compared to its related collection
			 * @return the maximum of the target's size and the associated context collection size
			 */
			private int getTotal(List<String> target) {
				int total = target.size();
				if (classes.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getClasses()
											.size());
				}
				else if (accessibleClasses.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getAccessibleClasses()
											.size());
				}
				else if (instantiableClasses.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getInstantiableClasses()
											.size());
				}
				else if (builtinFunctions.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getBuiltinFunctions()
											.size());
				}
				else if (jvmFunctions.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getJvmFunctions()
											.size());
				}
				return total;
			}


			/**
			 * Retrieves and formats the detailed information for a class or function
			 * at a given index from the specified target list or its related context collection.
			 * <p>
			 * If the requested index exceeds the target list’s current size, this method
			 * attempts to fetch the corresponding element from the appropriate collection
			 * in {@link DefaultContext}. Depending on the target, it delegates to one of:
			 * <ul>
			 * <li>{@code loadDetailedClass(Map.Entry)}</li>
			 * <li>{@code loadBuiltinFunction(Map.Entry)}</li>
			 * <li>{@code loadJvmFunction(Map.Entry)}</li>
			 * </ul>
			 * </p>
			 *
			 * @param index  the index of the element to retrieve (0-based)
			 * @param target the list corresponding to one of the tracked element types
			 * @return a formatted string representing the detailed view of the element
			 * @throws NaftahBugError if no valid element can be resolved for the given index
			 */
			private String loadClassOrFunction(int index, List<String> target) {
				if (target.size() > index) {
					return target.get(index);
				}
				else {
					String result = null;
					boolean validIndex = true;
					if (classes.equals(target) && (validIndex = DefaultContext
							.getClasses()
							.entrySet()
							.size() > index)) {
						var element = CollectionUtils
								.getElementAt(
												DefaultContext
														.getClasses()
														.entrySet(),
												index);
						if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
							result = loadDetailedClass((Map.Entry<String, Class<?>>) entry);
						}
					}
					else if (accessibleClasses.equals(target) && (validIndex = DefaultContext
							.getAccessibleClasses()
							.entrySet()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getAccessibleClasses()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									result = loadDetailedClass((Map.Entry<String, Class<?>>) entry);
								}
							}
					else if (instantiableClasses.equals(target) && (validIndex = DefaultContext
							.getInstantiableClasses()
							.entrySet()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getInstantiableClasses()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									result = loadDetailedClass((Map.Entry<String, Class<?>>) entry);
								}
							}
					else if (builtinFunctions.equals(target) && (validIndex = DefaultContext
							.getBuiltinFunctions()
							.entrySet()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getBuiltinFunctions()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									result = loadBuiltinFunction((Map.Entry<String, List<BuiltinFunction>>) entry);
								}
							}
					else if (jvmFunctions.equals(target) && (validIndex = DefaultContext
							.getJvmFunctions()
							.entrySet()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getJvmFunctions()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									result = loadJvmFunction((Map.Entry<String, List<JvmFunction>>) entry);
								}
							}

					if (!validIndex) {
						return null;
					}
					else {
						if (result == null) {
							throw new NaftahBugError(
														"""
														حدث خطأ غير متوقع أثناء تحميل العنصر المطلوب من القائمة المحددة.
														""");
						}

						target.add(result);
						return result;
					}
				}
			}

			/**
			 * Prints a list of lines to the terminal in a paginated format, displaying
			 * a fixed number of lines per page and prompting the user to continue after each page.
			 * <p>
			 * The method reads user input between pages and supports quitting early. After displaying
			 * each page of {@code PAGE_SIZE} lines, it prompts the user with:
			 * <pre>
			 * [اضغط Enter للمتابعة، أو أدخل 'q' أو 'quit' أو 'خروج' لإنهاء التصفح والعودة إلى البرنامج الرئيسي]
			 * </pre>
			 * If the user enters any of the following commands (case-insensitive):
			 * <ul>
			 * <li>{@code q}</li>
			 * <li>{@code quit}</li>
			 * <li>{@code خروج} (Arabic for "exit")</li>
			 * </ul>
			 * the pagination is stopped, and the method exits early.
			 *
			 * @param lines the list of lines to display, paginated
			 */
			private void printPaginated(List<String> lines) {
				int total = getTotal(lines);
				int printedLines = 0;
				int currentIndex = 0;

				outerLoop:
				while (currentIndex < total) {
					for (int i = 0; i < PAGE_SIZE && currentIndex < total; i++, currentIndex++) {
						var current = loadClassOrFunction(currentIndex, lines);
						if (Objects.isNull(current)) {
							break outerLoop;
						}
						var currentLines = current.lines().toList();
						var currentLinesCount = currentLines.size();
						int terminalHeight = Integer.getInteger(TERMINAL_HEIGHT_PROPERTY) - 1;
						if ((printedLines % terminalHeight) + currentLinesCount >= terminalHeight) {
							for (String currentLine : currentLines) {
								if ((printedLines % terminalHeight) + 1 >= terminalHeight) {
									if (shouldQuit(reader)) {
										break outerLoop;
									}
									else {
										padText(currentLine, true);
										printedLines++;
									}
								}
								else {
									padText(currentLine, true);
									printedLines++;
								}
							}
						}
						else {
							padText(current, true);
							printedLines += currentLinesCount;
						}
					}

					if (currentIndex < total && shouldQuit(reader)) {
						break;
					}
				}
			}

			/**
			 * Loads all available manual topics from the {@code manualDir} directory
			 * and returns them as a map of topic keys to their corresponding file paths.
			 * <p>
			 * Each file in the directory is assumed to follow a naming convention like:
			 * <pre>
			 * 001_topic-name.md
			 * </pre>
			 * The method:
			 * <ul>
			 * <li>Lists all files in the {@code manualDir} directory</li>
			 * <li>Strips the file extension</li>
			 * <li>Splits the name by underscore ({@code _})</li>
			 * <li>Skips the numeric prefix (e.g. {@code 001})</li>
			 * <li>Joins the remaining parts using hyphens ({@code -}) to form the topic key</li>
			 * </ul>
			 * For example, {@code 002_getting_started.md} becomes {@code getting-started}.
			 * The resulting map allows easy lookup of topics by normalized key.
			 *
			 * @return a map of topic keys (e.g., {@code getting-started}) to {@link Path} objects
			 *         representing their files
			 * @throws IOException if an I/O error occurs while accessing the {@code manualDir}
			 */
			private Map<String, Path> loadAvailableTopics() throws IOException {
				try (var list = Files.list(manualDir)) {
					return list
							.map(path -> {
								var topicKey = Arrays
										.stream(path
												.getFileName()
												.toString()
												.replaceFirst("[.][^.]+$", "")
												.split("_"))
										.skip(1)
										.collect(Collectors.joining("-"));

								return Map.entry(topicKey, path);
							}
							)
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				}
			}

			/**
			 * Displays the content of a manual topic in the terminal, formatted from Markdown.
			 * <p>
			 * This method retrieves the file path for the given topic from the {@code topics} map,
			 * reads its content, parses it using a Markdown parser (such as {@code commonmark}),
			 * and then prints the parsed output to the terminal in a readable format.
			 * <p>
			 * The output includes:
			 * <ul>
			 * <li>A header showing the topic name</li>
			 * <li>A visual separator</li>
			 * <li>The formatted content of the manual topic</li>
			 * </ul>
			 *
			 * @param topic the name of the manual topic to display
			 * @throws IOException if the file for the given topic cannot be read
			 */
			private void showManualTopic(String topic) throws IOException {
				int printedLines = 0;
				Path path = topics.get(topic);
				var topicContent = Files.readString(path);

				// Output to terminal
				padText("📖 الدليل: %s - %s"
						.formatted(
									ArabicUtils
											.transliterateToArabicScriptDefault(false, topic)[0],
									topic), true);
				padText("────────────────────────────────────────────", true);
				printedLines += 2;
				topicContent = getMarkdownAsString(topicContent);
				var currentLines = topicContent.lines().toList();
				int terminalHeight = Integer.getInteger(TERMINAL_HEIGHT_PROPERTY) - 1;
				for (String currentLine : currentLines) {
					if ((printedLines % terminalHeight) + 1 >= terminalHeight) {
						if (shouldQuit(reader)) {
							break;
						}
						else {
							padText(currentLine, true);
							printedLines++;
						}
					}
					else {
						padText(currentLine, true);
						printedLines++;
					}
				}
			}

			/**
			 * Loads detailed string representations of the provided classes and appends them
			 * to the given target list.
			 * <p>
			 * Each class is formatted using {@link ClassUtils#classToDetailedString(Class)} and prefixed with
			 * a visual separator and its associated key from the provided map.
			 * The final string for each class follows the structure:
			 *
			 * <pre>
			 * ---------------------------------------------------
			 * [Class Key]
			 * [Detailed Class Info]
			 * ---------------------------------------------------
			 * </pre>
			 *
			 * <p>This method is useful for debugging, documentation, or logging purposes
			 * where a full breakdown of multiple classes is required in an easily readable form.
			 *
			 * @param classes a map of string keys (e.g., class labels or names) to {@link Class} objects
			 * @param target  the list to which the formatted, detailed string representations will be added
			 */
			private void loadDetailedClasses(Map<String, Class<?>> classes, List<String> target) {
				classes
						.entrySet()
						.stream()
						.map(this::loadDetailedClass)
						.filter(Objects::nonNull)
						.forEach(target::add);
			}

			private String loadDetailedClass(Map.Entry<String, Class<?>> JvmFunction) {
				return """
						---------------------------------------------------
						%s
						%s
						---------------------------------------------------
						"""
						.formatted( JvmFunction.getKey(),
									classToDetailedString(JvmFunction.getValue()));
			}
		}


		/**
		 * The 'shell' subcommand that starts the interactive Naftah REPL.
		 */
		@Command(   name = ShellCommand.NAME,
					customSynopsis = "naftah shell [options] [filename] [args]",
					description = { """
									The Naftah shell command. it starts a REPL (Read-Eval-Print Loop), an interactive programming environment where you can enter single lines of naftah code.""",
									"""
									يبدأ أمر نفطة شال. يبدأ بيئة تفاعلية للبرمجة (REPL - قراءة-تقييم-طباعة)، حيث يمكنك إدخال أسطر مفردة من كود نفطح وتنفيذها فورًا."""
					},
					sortOptions = false)
		private static final class ShellCommand extends NaftahCommand {
			private static final String NAME = "shell";

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void run(Naftah main, boolean bootstrapAsync) throws Exception {
				System.setProperty(INSIDE_REPL_PROPERTY, Boolean.toString(true));
				super.run(main, bootstrapAsync);

				Terminal terminal = getTerminal();

				setupTerminalCapabilities(terminal);

				setupRefreshTerminalWidthAndHeight(terminal);

				LineReader reader = getLineReader(terminal);

				setupHistoryConfig(reader);

				setupKeyBindingsConfig(reader);

				StringBuilder fullLine = new StringBuilder();

				while (true) {
					try {
						String line = MULTILINE_IS_ACTIVE ?
								reader.readLine(null, RTL_MULTILINE_PROMPT, (MaskingCallback) null, null) :
								reader.readLine(null, RTL_PROMPT, (MaskingCallback) null, null);

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

						var result = doRun(parser, main.args);

						if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result) && !None.isNone(result)) {
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
						String currentLine = reader.getBuffer().atChar(reader.getBuffer().length() - 1) == '\n' ?
								reader.getBuffer().substring(0, reader.getBuffer().length() - 2) :
								reader.getBuffer().substring(0, reader.getBuffer().length() - 1);
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
			 * Checks for management commands like ':exit' or 'خروج:' and handles them accordingly.
			 *
			 * @param line the input line to check
			 */
			private void checkManagementCommands(String line) {
				/*
				 * TODO: add support for :reset : reset repl :list : list all valid code
				 * snippets :drop : drops a variable :vars : list variables :functions : list
				 * created functions :save : save the valid codes snippets into a file :history
				 * : shows full history :help : shows help of commands
				 */

				if (List.of(":exit", ":خروج").contains(line.trim())) {
					throw new UserInterruptException(line);
				}
			}
		}
	}
}
