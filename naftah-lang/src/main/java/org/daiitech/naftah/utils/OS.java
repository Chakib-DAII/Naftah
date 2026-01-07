package org.daiitech.naftah.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.daiitech.naftah.errors.NaftahBugError;

import static org.daiitech.naftah.errors.ExceptionUtils.newNaftahBugInvalidUsageError;
import static org.daiitech.naftah.utils.reflect.ClassUtils.QUALIFIED_NAME_SEPARATOR;

/**
 * Utility class for detecting the current operating system's properties,
 * including name, architecture, version, and system family.
 * <p>
 * Provides a set of static methods for querying the current environment,
 * particularly useful for platform-specific behavior.
 * </p>
 *
 * <p>This class is not instantiable.</p>
 *
 * <p><strong>Examples:</strong></p>
 * <pre>{@code
 * if (OS.isFamilyWindows()) {
 *     // Windows-specific logic
 * }
 * }</pre>
 *
 * <p>
 * Internally uses {@code System.getProperty} and lowercase matching.
 * </p>
 *
 * @author Chakib Daii
 */
public final class OS {
	/**
	 * System property key for the operating system name.
	 */
	public static final String OS_NAME_PROPERTY = "os.name";

	/**
	 * System property key for the operating system architecture.
	 */
	public static final String OS_ARCH_PROPERTY = "os.arch";

	/**
	 * System property key for the operating system version.
	 */
	public static final String OS_VERSION_PROPERTY = "os.version";

	/**
	 * WSL environment variable containing the current Linux distribution name.
	 */
	public static final String WSL_DISTRO_NAME_ENV = "WSL_DISTRO_NAME";

	/**
	 * WSL environment variable used for Windows–Linux process interop.
	 */
	public static final String WSL_INTEROP_ENV = "WSL_INTEROP";

	/**
	 * WSL-specific environment variable controlling Windows–Linux env propagation.
	 */
	public static final String WSL_ENV = "WSLENV";

	/**
	 * Identifier for OS/400 family.
	 */
	private static final String FAMILY_OS_400 = "os/400";

	/**
	 * Identifier for z/OS family.
	 */
	private static final String FAMILY_Z_OS = "z/os";

	/**
	 * Identifier for Windows 9x family (legacy Windows).
	 */
	private static final String FAMILY_WIN9X = "win9x";

	/**
	 * Identifier for OpenVMS family.
	 */
	private static final String FAMILY_OPENVMS = "openvms";

	/**
	 * Identifier for Unix family.
	 */
	private static final String FAMILY_UNIX = "unix";

	/**
	 * Identifier for Tandem family.
	 */
	private static final String FAMILY_TANDEM = "tandem";

	/**
	 * Identifier for Mac family.
	 */
	private static final String FAMILY_MAC = "mac";

	/**
	 * Identifier for DOS family.
	 */
	private static final String FAMILY_DOS = "dos";

	/**
	 * Identifier for NetWare family.
	 */
	private static final String FAMILY_NETWARE = "netware";

	/**
	 * Identifier for OS/2 family.
	 */
	private static final String FAMILY_OS_2 = "os/2";

	/**
	 * Identifier for Windows family.
	 */
	private static final String FAMILY_WINDOWS = "windows";
	/**
	 * Whether the current OS is running under Windows Subsystem for Linux (WSL).
	 */
	private static final boolean IS_WSL;

	/**
	 * The OS name in lowercase, retrieved from system properties.
	 */
	private static final String OS_NAME;

	/**
	 * The OS architecture in lowercase, retrieved from system properties.
	 */
	private static final String OS_ARCH;

	/**
	 * The OS version in lowercase, retrieved from system properties.
	 */
	private static final String OS_VERSION;

	/**
	 * The system path separator character(s).
	 */
	private static final String PATH_SEP;

	static {
		OS_NAME = System.getProperty(OS_NAME_PROPERTY).toLowerCase(Locale.US);
		OS_ARCH = System.getProperty(OS_ARCH_PROPERTY).toLowerCase(Locale.US);
		OS_VERSION = System.getProperty(OS_VERSION_PROPERTY).toLowerCase(Locale.US);
		PATH_SEP = File.pathSeparator;
		IS_WSL = checkIfInsideWSL();
	}

