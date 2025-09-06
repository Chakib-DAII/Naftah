package org.daiitech.naftah.utils;

import java.io.File;
import java.util.Locale;

import org.daiitech.naftah.errors.NaftahBugError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OSTests {
	private static String osName;
	private static String osArch;
	private static String osVersion;

	@BeforeAll
	static void backupSystemProperties() {
		osName = System.getProperty(OS.OS_NAME_PROPERTY).toLowerCase(Locale.US);
		osArch = System.getProperty(OS.OS_ARCH_PROPERTY).toLowerCase(Locale.US);
		osVersion = System.getProperty(OS.OS_VERSION_PROPERTY).toLowerCase(Locale.US);
	}

	@Nested
	class FamilyTests {
		@Test
		void isFamilyWindowsTest() {
			boolean familyWindows = OS.isFamilyWindows();
			if (osName.contains("windows")) {
				assertTrue(familyWindows);
			}
			else {
				assertFalse(familyWindows);
			}
		}

		@Test
		void isFamilyUnixTest() {
			boolean familyUnix = OS.isFamilyUnix();
			System.out.println(osName);
			System.out.println(osArch);
			System.out.println(osVersion);
			System.out.println(File.pathSeparator);
			System.out.println(familyUnix);
			if (osName.contains("unix")) {
				assertTrue(familyUnix);
			}
			else {
				assertFalse(familyUnix);
			}
		}

		@Test
		void isFamilyWin9xTest() {
			boolean familyWin9x = OS.isFamilyWin9x();
			if (osName.contains("win9x")) {
				assertTrue(familyWin9x);
			}
			else {
				assertFalse(familyWin9x);
			}
		}

		@Test
		void isFamilyDOSTest() {
			boolean familyDOS = OS.isFamilyDOS();
			if (osName.contains("dos") || osName.contains("windows")) {
				assertTrue(familyDOS);
			}
			else {
				assertFalse(familyDOS);
			}
		}

		@Test
		void isFamilyMacTest() {
			boolean familyMac = OS.isFamilyMac();
			if (osName.contains("mac")) {
				assertTrue(familyMac);
			}
			else {
				assertFalse(familyMac);
			}
		}

		@Test
		void isFamilyNetwareTest() {
			boolean familyNetware = OS.isFamilyNetware();
			if (osName.contains("netware")) {
				assertTrue(familyNetware);
			}
			else {
				assertFalse(familyNetware);
			}
		}

		@Test
		void isFamilyOS2Test() {
			boolean familyOS2 = OS.isFamilyOS2();
			if (osName.contains("os/2")) {
				assertTrue(familyOS2);
			}
			else {
				assertFalse(familyOS2);
			}
		}

		@Test
		void isFamilyTandemTest() {
			boolean familyTandem = OS.isFamilyTandem();
			if (osName.contains("tandem")) {
				assertTrue(familyTandem);
			}
			else {
				assertFalse(familyTandem);
			}
		}

		@Test
		void isFamilyZOSTest() {
			boolean familyZOS = OS.isFamilyZOS();
			if (osName.contains("z/os")) {
				assertTrue(familyZOS);
			}
			else {
				assertFalse(familyZOS);
			}
		}

		@Test
		void isFamilyOS400Test() {
			boolean familyOS400 = OS.isFamilyOS400();
			if (osName.contains("os/400")) {
				assertTrue(familyOS400);
			}
			else {
				assertFalse(familyOS400);
			}
		}

		@Test
		void isFamilyOpenVmsTest() {
			boolean familyOpenVms = OS.isFamilyOpenVms();
			if (osName.contains("openvms")) {
				assertTrue(familyOpenVms);
			}
			else {
				assertFalse(familyOpenVms);
			}
		}

		@Test
		void unsupportedFamilyThrowsTest() {
			NaftahBugError ex = assertThrows(NaftahBugError.class, () -> OS.isOs("nonexistent", null, null, null));
			assertTrue(ex.getMessage().contains("لا يمكن تحديد عائلة نظام التشغيل"));
		}
	}

	@Nested
	class NameArchVersionTests {
		@Test
		void isNameMatchesTest() {
			assertTrue(OS.isName(osName));
			assertFalse(OS.isName("invalidOS"));
		}

		@Test
		void isArchMatchesTest() {
			assertTrue(OS.isArch(osArch));
			assertFalse(OS.isArch("invalidOSArch"));
		}

		@Test
		void isVersionMatchesTest() {
			assertTrue(OS.isVersion(osVersion));
			assertFalse(OS.isVersion("x.x.x"));
		}

		@Test
		void isOsAllMatchTest() {
			assertTrue(OS.isOs(null, osName, osArch, osVersion));
			assertFalse(OS.isOs(null, "invalidName", "invalidOSArch", "x.x.x"));
		}
	}
}
