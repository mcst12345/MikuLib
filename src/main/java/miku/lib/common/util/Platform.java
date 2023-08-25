/*
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package miku.lib.common.util;

//Well, this file is copied from jna. We don't have jna on server-side,so I created this.

/**
 * Provide simplified platform information.
 */
public final class Platform {
    public static final int UNSPECIFIED = -1;
    public static final int MAC = 0;
    public static final int LINUX = 1;
    public static final int WINDOWS = 2;
    public static final int SOLARIS = 3;
    public static final int FREEBSD = 4;
    public static final int OPENBSD = 5;
    public static final int WINDOWSCE = 6;
    public static final int AIX = 7;
    public static final int ANDROID = 8;
    public static final int GNU = 9;
    public static final int KFREEBSD = 10;
    public static final int NETBSD = 11;

    /**
     * Whether read-only (final) fields within Structures are supported.
     */
    public static final boolean RO_FIELDS;
    /**
     * Whether this platform provides NIO Buffers.
     */
    public static final boolean HAS_BUFFERS;
    /**
     * Whether this platform provides the AWT Component class; also false if
     * running headless.
     */
    public static final boolean HAS_AWT;
    /**
     * Whether this platform supports the JAWT library.
     */
    public static final boolean HAS_JAWT;
    /**
     * Canonical name of this platform's math library.
     */
    public static final String MATH_LIBRARY_NAME;
    /**
     * Canonical name of this platform's C runtime library.
     */
    public static final String C_LIBRARY_NAME;
    /**
     * Whether in-DLL callbacks are supported.
     */
    public static final boolean HAS_DLL_CALLBACKS;

    private static final int osType;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            if ("dalvik".equalsIgnoreCase(System.getProperty("java.vm.name"))) {
                osType = ANDROID;
                // Native libraries on android must be bundled with the APK
                System.setProperty("jna.nounpack", "true");
            } else {
                osType = LINUX;
            }
        } else if (osName.startsWith("AIX")) {
            osType = AIX;
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            osType = MAC;
        } else if (osName.startsWith("Windows CE")) {
            osType = WINDOWSCE;
        } else if (osName.startsWith("Windows")) {
            osType = WINDOWS;
        } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            osType = SOLARIS;
        } else if (osName.startsWith("FreeBSD")) {
            osType = FREEBSD;
        } else if (osName.startsWith("OpenBSD")) {
            osType = OPENBSD;
        } else if (osName.equalsIgnoreCase("gnu")) {
            osType = GNU;
        } else if (osName.equalsIgnoreCase("gnu/kfreebsd")) {
            osType = KFREEBSD;
        } else if (osName.equalsIgnoreCase("netbsd")) {
            osType = NETBSD;
        } else {
            osType = UNSPECIFIED;
        }
        boolean hasBuffers = false;
        try {
            Class.forName("java.nio.Buffer");
            hasBuffers = true;
        } catch (ClassNotFoundException ignored) {
        }
        // NOTE: we used to do Class.forName("java.awt.Component"), but that
        // has the unintended side effect of actually loading AWT native libs,
        // which can be problematic
        HAS_AWT = osType != WINDOWSCE && osType != ANDROID && osType != AIX;
        HAS_JAWT = HAS_AWT && osType != MAC;
        HAS_BUFFERS = hasBuffers;
        RO_FIELDS = osType != WINDOWSCE;
        C_LIBRARY_NAME = osType == WINDOWS ? "msvcrt" : osType == WINDOWSCE ? "coredll" : "c";
        MATH_LIBRARY_NAME = osType == WINDOWS ? "msvcrt" : osType == WINDOWSCE ? "coredll" : "m";
        HAS_DLL_CALLBACKS = osType == WINDOWS;
    }

    private Platform() {
    }

    public static int getOSType() {
        return osType;
    }

    public static boolean isMac() {
        return osType == MAC;
    }

    public static boolean isAndroid() {
        return osType == ANDROID;
    }

    public static boolean isLinux() {
        return osType == LINUX;
    }

    public static boolean isAIX() {
        return osType == AIX;
    }

    /**
     * @deprecated
     */
    public static boolean isAix() {
        return isAIX();
    }

    public static boolean isWindowsCE() {
        return osType == WINDOWSCE;
    }

    /**
     * Returns true for any windows variant.
     */
    public static boolean isWindows() {
        return osType == WINDOWS || osType == WINDOWSCE;
    }

    public static boolean isSolaris() {
        return osType == SOLARIS;
    }

    public static boolean isFreeBSD() {
        return osType == FREEBSD;
    }

    public static boolean isOpenBSD() {
        return osType == OPENBSD;
    }

    public static boolean isNetBSD() {
        return osType == NETBSD;
    }

    public static boolean isGNU() {
        return osType == GNU;
    }

    public static boolean iskFreeBSD() {
        return osType == KFREEBSD;
    }

    public static boolean isX11() {
        // TODO: check filesystem for /usr/X11 or some other X11-specific test
        return !Platform.isWindows() && !Platform.isMac();
    }

    public static boolean hasRuntimeExec() {
        return !isWindowsCE() || !"J9".equals(System.getProperty("java.vm.name"));
    }

    static String getCanonicalArchitecture(String arch, boolean softfloat) {
        arch = arch.toLowerCase().trim();
        switch (arch) {
            case "powerpc":
                arch = "ppc";
                break;
            case "powerpc64":
                arch = "ppc64";
                break;
            case "i386":
            case "i686":
                arch = "x86";
                break;
            case "x86_64":
            case "amd64":
                arch = "x86-64";
                break;
        }
        // Work around OpenJDK mis-reporting os.arch
        // https://bugs.openjdk.java.net/browse/JDK-8073139
        if ("ppc64".equals(arch) && "little".equals(System.getProperty("sun.cpu.endian"))) {
            arch = "ppc64le";
        }
        // Map arm to armel if the binary is running as softfloat build
        if ("arm".equals(arch) && softfloat) {
            arch = "armel";
        }


        return arch;
    }

    /**
     * Generate a canonical String prefix based on the given OS
     * type/arch/name.
     *
     * @param osType from {@link #getOSType()}
     * @param arch   from <code>os.arch</code> System property
     * @param name   from <code>os.name</code> System property
     */

    static String getNativeLibraryResourcePrefix(int osType, String arch, String name, boolean isSoftfloat) {
        String osPrefix;
        arch = getCanonicalArchitecture(arch, isSoftfloat);
        switch (osType) {
            case Platform.ANDROID:
                if (arch.startsWith("arm")) {
                    arch = "arm";
                }
                osPrefix = "android-" + arch;
                break;
            case Platform.WINDOWS:
                osPrefix = "win32-" + arch;
                break;
            case Platform.WINDOWSCE:
                osPrefix = "w32ce-" + arch;
                break;
            case Platform.MAC:
                osPrefix = "darwin";
                break;
            case Platform.LINUX:
                osPrefix = "linux-" + arch;
                break;
            case Platform.SOLARIS:
                osPrefix = "sunos-" + arch;
                break;
            case Platform.FREEBSD:
                osPrefix = "freebsd-" + arch;
                break;
            case Platform.OPENBSD:
                osPrefix = "openbsd-" + arch;
                break;
            case Platform.NETBSD:
                osPrefix = "netbsd-" + arch;
                break;
            case Platform.KFREEBSD:
                osPrefix = "kfreebsd-" + arch;
                break;
            default:
                osPrefix = name.toLowerCase();
                int space = osPrefix.indexOf(" ");
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space);
                }
                osPrefix += "-" + arch;
                break;
        }
        return osPrefix;
    }
}