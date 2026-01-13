package org.daiitech.naftah;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
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
import org.daiitech.naftah.builtin.lang.DeclaredVariable;
import org.daiitech.naftah.builtin.lang.JvmFunction;
import org.daiitech.naftah.builtin.lang.None;
import org.daiitech.naftah.builtin.utils.CollectionUtils;
import org.daiitech.naftah.builtin.utils.ObjectUtils;
import org.daiitech.naftah.builtin.utils.tuple.Pair;
import org.daiitech.naftah.errors.NaftahBugError;
import org.daiitech.naftah.parser.DefaultContext;
import org.daiitech.naftah.parser.NaftahErrorListener;
import org.daiitech.naftah.parser.REPLContext;
import org.daiitech.naftah.utils.OS;
import org.daiitech.naftah.utils.ResourceUtils;
import org.daiitech.naftah.utils.reflect.ClassUtils;
import org.daiitech.naftah.utils.reflect.type.JavaType;
import org.daiitech.naftah.utils.repl.REPLHelper;
import org.daiitech.naftah.utils.script.ScriptUtils;
import org.jline.reader.EOFError;
import org.jline.reader.EndOfFileException;
import org.jline.reader.History;
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
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_CALL_SEPARATOR;
import static org.daiitech.naftah.utils.reflect.ClassUtils.classToDetailedString;
import static org.daiitech.naftah.utils.reflect.RuntimeClassScanner.CLASS_PATH_PROPERTY;
import static org.daiitech.naftah.utils.repl.REPLHelper.CLOSING_MSG;
import static org.daiitech.naftah.utils.repl.REPLHelper.ESCAPE_CHARS_REGEX;
import static org.daiitech.naftah.utils.repl.REPLHelper.LAST_PRINTED;
import static org.daiitech.naftah.utils.repl.REPLHelper.MULTILINE_IS_ACTIVE;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_MULTILINE_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.RTL_PROMPT;
import static org.daiitech.naftah.utils.repl.REPLHelper.TEXT_PASTE_DETECTED;
import static org.daiitech.naftah.utils.repl.REPLHelper.clearScreen;
import static org.daiitech.naftah.utils.repl.REPLHelper.getLineReader;
import static org.daiitech.naftah.utils.repl.REPLHelper.getMarkdownAsString;
import static org.daiitech.naftah.utils.repl.REPLHelper.getTerminal;
import static org.daiitech.naftah.utils.repl.REPLHelper.println;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupHistoryConfig;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupKeyBindingsConfig;
import static org.daiitech.naftah.utils.repl.REPLHelper.setupTerminalCapabilities;
import static org.daiitech.naftah.utils.repl.REPLHelper.shouldQuit;
import static org.daiitech.naftah.utils.script.ScriptUtils.ARABIC_LOCALE;
import static org.daiitech.naftah.utils.script.ScriptUtils.containsArabicLetters;
import static org.daiitech.naftah.utils.script.ScriptUtils.padText;

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
	 * Property to include jvm functions and instantiatable classes in repl completions, disabled by default.
	 */
	public static final String INCLUDE_ALL_IN_COMPLETIONS_PROPERTY = "naftah.repl.includeAllInCompletions";
	/**
	 * Property to enable scanning the Jdk classes for Naftah types.
	 */
	public static final String SCAN_JDK_PROPERTY = "naftah.scanJDK";
	/**
	 * Cache results of classpath and JDK scanning.
	 */
	public static final String CACHE_SCANNING_RESULTS_PROPERTY = "naftah.cacheScanningResults";
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
	 * Property to enable number formatter in Naftah.
	 */
	public static final String NUMBER_FORMATTER_PROPERTY = "naftah.number.formatter.active";
	/**
	 * Property to enable Arabic-Indic digit formatting in Naftah.
	 */
	public static final String ARABIC_INDIC_PROPERTY = "naftah.number.arabicIndic.active";
	/**
	 * Property to enable caching of multiline text processing in Naftah.
	 */
	public static final String MULTILINE_CACHE_PROPERTY = "naftah.cache.multiline.active";
	/**
	 * Property to enable chunks of long words in text processing in Naftah.
	 */
	public static final String WORD_CHUNK_PROPERTY = "naftah.word.chunk.active";
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
	 * System property enabling or disabling deep reflection for Java object mapping.
	 */
	public static final String JAVA_OBJECT_REFLECT_ACTIVE_PROPERTY = "naftah.javaObject.deepReflect.active";
	/**
	 * System property specifying the maximum depth allowed during deep reflection.
	 */
	public static final String JAVA_OBJECT_REFLECT_MAX_DEPTH_PROPERTY = "naftah.javaObject.deepReflect.maxDepth";
	/**
	 * System property controlling whether null fields are skipped during reflection.
	 */
	public static final String JAVA_OBJECT_REFLECT_SKIP_NULLS_PROPERTY = "naftah.javaObject.deepReflect.skipNulls";
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
	 * Constant representing a single underscore character ("_").
	 */
	public static final String UNDERSCORE = "_";

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
	 * Blocks until the user presses Ctrl+C, using the REPL terminal and line reader.
	 *
	 * <p>This is a convenient wrapper around {@link REPLHelper#waitForUserInterruption(LineReader)},
	 * automatically obtaining the terminal and reader.</p>
	 *
	 * <p>All other input is ignored; the user sees a friendly Arabic message and can exit gracefully.</p>
	 */
	private static void waitForUserInterruption() {
		try {
			Terminal terminal = REPLHelper.getTerminal();
			LineReader reader = getLineReader(terminal);
			REPLHelper.waitForUserInterruption(reader);
		}
		catch (Throwable ignored) {
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

		boolean shouldWaitForUserInterruption = OS.isRealXTerm();

		try {
			if (Boolean.getBoolean(DEBUG_PROPERTY)) {
				Thread.sleep(5000);
			}

			ParseResult result = parser.parseArgs(args);

			if (printHelpIfRequested(result)) {
				return;
			}

			if (ObjectUtils.isEmpty(result.subcommands())) {
				throw new InitializationException("خطأ: لم يتم تقديم أمر (run/shell/init/man)");
			}

			var matchedSubCommandResult = result.subcommands().get(result.subcommands().size() - 1);
			var matchedCommand = (NaftahCommand) matchedSubCommandResult.commandSpec().userObject();

			shouldWaitForUserInterruption = shouldWaitForUserInterruption && (matchedCommand instanceof NaftahCommand.InitCommand || matchedCommand instanceof NaftahCommand.RunCommand);

			// If we fail, then exit with an error so scripting frameworks can catch it.
			if (!naftahCommand.process(matchedSubCommandResult, matchedCommand)) {
				if (shouldWaitForUserInterruption) {
					waitForUserInterruption();
				}
				System.exit(1);
			}
		}
		catch (ParameterException ex) { // command line arguments could not be parsed
			printPaddedErrorMessageToString(ex);
			ex.getCommandLine().usage(System.err);
		}
		catch (Exception e) {
			printPaddedErrorMessageToString(e);
		}
		finally {
			if (shouldWaitForUserInterruption) {
				waitForUserInterruption();
			}
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
	 * The error message is formatted in Arabic script as "تم التقاط الخطأ: 'message'".
	 *
	 * @param t the throwable whose error message will be printed with padding
	 */
	public static void printPaddedErrorMessageToString(Throwable t) {
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
		boolean bootstrapAsync = !(naftahCommand instanceof NaftahCommand.InitCommand || naftahCommand instanceof NaftahCommand.ManualCommand);
		try {
			naftahCommand.run(this, bootstrapAsync);
			return true;
		}
		catch (ParseCancellationException e) {
			return false;
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
								Naftah Version (إصدار نفطه): %s
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
				description = {"The Naftah command line processor.", "معالج الأوامر الخاص بـلغة البرمجة نفطه"},
				sortOptions = false,
				versionProvider = VersionProvider.class)
	private static class NaftahCommand {
		/**
		 * The main command name.
		 */
		private static final String NAME = "naftah";
		@Unmatched
		final List<String> arguments = new ArrayList<>();

		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
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
		final List<String> enabledCaches = new ArrayList<>();

		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
		@Option(names = {"-D", "--define"},
				paramLabel = "<property=value>",
				description = { "Define a system property",
								"تعريف خاصية نظام"})
		private final Map<String, String> systemProperties = new LinkedHashMap<>();

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
								"حدد ما إذا كان يجب إعادة استخدام فئات المسار (classpath) كأنواع في نفطه."})
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

		@Option(names = {"-nr_f", "--number_formatting"},
				description = {
								"""
								Use numerals and formatting symbols (e.g., arabic decimal separator, Ascii digit shapes).
								""",
								"استخدام الأرقام ورموز التنسيق (مثل الفاصلة العشرية العربية وأشكال الأرقام المستخدمة من قبل التونسيين (أسكي))."
				})
		private boolean useNumberFormatter;

		@Option(names = {"-ar_ind", "--arabic_indic"},
				description = {
								"Display numbers using Arabic-Indic digits (٠١٢٣٤٥٦٧٨٩)",
								"عرض الأرقام باستخدام الأرقام الهندية-العربية (٠١٢٣٤٥٦٧٨٩)"
				})
		private boolean useArabicIndic;

		@Option(names = {"-load_clf", "--load_classes_and_functions"},
				description = {
								"Load additional classes and functions at startup for manual",
								"تحميل فئات ودوال إضافية عند تشغيل البرنامج"
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
			System.setProperty(SCAN_JDK_PROPERTY, Boolean.toString(true));
			System.setProperty(CACHE_SCANNING_RESULTS_PROPERTY, Boolean.toString(true));
			if (Objects.isNull(System.getProperty(WORD_CHUNK_PROPERTY))) {
				System.setProperty(WORD_CHUNK_PROPERTY, Boolean.toString(true));
			}
			initConfig();
			bootstrap(bootstrapAsync);
		}

		/**
		 * Processes the parsed command line arguments and configures the environment.
		 *
		 * @param parseResult    the parsed command line result
		 * @param matchedCommand the matched command
		 * @return true if processing succeeded; false otherwise
		 * @throws ParameterException if the command line is invalid
		 */
		private boolean process(ParseResult parseResult, NaftahCommand matchedCommand) throws ParameterException {
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

			if (matchedCommand.useNumberFormatter) {
				System.setProperty(NUMBER_FORMATTER_PROPERTY, Boolean.toString(true));
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
									"أمر تشغيل نفطه. يقوم بتشغيل مفسر اللغة (يُفسر سكربت بلغة نفطه)."},
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
									أمر بدء نفطه. يقوم بتحضير فئات مسار فئات جافا (Java classpath) ومعالجتها لإعادة استخدامها داخل سكربت نفطه."""},
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
		 * The {@code 'man'} subcommand for Naftah.
		 *
		 * <p>This command loads and displays documentation topics related to Naftah usage.
		 * It provides help pages for classes, accessible classes, instantiable classes,
		 * built-in functions, JVM functions, and other runtime information.</p>
		 *
		 * <p>The command supports both English and Arabic aliases for flexibility in multi-language environments.</p>
		 *
		 * <p><strong>Usage:</strong></p>
		 * <pre>{@code
		 * naftah man [options] [filename] [args]
		 * }</pre>
		 *
		 * <p>The {@code ManualCommand} maintains lists of:</p>
		 * <ul>
		 * <li>{@link #classes} – all available classes</li>
		 * <li>{@link #accessibleClasses} – classes accessible from the current context</li>
		 * <li>{@link #instantiableClasses} – classes that can be instantiated</li>
		 * <li>{@link #builtinFunctions} – built-in Naftah functions</li>
		 * <li>{@link #jvmFunctions} – Java/JVM functions available for use</li>
		 * </ul>
		 *
		 * <p>It also keeps track of filtered results in {@link #filteredClassesOrFunctions}
		 * and uses a {@link LineReader} for interactive input, if needed.</p>
		 */
		@Command(   name = ManualCommand.NAME,
					customSynopsis = "naftah man [options] [filename] [args]",
					description = {
									"""
									The Naftah manual command. It loads and displays documentation topics related to Naftah usage.""",
									"""
									أمر 'man' في نفطه. يعرض صفحات المساعدة والمواضيع الخاصة باستخدام نفطه."""
					},
					sortOptions = false)
		private static final class ManualCommand extends NaftahCommand {
			/**
			 * Represents different targets for the manual command.
			 */
			private enum Target {
				CLASSES, ACCESSIBLE_CLASSES, INSTANTIABLE_CLASSES, BUILTIN_FUNCTIONS, JVM_FUNCTIONS,
			}

			/**
			 * Enum representing all manual subcommands supported in Naftah.
			 *
			 * <p>Each subcommand may have multiple aliases, supporting both English and Arabic inputs.
			 * The subcommands can be matched against user input using {@link #matches(String)}.</p>
			 *
			 * <p>Common manual subcommands include:</p>
			 * <ul>
			 * <li>{@link #USAGE} – display general usage/help instructions</li>
			 * <li>{@link #LIST_TOPICS} – list all available documentation topics</li>
			 * <li>{@link #CLASSES} – list all Java classes</li>
			 * <li>{@link #ACCESSIBLE_CLASSES} – list accessible Java classes</li>
			 * <li>{@link #INSTANTIABLE_CLASSES} – list Java classes that can be instantiated</li>
			 * <li>{@link #BUILTIN_FUNCTIONS} – list built-in Naftah functions</li>
			 * <li>{@link #JVM_FUNCTIONS} – list Java (JVM) functions</li>
			 * <li>{@link #EXIT} – exit the manual or REPL session</li>
			 * <li>{@link #HISTORY}, {@link #CURRENT_SESSION_HISTORY}, {@link #SANITIZE_HISTORY}, {@link #PURGE_HISTORY}
			 * – manage command history (view, sanitize, or purge)</li>
			 * </ul>
			 *
			 * <p>Usage example:</p>
			 * <pre>
			 * if (ManCommand.USAGE.matches(input)) {
			 * // display help instructions
			 * }
			 * </pre>
			 */
			private enum ManCommand {
				USAGE(Set.of("usage", "مساعدة")),
				LIST_TOPICS(Set.of("list", "المواضيع")),
				CLASSES(Set.of("classes", "الأصناف")),
				ACCESSIBLE_CLASSES(Set.of("accessible-classes", "الأصناف-المتاحة")),
				INSTANTIABLE_CLASSES(Set
						.of("instantiable-classes", "الأصناف-القابلة-للصنع", "الأصناف-القابلة-للتهيئة")),
				BUILTIN_FUNCTIONS(Set.of("builtin-functions", "الدوال-المدمجة")),
				JVM_FUNCTIONS(Set.of("jvm-functions", "دوال-جافا")),
				EXIT(Set.of("exit", "خروج")),
				HISTORY(Set.of("history", "الأوامر-المحفوظة")),
				CURRENT_SESSION_HISTORY(Set.of("current-session-history", "الأوامر-المحفوظة-الحالية")),
				SANITIZE_HISTORY(Set.of("sanitize-history", "تنظيف-الأوامر-المحفوظة")),
				PURGE_HISTORY(Set.of("purge-history", "مسح-الأوامر-المحفوظة"));

				private final Set<String> aliases;

				/**
				 * Constructs a manual command with the given set of aliases.
				 *
				 * @param aliases the set of aliases for this command (English and Arabic)
				 */
				ManCommand(Set<String> aliases) {
					this.aliases = aliases;
				}

				/**
				 * Checks if the input string matches any alias of this manual command.
				 *
				 * @param input the input string to check
				 * @return {@code true} if the input matches any alias, {@code false} otherwise
				 */
				boolean matches(String input) {
					String command = input.trim().toLowerCase(ARABIC_LOCALE);
					return aliases.contains(command);
				}

				/**
				 * Returns the set of aliases for this manual command.
				 *
				 * @return the set of aliases
				 */
				Set<String> getAliases() {
					return aliases;
				}

				/**
				 * Returns a set of all aliases for all manual commands.
				 *
				 * @return a {@link Set} containing all aliases
				 */
				static Set<String> getAllAliases() {
					Set<String> all = new HashSet<>();
					for (ManCommand cmd : ManCommand.values()) {
						all.addAll(cmd.getAliases());
					}
					return all;
				}
			}

			/** The name of this command. */
			private static final String NAME = "man";
			/** The start time when the command was initialized. */
			private static final Instant START_TIME = Instant.now();
			/** Marker value to skip processing certain entries. */
			private static final String SKIP = "SKIP";
			/** Number of items to display per page in paginated output. */
			private static final int PAGE_SIZE = 5;
			/** Path to the manual directory relative to the JAR. */
			private final Path manualDir = Paths.get(getJarDirectory().getParent() + "/manual");
			/** List of all classes known to the manual. */
			private final List<String> classes = new CopyOnWriteArrayList<>();
			/** List of classes accessible in the current context. */
			private final List<String> accessibleClasses = new CopyOnWriteArrayList<>();
			/** List of classes that can be instantiated. */
			private final List<String> instantiableClasses = new CopyOnWriteArrayList<>();
			/** List of all built-in functions available. */
			private final List<String> builtinFunctions = new CopyOnWriteArrayList<>();
			/** List of JVM/Java functions available. */
			private final List<String> jvmFunctions = new CopyOnWriteArrayList<>();
			/** Filtered results for classes or functions after applying user input. */
			private final List<String> filteredClassesOrFunctions = new CopyOnWriteArrayList<>();

			/** Interactive line reader for user input. */
			private LineReader reader;
			/** Mapping of topic names to their corresponding documentation paths. */
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

				setupHistoryConfig(reader, ".naftah/.naftah_man_history");

				setupKeyBindingsConfig(reader);

				clearScreen();

				History history = reader.getHistory();

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

										يمكنك استخدام اختصارات النسخ واللصق في هذه الواجهة:
										Alt+L
										نسخ آخر نص مطبوع إلى الحافظة
										Alt+V
										لصق محتوى الحافظة في محرر الإدخال لإعادة استخدامه

										استمتع بالتجربة وتعلم بسرعة!
										""", true);
							}
							line = reader.readLine(null, RTL_PROMPT, (MaskingCallback) null, null).trim();
						}

						if (line.isBlank()) {
							continue;
						}

						var matchedManagementCommand = checkManagementCommands(line, history);

						if (!matchedManagementCommand) {
							if (topics.containsKey(line)) {
								showManualTopic(line);
							}
							else {
								String qualifiedNameOrBuiltinFunction = null;
								String[] lineParts;
								if (!containsArabicLetters(line) && line.contains(".")) {
									if ((lineParts = line.split(QUALIFIED_CALL_SEPARATOR)).length == 2) {
										qualifiedNameOrBuiltinFunction = ClassUtils
												.getQualifiedCall(ClassUtils
														.getQualifiedName(
																			lineParts[0]), lineParts[1]);

									}
									else if (lineParts.length == 1) {
										qualifiedNameOrBuiltinFunction = ClassUtils.getQualifiedName(line);
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
											qualifiedNameOrBuiltinFunction = builtinFunctions
													.get(0)
													.toDetailedString();
										}
										else {
											qualifiedNameOrBuiltinFunction = IntStream
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

								if (qualifiedNameOrBuiltinFunction != null) {
									LAST_PRINTED.set(qualifiedNameOrBuiltinFunction);
									padText(qualifiedNameOrBuiltinFunction, true);
									padText("""
											\n
											[استخدم Alt+L لنسخ آخر نص مطبوع إلى الحافظة، واستخدم Alt+V للصقه مرة أخرى في محرر الإدخال لإعادة استخدامه.]
											""",
											true);
								}
								else {
									padText("لم يتم العثور على دليل للموضوع.", true);
								}
							}
						}

						System.out.println();
					}
					catch (UserInterruptException | EndOfFileException e) {
						padText(CLOSING_MSG, true);
						break;
					}
					catch (Throwable t) {
						printPaddedErrorMessageToString(t);
					}
					finally {
						// Save history explicitly (though it's usually done automatically)
						history.save();
					}
				}
			}

			/**
			 * Extracts and normalizes the search text from a tokenized command line.
			 * <p>
			 * This method skips the first element of {@code commandParts} (the command keyword)
			 * and concatenates the remaining parts <strong>without any separator</strong>,
			 * then trims leading and trailing whitespace from the resulting string.
			 * </p>
			 *
			 * <p>
			 * For example:
			 * <pre>
			 * ["classes", "java", "util"] → "javautil"
			 * ["الأصناف", "نص", "بحث"] → "نصبحث"
			 * </pre>
			 * </p>
			 *
			 * @param commandParts the command line split into parts, where the first element
			 *                     represents the command keyword
			 * @return the concatenated search text derived from the remaining command parts,
			 *         or an empty string if no search text is provided
			 */
			private String getSearchTextFromCommand(String[] commandParts) {
				return Arrays
						.stream(commandParts)
						.skip(1)
						.collect(Collectors.joining())
						.trim();
			}

			/**
			 * Processes and executes management (meta) commands entered by the user in the interactive Naftah manual.
			 *
			 * <p>This method parses the given input line and determines if it corresponds to a supported
			 * management command. Supported commands allow the user to:</p>
			 * <ul>
			 * <li>Display usage/help instructions</li>
			 * <li>List available topics</li>
			 * <li>List Java classes, accessible classes, or instantiable classes</li>
			 * <li>List builtin (Naftah) functions or JVM functions</li>
			 * <li>Filter any of the above lists by a search text</li>
			 * <li>View or manage command history</li>
			 * <li>Exit the program</li>
			 * </ul>
			 *
			 * <p>Commands may be entered in either English or Arabic. Some commands optionally accept
			 * a search text argument, in which case only entries matching the search text are displayed.</p>
			 *
			 * <p>When a recognized command is matched, this method executes the corresponding action
			 * (e.g., printing paginated results, filtering lists, or updating the command history)
			 * and returns {@code true}. If the input does not match any known management command,
			 * no action is taken and {@code false} is returned.</p>
			 *
			 * <p><strong>Examples of supported commands:</strong></p>
			 * <ul>
			 * <li>{@code usage / مساعدة} – Display detailed instructions for the interactive manual</li>
			 * <li>{@code list / المواضيع} – List all available documentation topics</li>
			 * <li>{@code classes <search> / الأصناف <نص البحث>} – Show Java classes optionally filtered by a search
			 * term</li>
			 * <li>{@code accessible-classes / الأصناف-المتاحة} – Show accessible classes</li>
			 * <li>{@code instantiable-classes / الأصناف-القابلة-للتهيئة} – Show classes that can be instantiated</li>
			 * <li>{@code builtin-functions / الدوال-المدمجة} – List Naftah builtin functions</li>
			 * <li>{@code jvm-functions / دوال-جافا} – List available JVM/Java functions</li>
			 * <li>{@code history / الأوامر-المحفوظة} – Display all saved REPL commands</li>
			 * <li>{@code exit / خروج} – Exit the interactive session</li>
			 * </ul>
			 *
			 * <p>Commands that accept search text (e.g., classes or functions) will filter results
			 * and display them in a paginated format. The filtering uses the {@link #filteredClassesOrFunctions}
			 * list, and paginated output is handled by {@link #printPaginated(Target, List)}.</p>
			 *
			 * @param line    the raw input command line provided by the user
			 * @param history the REPL command history used for displaying, sanitizing, or purging entries
			 * @return {@code true} if the input matched a known management command and an action was executed;
			 *         {@code false} if the input did not match any known command
			 * @throws UserInterruptException if the input corresponds to an exit command
			 *                                (for example {@code "exit"} or {@code "خروج"}), signaling that
			 *                                the interactive session should be terminated
			 * @throws IOException            if an I/O error occurs during reading, filtering, or paginated output
			 * @see #printPaginated(Target, List)
			 * @see #filteredClassesOrFunctions
			 * @see ManualCommand.ManCommand
			 * @see ManualCommand.Target
			 */
			private boolean checkManagementCommands(String line, History history) throws IOException {
				var matched = false;
				String command = line.trim().toLowerCase(ARABIC_LOCALE);
				if (ManCommand.USAGE.matches(command)) {
					matched = true;
					padText(
							"""
							أوامر الواجهة التفاعلية للكتيبات التقنية لنفطه:

							- المواضيع أو list -> المواضيع المتوفرة.

							- <اسم الموضوع> -> فتح دليل الموضوع.

							- الأصناف أو classes -> الأصناف المتوفرة في Java مع أسمائها المؤهلة بالعربية.

							- الأصناف <نص البحث> أو classes <search text> -> الأصناف المتوفرة في Java المطابقة لنص البحث مع أسمائها المؤهلة بالعربية.

							- الأصناف-المتاحة أو accessible-classes -> الأصناف المتاحة في Java مع أسمائها المؤهلة بالعربية.

							- الأصناف-المتاحة <نص البحث> أو accessible-classes <search text> -> الأصناف المتاحة في Java المطابقة لنص البحث مع أسمائها المؤهلة بالعربية.

							- الأصناف-القابلة-للتهيئة أو الأصناف-القابلة-للصنع أو instantiable-classes -> الأصناف القابلة للتهيئة في Java مع أسمائها المؤهلة بالعربية.

							- الأصناف-القابلة-للتهيئة <نص البحث> أو الأصناف-القابلة-للصنع <نص البحث> أو instantiable-classes <search text> -> الأصناف القابلة للتهيئة في Java مع أسمائها المؤهلة بالعربية.

							- الدوال-المدمجة أو builtin-functions -> الدوال المدمجة في نظام نفطه.

							- الدوال-المدمجة <نص البحث> أو builtin-functions <search text> -> الدوال المدمجة في نظام نفطه المطابقة لنص البحث.

							- دوال-جافا أو jvm-functions -> دوال JVM المتوفرة مع استدعاءاتها المؤهلة بالعربية.

							- دوال-جافا <نص البحث> أو jvm-functions <search text> -> دوال JVM المتوفرة المطابقة لنص البحث مع استدعاءاتها المؤهلة بالعربية.

							- <الاسم المؤهل لصنف Java> -> تحويل الاسم إلى الصيغة العربية (نفطه).

							- history أو الأوامر-المحفوظة -> عرض كامل الأوامر المحفوظة في سجل المفسّر.

							- current-session-history أو الأوامر-المحفوظة-الحالية -> عرض أوامر الجلسة الحالية فقط.

							- sanitize-history أو تنظيف-الأوامر-المحفوظة -> تنظيف تاريخ الأوامر المحفوظة وحذف الإدخالات غير الصالحة.

							- purge-history أو مسح-السجل -> حذف جميع الأوامر من السجل وتنظيف الإدخالات غير الصالحة.

							- مساعدة أو usage -> عرض هذه التعليمات.

							- خروج أو exit -> إنهاء البرنامج.
							""",
							true);
				}
				else if (ManCommand.LIST_TOPICS.matches(command)) {
					matched = true;
					padText("المواضيع المتوفرة:", true);
					topics
							.keySet()
							.forEach(topic -> padText("\t\t- " + ScriptUtils
									.transliterateToArabicScriptDefault(topic)[0] + " - " + topic, true));
				}
				else if (ManCommand.EXIT.matches(command)) {
					throw new UserInterruptException(line);
				}
				else if (ManCommand.HISTORY.matches(command)) {
					matched = true;
					REPLHelper.printFullHistory(history, ManCommand.getAllAliases());
				}
				else if (ManCommand.CURRENT_SESSION_HISTORY.matches(command)) {
					matched = true;
					REPLHelper.printHistory(history, ManCommand.getAllAliases(), START_TIME);
				}
				else if (ManCommand.SANITIZE_HISTORY.matches(command)) {
					matched = true;
					REPLHelper.sanitizeHistory(history, ManCommand.getAllAliases());
				}
				else if (ManCommand.PURGE_HISTORY.matches(command)) {
					matched = true;
					history.purge();
				}
				else {
					String searchFormatter = "المطابقة لنص البحث %s";
					var commandParts = command.split("\\s");
					command = commandParts[0].trim();
					if (ManCommand.CLASSES.matches(command)) {
						matched = true;
						var target = Target.CLASSES;
						String baseMsg = "الأصناف المتوفرة في Java مع أسمائها المؤهلة بالعربية";
						if (commandParts.length > 1) {
							String searchText = getSearchTextFromCommand(commandParts);
							padText(baseMsg + searchFormatter.formatted(searchText) + ":", true);
							filteredClassesOrFunctions.clear();
							printPaginated(target, filteredClassesOrFunctions, searchText);
						}
						else {
							padText(baseMsg + ":", true);
							printPaginated(target, classes);
						}
					}
					else if (ManCommand.ACCESSIBLE_CLASSES.matches(command)) {
						matched = true;
						var target = Target.ACCESSIBLE_CLASSES;
						String baseMsg = "الأصناف المتاحة في Java مع أسمائها المؤهلة بالعربية";
						if (commandParts.length > 1) {
							String searchText = getSearchTextFromCommand(commandParts);
							padText(baseMsg + searchFormatter.formatted(searchText) + ":", true);
							filteredClassesOrFunctions.clear();
							printPaginated(target, filteredClassesOrFunctions, searchText);
						}
						else {
							padText(baseMsg + ":", true);
							printPaginated(target, accessibleClasses);
						}
					}
					else if (ManCommand.INSTANTIABLE_CLASSES.matches(command)) {
						matched = true;
						var target = Target.INSTANTIABLE_CLASSES;
						String baseMsg = "الأصناف القابلة للتهيئة في Java مع أسمائها المؤهلة بالعربية";
						if (commandParts.length > 1) {
							String searchText = getSearchTextFromCommand(commandParts);
							padText(baseMsg + searchFormatter.formatted(searchText) + ":", true);
							filteredClassesOrFunctions.clear();
							printPaginated(target, filteredClassesOrFunctions, searchText);
						}
						else {
							padText(baseMsg + ":", true);
							printPaginated(target, instantiableClasses);
						}
					}
					else if (ManCommand.BUILTIN_FUNCTIONS.matches(command)) {
						matched = true;
						var target = Target.BUILTIN_FUNCTIONS;
						String baseMsg = "الدوال المدمجة في نظام نفطه";
						if (commandParts.length > 1) {
							String searchText = getSearchTextFromCommand(commandParts);
							padText(baseMsg + searchFormatter.formatted(searchText) + ":", true);
							filteredClassesOrFunctions.clear();
							printPaginated(target, filteredClassesOrFunctions, searchText);
						}
						else {
							padText(baseMsg + ":", true);
							printPaginated(target, builtinFunctions);
						}
					}
					else if (ManCommand.JVM_FUNCTIONS.matches(command)) {
						matched = true;
						var target = Target.JVM_FUNCTIONS;
						String baseMsg = "دوال JVM المتوفرة مع استدعاءاتها المؤهلة بالعربية";
						if (commandParts.length > 1) {
							String searchText = getSearchTextFromCommand(commandParts);
							padText(baseMsg + searchFormatter.formatted(searchText) + ":", true);
							filteredClassesOrFunctions.clear();
							printPaginated(target, filteredClassesOrFunctions, searchText);
						}
						else {
							padText(baseMsg + ":", true);
							printPaginated(target, jvmFunctions);
						}
					}
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
			 * Computes the effective total number of elements for a given {@link Target}
			 * by comparing the provided total with the size of the corresponding
			 * collection in {@link DefaultContext}.
			 * <p>
			 * The returned value is always the maximum of:
			 * <ul>
			 * <li>the supplied {@code total} value (typically derived from a cached or
			 * partially populated list), and</li>
			 * <li>the size of the runtime collection associated with the specified
			 * {@code target}.</li>
			 * </ul>
			 * This ensures pagination and indexed access remain consistent even when
			 * the target list has not yet been fully populated.
			 * </p>
			 *
			 * @param target the target type whose backing context collection should be used
			 *               for comparison
			 * @param total  the current known total (for example, the size of a cached list)
			 * @return the maximum of {@code total} and the size of the corresponding
			 *         {@link DefaultContext} collection for the given target
			 */
			private int getTotal(Target target, int total) {
				if (Target.CLASSES.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getClasses()
											.size());
				}
				else if (Target.ACCESSIBLE_CLASSES.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getAccessibleClasses()
											.size());
				}
				else if (Target.INSTANTIABLE_CLASSES.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getInstantiableClasses()
											.size());
				}
				else if (Target.BUILTIN_FUNCTIONS.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getBuiltinFunctions()
											.size());
				}
				else if (Target.JVM_FUNCTIONS.equals(target)) {
					total = Math
							.max(   total,
									DefaultContext
											.getJvmFunctions()
											.size());
				}
				return total;
			}


			/**
			 * Resolves, loads, and formats the detailed representation of a class or function
			 * at the specified index for the given target category.
			 * <p>
			 * This method first attempts to return a previously loaded (cached) entry from
			 * {@code targetList}. If the requested index is not yet available in that list,
			 * it performs a lazy lookup against the corresponding collection in
			 * {@link DefaultContext}, based on the provided {@link Target}.
			 * </p>
			 *
			 * <p>
			 * Depending on the target type, the lookup is delegated to one of the following
			 * detail loaders:
			 * <ul>
			 * <li>{@code loadDetailedClass(Map.Entry)} for class-based targets</li>
			 * <li>{@code loadBuiltinFunction(Map.Entry)} for builtin functions</li>
			 * <li>{@code loadJvmFunction(Map.Entry)} for JVM functions</li>
			 * </ul>
			 * </p>
			 *
			 * <p>
			 * If a {@code classOrFunctionFilter} is provided, the resolved element must match
			 * the filter (by name or related metadata, depending on the target). If the
			 * element at the given index does not satisfy the filter, the lookup is considered
			 * invalid and {@code null} is returned.
			 * </p>
			 *
			 * <p>
			 * Successfully resolved elements are formatted, appended to {@code targetList}
			 * for caching purposes, and then returned.
			 * </p>
			 *
			 * @param index                 the zero-based index of the element to resolve
			 * @param target                the target category indicating which context
			 *                              collection should be queried
			 * @param targetList            the cache of already loaded and formatted entries
			 *                              for the given target
			 * @param classOrFunctionFilter an optional search filter used to restrict results;
			 *                              may be {@code null} or blank
			 * @return the formatted detailed representation of the resolved element,
			 *         or {@code null} if the index exists but does not satisfy the filter
			 * @throws NaftahBugError if the index is valid but the element cannot be resolved
			 *                        or formatted due to an unexpected internal error
			 */
			private String loadClassOrFunction( int index,
												Target target,
												List<String> targetList,
												String classOrFunctionFilter) {
				if (targetList.size() > index) {
					return targetList.get(index);
				}
				else {
					String result = null;
					boolean validIndex = true;
					boolean skip = false;
					if (Target.CLASSES.equals(target) && (validIndex = DefaultContext
							.getClasses()
							.size() > index)) {
						var element = CollectionUtils
								.getElementAt(
												DefaultContext
														.getClasses()
														.entrySet(),
												index);
						if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
							//noinspection unchecked
							var stringClassEntry = (Map.Entry<String, Class<?>>) entry;
							if ((classOrFunctionFilter == null || classOrFunctionFilter.isBlank()) || (stringClassEntry
									.getKey()
									.contains(classOrFunctionFilter) || stringClassEntry
											.getValue()
											.getName()
											.contains(classOrFunctionFilter))) {
								result = loadDetailedClass(stringClassEntry);
							}
							else {
								skip = true;
							}
						}
					}
					else if (Target.ACCESSIBLE_CLASSES.equals(target) && (validIndex = DefaultContext
							.getAccessibleClasses()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getAccessibleClasses()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									//noinspection unchecked
									var stringClassEntry = (Map.Entry<String, Class<?>>) entry;
									if ((classOrFunctionFilter == null || classOrFunctionFilter
											.isBlank()) || (stringClassEntry
													.getKey()
													.contains(classOrFunctionFilter) || stringClassEntry
															.getValue()
															.getName()
															.contains(classOrFunctionFilter))) {
										result = loadDetailedClass(stringClassEntry);
									}
									else {
										skip = true;
									}
								}
							}
					else if (Target.INSTANTIABLE_CLASSES.equals(target) && (validIndex = DefaultContext
							.getInstantiableClasses()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getInstantiableClasses()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									//noinspection unchecked
									var stringClassEntry = (Map.Entry<String, Class<?>>) entry;
									if ((classOrFunctionFilter == null || classOrFunctionFilter
											.isBlank()) || (stringClassEntry
													.getKey()
													.contains(classOrFunctionFilter) || stringClassEntry
															.getValue()
															.getName()
															.contains(classOrFunctionFilter))) {
										result = loadDetailedClass(stringClassEntry);
									}
									else {
										skip = true;
									}
								}
							}
					else if (Target.BUILTIN_FUNCTIONS.equals(target) && (validIndex = DefaultContext
							.getBuiltinFunctions()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getBuiltinFunctions()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									//noinspection unchecked
									var stringListEntry = (Map.Entry<String, List<BuiltinFunction>>) entry;
									if ((classOrFunctionFilter == null || classOrFunctionFilter
											.isBlank()) || stringListEntry.getKey().contains(classOrFunctionFilter)) {
										result = loadBuiltinFunction(stringListEntry);
									}
									else {
										skip = true;
									}
								}
							}
					else if (Target.JVM_FUNCTIONS.equals(target) && (validIndex = DefaultContext
							.getJvmFunctions()
							.size() > index)) {
								var element = CollectionUtils
										.getElementAt(
														DefaultContext
																.getJvmFunctions()
																.entrySet(),
														index);
								if (!None.isNone(element) && element instanceof Map.Entry<?, ?> entry) {
									//noinspection unchecked
									var stringListEntry = (Map.Entry<String, List<JvmFunction>>) entry;
									if ((classOrFunctionFilter == null || classOrFunctionFilter
											.isBlank()) || (stringListEntry
													.getKey()
													.contains(classOrFunctionFilter) || stringListEntry
															.getValue()
															.stream()
															.anyMatch(jvmFunction -> jvmFunction
																	.getClazz()
																	.getName()
																	.contains(classOrFunctionFilter) || jvmFunction
																			.getMethod()
																			.getName()
																			.contains(classOrFunctionFilter)))) {
										result = loadJvmFunction(stringListEntry);
									}
									else {
										skip = true;
									}
								}
							}

					if (!validIndex) {
						return null;
					}
					else {
						if (skip) {
							return SKIP;
						}
						if (result == null) {
							throw new NaftahBugError(
														"""
														حدث خطأ غير متوقع أثناء تحميل العنصر المطلوب من القائمة المحددة.
														""");
						}

						targetList.add(result);
						return result;
					}
				}
			}

			/**
			 * Prints elements of the specified target to the terminal in a paginated manner.
			 * <p>
			 * This is a convenience overload that delegates to
			 * {@link #printPaginated(Target, List, String)} without applying any search filter.
			 * </p>
			 *
			 * @param target the target category whose elements are being displayed
			 * @param lines  the cache of already loaded and formatted elements; additional
			 *               elements may be lazily loaded during pagination
			 */
			private void printPaginated(Target target, List<String> lines) {
				printPaginated(target, lines, null);
			}

			/**
			 * Prints elements of the specified target to the terminal in a paginated manner,
			 * with optional filtering applied during lazy loading.
			 * <p>
			 * Elements are resolved on demand using {@link #loadClassOrFunction(int, Target, List, String)}.
			 * Already loaded entries are reused from {@code lines}, while missing entries
			 * are fetched, formatted, and appended as needed.
			 * </p>
			 *
			 * <p>
			 * Output is constrained by both a logical page size ({@code PAGE_SIZE}) and the
			 * effective terminal height. Multi-line entries are split as necessary to avoid
			 * overflowing the terminal viewport.
			 * </p>
			 *
			 * <p>
			 * Between pages, and whenever the terminal height limit is reached, the user is
			 * prompted to continue browsing. Pagination can be terminated early if the user
			 * enters any quit command recognized by
			 * {@link org.daiitech.naftah.utils.repl.REPLHelper#shouldQuit(LineReader)}.
			 * </p>
			 *
			 * @param target                the target category indicating which context
			 *                              collection is being paginated
			 * @param lines                 the cache of already loaded and formatted elements;
			 *                              this list may be mutated during execution
			 * @param classOrFunctionFilter an optional search filter applied during element
			 *                              resolution; may be {@code null} or blank
			 */
			private void printPaginated(Target target, List<String> lines, String classOrFunctionFilter) {
				int total = getTotal(target, lines.size());
				int printedLines = 0;
				int currentIndex = 0;

				outerLoop:
				while (currentIndex < total) {
					for (int i = 0; i < PAGE_SIZE && currentIndex < total; i++, currentIndex++) {
						var current = loadClassOrFunction(currentIndex, target, lines, classOrFunctionFilter);
						if (Objects.isNull(current)) {
							break outerLoop;
						}
						if (SKIP.equals(current)) {
							i--;
							continue;
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
												.split(UNDERSCORE))
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
									ScriptUtils
											.transliterateToArabicScriptDefault(topic)[0],
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
		 * The 'shell' subcommand that starts the interactive Naftah REPL (Read-Eval-Print Loop).
		 *
		 * <p>This command launches an interactive programming environment where users can enter
		 * single lines of Naftah code, evaluate them immediately, and see the results. It provides
		 * a REPL interface for rapid experimentation, learning, and testing of Naftah scripts.</p>
		 *
		 * <p>The REPL supports both English and Arabic commands, allowing users to:</p>
		 * <ul>
		 * <li>Exit the session</li>
		 * <li>Display help instructions</li>
		 * <li>Reset the session</li>
		 * <li>List variables, functions, and implementations</li>
		 * <li>Inspect imports</li>
		 * <li>Save session state</li>
		 * <li>View, sanitize, or purge command history</li>
		 * </ul>
		 *
		 * <p>Example usage:</p>
		 * <pre>{@code
		 * naftah shell
		 * :help                 // display available REPL commands
		 * :vars                 // list defined variables
		 * :functions            // list defined functions
		 * :exit                 // terminate the REPL session
		 * }</pre>
		 *
		 * @see ShellCommand.ReplCommand
		 */
		@Command(   name = ShellCommand.NAME,
					customSynopsis = "naftah shell [options] [filename] [args]",
					description = { """
									The Naftah shell command. it starts a REPL (Read-Eval-Print Loop), an interactive programming environment where you can enter single lines of naftah code.""",
									"""
									يبدأ أمر نفطه شال. يبدأ بيئة تفاعلية للبرمجة (REPL - قراءة-تقييم-طباعة)، حيث يمكنك إدخال أسطر مفردة من كود نفطه وتنفيذها فورًا."""
					},
					sortOptions = false)
		private static final class ShellCommand extends NaftahCommand {
			/**
			 * Enum representing the REPL-specific commands supported in the Naftah interactive shell.
			 *
			 * <p>Each command may have multiple aliases, supporting both English and Arabic inputs.
			 * Commands can be matched against user input using {@link #matches(String)}.</p>
			 *
			 * <p>Common REPL commands include:</p>
			 * <ul>
			 * <li>{@link #EXIT} – terminate the REPL session</li>
			 * <li>{@link #HELP} – display help information</li>
			 * <li>{@link #RESET} – reset the REPL session</li>
			 * <li>{@link #VARS} – list variables</li>
			 * <li>{@link #FUNCTIONS} – list functions</li>
			 * <li>{@link #IMPLEMENTATIONS} – list implementations</li>
			 * <li>{@link #IMPORTS} – show imported modules</li>
			 * <li>{@link #SAVE} – save the current session state</li>
			 * <li>{@link #HISTORY}, {@link #CURRENT_SESSION_HISTORY}, {@link #SANITIZE_HISTORY}, {@link #PURGE_HISTORY}
			 * – manage command history</li>
			 * <li>{@link #DROP} – drop variables or definitions</li>
			 * </ul>
			 */
			private enum ReplCommand {
				EXIT(Set.of(":exit", ":خروج")),
				HELP(Set.of(":help", ":مساعدة")),
				RESET(Set.of(":reset", ":إعادة_ضبط")),
				VARS(Set.of(":vars", ":المتغيرات")),
				FUNCTIONS(Set.of(":functions", ":الدوال")),
				IMPLEMENTATIONS(Set.of(":implementations", ":السلوكيات")),
				IMPORTS(Set.of(":imports", ":الواردات")),
				SAVE(Set.of(":save", ":حفظ")),
				HISTORY(Set.of(":history", ":الأوامر_المحفوظة")),
				CURRENT_SESSION_HISTORY(Set.of(":current_session_history", ":الأوامر_المحفوظة_الحالية")),
				SANITIZE_HISTORY(Set.of(":sanitize_history", ":تنظيف_الأوامر_المحفوظة")),
				PURGE_HISTORY(Set.of(":purge_history", ":مسح_الأوامر_المحفوظة")),
				DROP(Set.of(":drop", ":حذف"));

				private final Set<String> aliases;

				/**
				 * Constructs a repl command with the given set of aliases.
				 *
				 * @param aliases the set of aliases for this command (English and Arabic)
				 */
				ReplCommand(Set<String> aliases) {
					this.aliases = aliases;
				}

				/**
				 * Checks if the input string matches any alias of this REPL command.
				 *
				 * @param input the user input to check
				 * @return {@code true} if the input matches one of the command aliases, {@code false} otherwise
				 */
				boolean matches(String input) {
					String command = input.trim().toLowerCase(ARABIC_LOCALE);
					return aliases.contains(command);
				}

				/**
				 * Returns all aliases for this REPL command.
				 *
				 * @return a set of aliases for this command
				 */
				Set<String> getAliases() {
					return aliases;
				}

				/**
				 * Returns a set of all aliases for all REPL commands.
				 *
				 * @return a set of all REPL command aliases
				 */
				static Set<String> getAllAliases() {
					Set<String> all = new HashSet<>();
					for (ReplCommand cmd : ReplCommand.values()) {
						all.addAll(cmd.getAliases());
					}
					return all;
				}
			}

			/** The name of the 'shell' subcommand and the REPL session start timestamp. */
			private static final String NAME = "shell";
			/** The timestamp marking when the REPL session was started. */
			private static final Instant START_TIME = Instant.now();

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

				setupHistoryConfig(reader, ".naftah/.naftah_history");

				setupKeyBindingsConfig(reader);

				clearScreen();


				padText("""
						مرحبًا بك في واجهة حلقة القراءة والتقييم والتنفيذ التفاعلي (REPL) لنفطه:

						يمكنك استخدام اختصارات النسخ واللصق في هذه الواجهة:
						Alt+L
						نسخ آخر نص مطبوع إلى الحافظة
						Alt+V
						لصق محتوى الحافظة في محرر الإدخال لإعادة استخدامه

						استمتع بالتجربة وتعلم بسرعة!
						""", true);

				History history = reader.getHistory();

				StringBuilder fullLine = new StringBuilder();
				String line;
				while (true) {
					try {
						try {
							line = MULTILINE_IS_ACTIVE ?
									reader.readLine(null, RTL_MULTILINE_PROMPT, (MaskingCallback) null, null) :
									reader.readLine(null, RTL_PROMPT, (MaskingCallback) null, null);
						}
						catch (IndexOutOfBoundsException ignored) {
							line = "";
						}

						line = line.replaceAll(ESCAPE_CHARS_REGEX, "");

						if (!MULTILINE_IS_ACTIVE && line.isBlank()) {
							continue;
						}

						var matchedManagementCommand = checkManagementCommands(line, history);

						if (matchedManagementCommand) {
							continue;
						}

						if (!line.isBlank()) {
							fullLine.append(line);
						}

						var input = getCharStream(false, fullLine.toString());

						if (MULTILINE_IS_ACTIVE) {
							String historyLine = fullLine
									.toString()
									.replace("\r\n", " ")
									.replace("\n", " ")
									.trim();

							history.add(historyLine);

							MULTILINE_IS_ACTIVE = false;
							TEXT_PASTE_DETECTED = false;
						}

						fullLine.delete(0, fullLine.length());

						var parser = prepareRun(input, NaftahErrorListener.INSTANCE);

						var result = doRun(parser, main.args);

						if (isSimpleOrBuiltinOrCollectionOrMapOfSimpleType(result) && !None.isNone(result)
						// not a declaration with flag
								&& !(result instanceof Pair<?, ?> pair && JavaType
										.of(pair)
										.getTypeParameters()
										.get(0)
										.isOfType(DeclaredVariable.class) && JavaType
												.of(pair)
												.getTypeParameters()
												.get(1)
												.isOfType(Boolean.class))
						) {
							var resultStr = getNaftahValueToString(result);
							LAST_PRINTED.set(resultStr);
							printPaddedToString(resultStr);
						}
						System.out.println();

					}
					catch (UserInterruptException | EndOfFileException e) {
						padText(CLOSING_MSG, true);
						break;
					}
					catch (EOFError ignored) {
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
						history.save();
					}
				}
			}

			/**
			 * Checks and executes REPL management commands entered by the user.
			 *
			 * <p>
			 * This method parses the provided input line and determines if it matches any
			 * recognized REPL management command. Supported commands allow the user to:
			 * </p>
			 *
			 * <ul>
			 * <li>Exit the REPL session (:exit or :خروج)</li>
			 * <li>Display the help menu (:help or :مساعدة)</li>
			 * <li>Reset the REPL session (:reset or :إعادة_ضبط)</li>
			 * <li>List defined variables (:vars or :المتغيرات)</li>
			 * <li>List defined functions (:functions or :الدوال)</li>
			 * <li>List defined implementations and their functions (:implementations or :السلوكيات)</li>
			 * <li>List imported elements (:imports or :الواردات)</li>
			 * <li>Save the current session to a snippet file (:save or :حفظ)</li>
			 * <li>View the full command history (:history or :الأوامر_المحفوظة)</li>
			 * <li>View current session command history (:current_session_history or :الأوامر_المحفوظة_الحالية)</li>
			 * <li>Sanitize command history (:sanitize_history or :تنظيف_الأوامر_المحفوظة)</li>
			 * <li>Purge the command history (:purge_history or :مسح_الأوامر_المحفوظة)</li>
			 * <li>Drop specific variables, functions, implementations, or imports (:drop)</li>
			 * </ul>
			 *
			 * <p>
			 * Commands may be entered in either English or Arabic. Some commands, like
			 * <code>:drop</code>, accept additional arguments to specify which elements
			 * to remove. When a recognized command is executed, this method performs the
			 * corresponding action (e.g., clearing variables, displaying lists, paginating output)
			 * and returns {@code true}. If the input does not match any known management command,
			 * {@code false} is returned.
			 * </p>
			 *
			 * @param line    the input line to check for management commands
			 * @param history the REPL session history used for history-related commands
			 * @return {@code true} if the input matches and executes a known REPL management command;
			 *         {@code false} otherwise
			 * @throws IOException            if an I/O error occurs while processing the command
			 * @throws UserInterruptException if the input corresponds to an exit command
			 *                                (for example ":exit" or ":خروج"), indicating
			 *                                that the user session should be terminated
			 */
			private boolean checkManagementCommands(String line, History history) throws IOException {
				var matched = false;
				String command = line.trim().toLowerCase(ARABIC_LOCALE);

				if (ReplCommand.EXIT.matches(command)) {
					throw new UserInterruptException(line);
				}
				else if (ReplCommand.HELP.matches(command)) {
					matched = true;
					padText(
							"""
							أوامر واجهة حلقة القراءة والتقييم والتنفيذ التفاعلي (REPL) لنفطه:

							- :vars أو :المتغيرات -> عرض جميع المتغيرات المعرّفة في الجلسة الحالية.

							- :functions أو :الدوال -> عرض جميع الدوال المعرّفة في الجلسة الحالية.

							- :implementations أو :السلوكيات -> عرض السلوكيات المعرّفة ودوال كل سلوك.

							- :imports أو :الواردات -> عرض جميع الواردات (imports) المستخدمة في الجلسة.

							- :drop أو :حذف -> حذف المتغيرات، الدوال، السلوكيات، أو الواردات المحددة.

							\t\t\t\t- :drop :vars أو :حذف :المتغيرات -> حذف المتغيرات المحددة (مثال: :حذف :المتغيرات أ,ب,ت)

							\t\t\t\t- :drop :functions أو :حذف :الدوال -> حذف الدوال المحددة (مثال: :حذف :الدوال د1,د2)

							\t\t\t\t- :drop :implementations أو :حذف :السلوكيات -> حذف السلوكيات المحددة (مثال: :حذف :السلوكيات س1,س2)

							\t\t\t\t- :drop :imports أو :حذف :الواردات -> حذف الواردات المحددة (مثال: :حذف :الواردات وارد)

							- :reset أو :إعادة_ضبط -> إعادة ضبط الجلسة الحالية (مسح المتغيرات، الدوال، السلوكيات، والواردات).

							- :history أو :الأوامر_المحفوظة -> عرض كامل الأوامر المحفوظة في سجل المفسّر.

							- :current_session_history أو :الأوامر_المحفوظة_الحالية -> عرض أوامر الجلسة الحالية فقط.

							- :sanitize_history أو :تنظيف_الأوامر_المحفوظة -> تنظيف تاريخ الأوامر المحفوظة وحذف الإدخالات غير الصالحة.

							- :purge_history أو :مسح_السجل -> حذف جميع الأوامر من السجل وتنظيف الإدخالات غير الصالحة.

							- :save أو :حفظ -> حفظ أوامر الجلسة الحالية في ملف مقتطف (snippet).

							- :help أو :مساعدة -> عرض هذه القائمة.

							- :exit أو :خروج -> إنهاء جلسة نفطه والخروج .
							""",
							true
					);
				}
				else if (ReplCommand.RESET.matches(command)) {
					matched = true;
					REPLContext.clear();
				}
				else if (ReplCommand.VARS.matches(command)) {
					matched = true;
					REPLContext
							.getVariables()
							.forEach(declaredVariable -> padText("   - " + declaredVariable.toString() + "\n", true));
				}
				else if (ReplCommand.FUNCTIONS.matches(command)) {
					matched = true;
					REPLContext
							.getFunctions()
							.forEach(declaredFunction -> padText("   - " + declaredFunction.toString() + "\n", true));
				}
				else if (ReplCommand.IMPLEMENTATIONS.matches(command)) {
					matched = true;
					REPLContext
							.getImplementations()
							.forEach(declaredImplementation -> {
								padText("   - " + declaredImplementation.toString() + "\n", true);
								declaredImplementation
										.getImplementationFunctions()
										.values()
										.forEach(declaredFunction -> padText("	   - " + declaredFunction
												.toString() + "\n", true));
							});
				}
				else if (ReplCommand.IMPORTS.matches(command)) {
					matched = true;
					REPLContext
							.getImports()
							.forEach(importElement -> padText("   - " + importElement + "\n", true));
				}
				else if (ReplCommand.SAVE.matches(command)) {
					matched = true;
					REPLHelper.saveHistorySnippet(history, START_TIME);
				}
				else if (ReplCommand.HISTORY.matches(command)) {
					matched = true;
					REPLHelper.printFullHistory(history, ReplCommand.getAllAliases());
				}
				else if (ReplCommand.CURRENT_SESSION_HISTORY.matches(command)) {
					matched = true;
					REPLHelper.printHistory(history, ReplCommand.getAllAliases(), START_TIME);
				}
				else if (ReplCommand.SANITIZE_HISTORY.matches(command)) {
					matched = true;
					REPLHelper.sanitizeHistory(history, ReplCommand.getAllAliases());
				}
				else if (ReplCommand.PURGE_HISTORY.matches(command)) {
					matched = true;
					history.purge();
				}
				else {
					var commandParts = command.replaceAll("\\s*[,٬؛،٫]\\s*", ",").split("\\s");
					command = commandParts[0].trim();

					if (ReplCommand.DROP.matches(command)) {
						if (commandParts.length == 3) {
							command = commandParts[1].trim();
							if (ReplCommand.VARS.matches(command)) {
								matched = true;
								String[] ids = commandParts[2].split(",");
								REPLContext.dropVariables(ids);
							}
							else if (ReplCommand.FUNCTIONS.matches(command)) {
								matched = true;
								String[] ids = commandParts[2].split(",");
								REPLContext.dropFunctions(ids);
							}
							else if (ReplCommand.IMPLEMENTATIONS.matches(command)) {
								matched = true;
								String[] ids = commandParts[2].split(",");
								REPLContext.dropImplementations(ids);
							}
							else if (ReplCommand.IMPORTS.matches(command)) {
								matched = true;
								String[] ids = commandParts[2].split(",");
								REPLContext.dropImports(ids);
							}
						}
					}
				}

				return matched;
			}
		}
	}
}
