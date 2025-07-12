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
    return isFamily("dos");
  }

  public static boolean isFamilyMac() {
    return isFamily("mac");
  }

  public static boolean isFamilyNetware() {
    return isFamily("netware");
  }

  public static boolean isFamilyOS2() {
    return isFamily("os/2");
  }

  public static boolean isFamilyTandem() {
    return isFamily("tandem");
  }

  public static boolean isFamilyUnix() {
    return isFamily("unix");
  }

  public static boolean isFamilyWindows() {
    return isFamily("windows");
  }

  public static boolean isFamilyWin9x() {
    return isFamily("win9x");
  }

  public static boolean isFamilyZOS() {
    return isFamily("z/os");
  }

  public static boolean isFamilyOS400() {
    return isFamily("os/400");
  }

  public static boolean isFamilyOpenVms() {
    return isFamily("openvms");
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
        if (family.equals("windows")) {
          isFamily = OS_NAME.contains("windows");
        } else if (family.equals("os/2")) {
          isFamily = OS_NAME.contains("os/2");
        } else if (family.equals("netware")) {
          isFamily = OS_NAME.contains("netware");
        } else if (family.equals("dos")) {
          isFamily = PATH_SEP.equals(";") && !isFamily("netware");
        } else if (family.equals("mac")) {
          isFamily = OS_NAME.contains("mac");
        } else if (family.equals("tandem")) {
          isFamily = OS_NAME.contains("nonstop_kernel");
        } else if (family.equals("unix")) {
          isFamily =
              PATH_SEP.equals(":")
                  && !isFamily("openvms")
                  && (!isFamily("mac") || OS_NAME.endsWith("x"));
        } else if (family.equals("win9x")) {
          isFamily =
              isFamily("windows")
                  && (OS_NAME.contains("95")
                      || OS_NAME.contains("98")
                      || OS_NAME.contains("me")
                      || OS_NAME.contains("ce"));
        } else if (!family.equals("z/os")) {
          if (family.equals("os/400")) {
            isFamily = OS_NAME.contains("os/400");
          } else {
            if (!family.equals("openvms")) {
              throw new NaftahBugError(
                  "لا يمكن تحديد عائلة نظام التشغيل \"%s\" بسبب عدم توفر المعلومات الكافية."
                      .formatted(family));
            }

            isFamily = OS_NAME.contains("openvms");
          }
        } else {
          isFamily = OS_NAME.contains("z/os") || OS_NAME.contains("os/390");
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