	/**
	 * Private constructor to prevent instantiation.
	 * Always throws a {@link NaftahBugError} when called.
	 */
	private OS() {
		throw newNaftahBugInvalidUsageError();
	}

	/**
	 * Detects whether the JVM is running inside Windows Subsystem for Linux (WSL).
	 */
	private static boolean checkIfInsideWSL() {
		// WSL is always Linux
		if (!isFamilyUnix()) {
			return false;
		}

		// Fast path: environment variables
		if (System.getenv().containsKey(WSL_DISTRO_NAME_ENV) || System.getenv().containsKey(WSL_INTEROP_ENV) || System
				.getenv()
				.containsKey(WSL_ENV)) {
			return true;
		}

		// Fallback: kernel version
		try {
			String version = Files.readString(Path.of("/proc/version")).toLowerCase(Locale.US);
			return version.contains("microsoft");
		}
		catch (IOException ignored) {
			return false;
		}
	}


	/**
	 * Checks if the current OS matches the specified family.
	 *
	 * @param family the OS family name to check
	 * @return true if current OS belongs to that family
	 */
	private static boolean isFamily(String family) {
		return isOs(family, null, null, null);
	}

	/**
	 * Checks if the current OS belongs to the DOS family.
	 *
	 * @return {@code true} if the OS is in the DOS family, {@code false} otherwise.
	 */
	public static boolean isFamilyDOS() {
		return isFamily(FAMILY_DOS);
	}

	/**
	 * Checks if the current OS belongs to the Mac family.
	 *
	 * @return {@code true} if the OS is in the Mac family, {@code false} otherwise.
	 */
	public static boolean isFamilyMac() {
		return isFamily(FAMILY_MAC);
	}

	/**
	 * Checks if the current OS belongs to the NetWare family.
	 *
	 * @return {@code true} if the OS is in the NetWare family, {@code false} otherwise.
	 */
	public static boolean isFamilyNetware() {
		return isFamily(FAMILY_NETWARE);
	}

	/**
	 * Checks if the current OS belongs to the OS/2 family.
	 *
	 * @return {@code true} if the OS is in the OS/2 family, {@code false} otherwise.
	 */
	public static boolean isFamilyOS2() {
		return isFamily(FAMILY_OS_2);
	}

	/**
	 * Checks if the current OS belongs to the Tandem family.
	 *
	 * @return {@code true} if the OS is in the Tandem family, {@code false} otherwise.
	 */
	public static boolean isFamilyTandem() {
		return isFamily(FAMILY_TANDEM);
	}

	/**
	 * Checks if the current OS belongs to the Unix family.
	 *
	 * @return {@code true} if the OS is in the Unix family, {@code false} otherwise.
	 */
	public static boolean isFamilyUnix() {
		return isFamily(FAMILY_UNIX);
	}

	/**
	 * Checks if the current OS belongs to the Windows family.
	 *
	 * @return {@code true} if the OS is in the Windows family, {@code false} otherwise.
	 */
	public static boolean isFamilyWindows() {
		return isFamily(FAMILY_WINDOWS);
	}

	/**
	 * Checks whether the current OS is running under Windows Subsystem for Linux (WSL).
	 *
	 * @return {@code true} if running in WSL, {@code false} otherwise
	 */
	public static boolean isWSL() {
		return IS_WSL;
	}

	/**
	 * Checks if the current OS belongs to the legacy Windows 9x family.
	 *
	 * @return {@code true} if the OS is in the Windows 9x family, {@code false} otherwise.
	 */
	public static boolean isFamilyWin9x() {
		return isFamily(FAMILY_WIN9X);
	}

	/**
	 * Checks if the current OS belongs to the z/OS family.
	 *
	 * @return {@code true} if the OS is in the z/OS family, {@code false} otherwise.
	 */
	public static boolean isFamilyZOS() {
		return isFamily(FAMILY_Z_OS);
	}

	/**
	 * Checks if the current OS belongs to the OS/400 family.
	 *
	 * @return {@code true} if the OS is in the OS/400 family, {@code false} otherwise.
	 */
	public static boolean isFamilyOS400() {
		return isFamily(FAMILY_OS_400);
	}

