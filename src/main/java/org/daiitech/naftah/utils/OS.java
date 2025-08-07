package org.daiitech.naftah.utils;

import java.io.File;
import java.util.Locale;

import org.daiitech.naftah.errors.NaftahBugError;

public final class OS {
  public static final String OS_NAME_PROPERTY = "os.name";
  public static final String OS_ARCH_PROPERTY = "os.arch";
  public static final String OS_VERSION_PROPERTY = "os.version";

  private static final String FAMILY_OS_400 = "os/400";
  private static final String FAMILY_Z_OS = "z/os";
  private static final String FAMILY_WIN9X = "win9x";
  private static final String FAMILY_OPENVMS = "openvms";
  private static final String FAMILY_UNIX = "unix";
  private static final String FAMILY_TANDEM = "tandem";
  private static final String FAMILY_MAC = "mac";
  private static final String FAMILY_DOS = "dos";
  private static final String FAMILY_NETWARE = "netware";
  private static final String FAMILY_OS_2 = "os/2";
  private static final String FAMILY_WINDOWS = "windows";
  private static final String OS_NAME;
  private static final String OS_ARCH;
  private static final String OS_VERSION;
  private static final String PATH_SEP;

  private OS() {}

  private static boolean isFamily(String family) {
    return isOs(family, null, null, null);
  }

  public static boolean isFamilyDOS() {
    return isFamily(FAMILY_DOS);
  }

  public static boolean isFamilyMac() {
    return isFamily(FAMILY_MAC);
  }

  public static boolean isFamilyNetware() {
    return isFamily(FAMILY_NETWARE);
  }

  public static boolean isFamilyOS2() {
    return isFamily(FAMILY_OS_2);
  }

  public static boolean isFamilyTandem() {
    return isFamily(FAMILY_TANDEM);
  }

  public static boolean isFamilyUnix() {
    return isFamily(FAMILY_UNIX);
  }

  public static boolean isFamilyWindows() {
    return isFamily(FAMILY_WINDOWS);
  }

  public static boolean isFamilyWin9x() {
    return isFamily(FAMILY_WIN9X);
  }

  public static boolean isFamilyZOS() {
    return isFamily(FAMILY_Z_OS);
  }

  public static boolean isFamilyOS400() {
    return isFamily(FAMILY_OS_400);
  }

  public static boolean isFamilyOpenVms() {
    return isFamily(FAMILY_OPENVMS);
  }

  public static boolean isName(String name) {
    return isOs(null, name, null, null);
  }

  public static boolean isArch(String arch) {
    return isOs(null, null, arch, null);
  }

  public static boolean isVersion(String version) {
    return isOs(null, null, null, version);
  }

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
        } else if (family.equals(FAMILY_OS_2)) {
          isFamily = OS_NAME.contains(FAMILY_OS_2);
        } else if (family.equals(FAMILY_NETWARE)) {
          isFamily = OS_NAME.contains(FAMILY_NETWARE);
        } else if (family.equals(FAMILY_DOS)) {
          isFamily = PATH_SEP.equals(";") && !isFamily(FAMILY_NETWARE);
        } else if (family.equals(FAMILY_MAC)) {
          isFamily = OS_NAME.contains(FAMILY_MAC);
        } else if (family.equals(FAMILY_TANDEM)) {
          isFamily = OS_NAME.contains("nonstop_kernel");
        } else if (family.equals(FAMILY_UNIX)) {
          isFamily =
              PATH_SEP.equals(":")
                  && !isFamily(FAMILY_OPENVMS)
                  && (!isFamily(FAMILY_MAC) || OS_NAME.endsWith("x"));
        } else if (family.equals(FAMILY_WIN9X)) {
          isFamily =
              isFamily(FAMILY_WINDOWS)
                  && (OS_NAME.contains("95")
                      || OS_NAME.contains("98")
                      || OS_NAME.contains("me")
                      || OS_NAME.contains("ce"));
        } else if (!family.equals(FAMILY_Z_OS)) {
          if (family.equals(FAMILY_OS_400)) {
            isFamily = OS_NAME.contains(FAMILY_OS_400);
          } else {
            if (!family.equals(FAMILY_OPENVMS)) {
              throw new NaftahBugError(
                  "لا يمكن تحديد عائلة نظام التشغيل \"%s\" بسبب عدم توفر المعلومات الكافية."
                      .formatted(family));
            }

            isFamily = OS_NAME.contains(FAMILY_OPENVMS);
          }
        } else {
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

  static {
    OS_NAME = System.getProperty(OS_NAME_PROPERTY).toLowerCase(Locale.US);
    OS_ARCH = System.getProperty(OS_ARCH_PROPERTY).toLowerCase(Locale.US);
    OS_VERSION = System.getProperty(OS_VERSION_PROPERTY).toLowerCase(Locale.US);
    PATH_SEP = File.pathSeparator;
  }
}