	/**
	 * Checks if the current OS belongs to the OpenVMS family.
	 *
	 * @return {@code true} if the OS is in the OpenVMS family, {@code false} otherwise.
	 */
	public static boolean isFamilyOpenVms() {
		return isFamily(FAMILY_OPENVMS);
	}

	/**
	 * Checks whether the current OS name matches the given name exactly.
	 *
	 * @param name the OS name to check
	 * @return true if matched
	 */
	public static boolean isName(String name) {
		return isOs(null, name, null, null);
	}

	/**
	 * Checks whether the current OS architecture matches the given arch exactly.
	 *
	 * @param arch the architecture string to match
	 * @return true if matched
	 */
	public static boolean isArch(String arch) {
		return isOs(null, null, arch, null);
	}

	/**
	 * Checks whether the current OS version matches the given version exactly.
	 *
	 * @param version the OS version to check
	 * @return true if matched
	 */
	public static boolean isVersion(String version) {
		return isOs(null, null, null, version);
	}

	/**
	 * Checks whether the current OS matches a combination of criteria.
	 * Any {@code null} parameter is ignored in the match.
	 *
	 * @param family  the OS family name (e.g., "windows")
	 * @param name    the OS name (e.g., "Windows 10")
	 * @param arch    the architecture (e.g., "x86_64")
	 * @param version the version (e.g., "10.0")
	 * @return true if all specified parameters match the current system
	 * @throws NaftahBugError if an unknown family is passed
	 */
	public static boolean isOs(String family, String name, String arch, String version) {
		boolean retValue = false;
		if (family != null || name != null || arch != null || version != null) {
			boolean isFamily = true;
			boolean isName = true;
			boolean isArch = true;
			boolean isVersion = true;

			if (family != null) {
				if (family.equals(FAMILY_WINDOWS)) {
					isFamily = OS_NAME.contains(FAMILY_WINDOWS);
				}
				else if (family.equals(FAMILY_OS_2)) {
					isFamily = OS_NAME.contains(FAMILY_OS_2);
				}
				else if (family.equals(FAMILY_NETWARE)) {
					isFamily = OS_NAME.contains(FAMILY_NETWARE);
				}
				else if (family.equals(FAMILY_DOS)) {
					isFamily = PATH_SEP.equals(";") && !isFamily(FAMILY_NETWARE);
				}
				else if (family.equals(FAMILY_MAC)) {
					isFamily = OS_NAME.contains(FAMILY_MAC);
				}
				else if (family.equals(FAMILY_TANDEM)) {
					isFamily = OS_NAME.contains("nonstop_kernel");
				}
				else if (family.equals(FAMILY_UNIX)) {
					isFamily = PATH_SEP.equals(QUALIFIED_NAME_SEPARATOR) && !isFamily(FAMILY_OPENVMS) && (!isFamily(
																													FAMILY_MAC) || OS_NAME
																															.endsWith("x"));
				}
				else if (family.equals(FAMILY_WIN9X)) {
					isFamily = isFamily(FAMILY_WINDOWS) && (OS_NAME.contains("95") || OS_NAME.contains("98") || OS_NAME
							.contains("me") || OS_NAME.contains("ce"));
				}
				else if (!family.equals(FAMILY_Z_OS)) {
					if (family.equals(FAMILY_OS_400)) {
						isFamily = OS_NAME.contains(FAMILY_OS_400);
					}
					else {
						if (!family.equals(FAMILY_OPENVMS)) {
							throw new NaftahBugError(
														"لا يمكن تحديد عائلة نظام التشغيل \"%s\" بسبب عدم توفر المعلومات الكافية."
																.formatted(family));
						}
						isFamily = OS_NAME.contains(FAMILY_OPENVMS);
					}
				}
				else {
					isFamily = OS_NAME.contains(FAMILY_Z_OS) || OS_NAME.contains("os/390");
				}
			}

			if (name != null) {
				isName = name.equals(OS_NAME);
			}

			if (arch != null) {
				isArch = arch.equals(OS_ARCH);
			}

			if (version != null) {
				isVersion = version.equals(OS_VERSION);
			}

			retValue = isFamily && isName && isArch && isVersion;
		}

		return retValue;
	}
}
