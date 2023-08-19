package miku.lib.common.core;

import com.sun.jna.Platform;
import miku.lib.common.util.JarFucker;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import sun.misc.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MikuCore implements IFMLLoadingPlugin {
    public static boolean Client = true;
    private static final long crc1 = 1446508905, crc2 = 3329933059L;//Edit this value if LaunchWrapper is changed.

    private static Class<?> deduceMainApplicationClass() {
        Class<?> result = null;
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    result = Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return result;
    }

    static {
        l:
        for (File file : Objects.requireNonNull(new File(System.getProperty("user.home")).listFiles())) {
            if (!file.isDirectory()) if (file.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(file)) {
                    for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().equals("net/minecraftforge/fml/relauncher/ServerLaunchWrapper.class")) {
                            Client = false;
                            break l;
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    protected static boolean restart = false;
    protected static final List<String> InvalidMods = new ArrayList<>();

    static final boolean win = Platform.isWindows();
    static final boolean Linux = Platform.isLinux();
    static final boolean MacOS = Platform.isMac();
    static final boolean BSD = Platform.isFreeBSD() || Platform.isNetBSD() || Platform.isOpenBSD() || Platform.iskFreeBSD();
    static final boolean Android = Platform.isAndroid();
    public MikuCore() {

        FuckLaunchWrapper();
        //FuckForge();

        if (win) {
            System.out.println("Holy fuck,MikuLib is running on Windows! This is not recommended! Use GNU/Linux instead if possible.");
        }
        if (Linux) {
            System.out.println("MikuLib is running on Linux. Weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee!");
        }
        if (MacOS) {
            System.out.println("MikuLib has never been tested on MacOS. Good luck.");
        }
        if (BSD) {
            System.out.println("MikuLib has never been tested on BSD. Good luck.");
        }
        if (Android) {
            System.out.println("The FUCK? You are running MikuLib on Android?");
        }

        if (restart) {
            try {
                StringBuilder LAUNCH = new StringBuilder();
                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (s.contains("=")) {
                        if (!win) LAUNCH.append('"');
                        LAUNCH.append(s);
                        if (!win) LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
                }

                if (!Client) {
                    CodeSource cs = deduceMainApplicationClass().getProtectionDomain().getCodeSource();
                    String file = cs.getLocation().toURI().getSchemeSpecificPart();

                    int lastIndex = file.lastIndexOf(".jar");
                    file = file.substring(5, lastIndex + 4);

                    System.out.println(file);

                    LAUNCH.insert(0, "-jar " + file + " ");
                }

                if (!Client) LAUNCH.insert(0, "-Dcatserver.skipCheckLibraries=true ");

                if (Client) {
                    LAUNCH.append("-cp ");
                    for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
                        if (!win) LAUNCH.append(path).append(":");
                        else LAUNCH.append(path).append(";");
                    }
                    LAUNCH = new StringBuilder(LAUNCH.substring(0, LAUNCH.length() - 1));

                    String USERNAME = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
                    String UUID = RandomStringUtils.randomAlphanumeric(32).toLowerCase();
                    LAUNCH.append(" net.minecraft.launchwrapper.Launch --tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker --username ").append(USERNAME);
                    LAUNCH.append(" --version 1.12.2");
                    LAUNCH.append(" --gameDir ").append(System.getProperty("user.dir"));
                    LAUNCH.append(" --assetsDir ").append(System.getProperty("user.dir")).append("/assets");
                    LAUNCH.append(" --assetIndex 1.12");
                    LAUNCH.append(" --uuid ").append(UUID);
                    LAUNCH.append("  --accessToken HatsuneMiku");
                    LAUNCH.append(" --userType msa --versionType Forge --width 854 --height 480");
                }
                String JAVA = System.getProperty("java.home");
                System.out.println("java.home:" + JAVA);
                if (JAVA.endsWith("jre")) {
                    String JavaHome = JAVA.substring(0, JAVA.length() - 3);
                    File jdk = new File(JavaHome + "bin/java");
                    if (jdk.exists()) {
                        String tmp = JavaHome + "bin/java ";
                        if (win) {
                            tmp = tmp.trim();
                            tmp = tmp.replace("\\", "\\\\").replace("/", "\\\\");
                            tmp = "\"" + tmp;
                            tmp = tmp + ".exe\"";
                        }
                        LAUNCH.insert(0, tmp + " ");
                    } else {
                        String tmp = JAVA + "/bin/java ";
                        if (win) {
                            tmp = tmp.trim();
                            tmp = tmp.replace("\\", "\\\\").replace("/", "\\\\");
                            tmp = "\"" + tmp;
                            tmp = tmp + ".exe\"";
                        }
                        LAUNCH.insert(0, tmp + " ");
                    }
                }


                String command = LAUNCH.toString().replace(",", "");
                if (Client) System.out.println("MikuLib has completed its file injection.Now restarting the game.");
                else System.out.println("MikuLib has completed its file injection.Now restarting the server.");
                System.out.println("Command:\n" + command);
                if (win) {
                    ProcessBuilder process = new ProcessBuilder("cmd /c " + command);
                    process.redirectErrorStream(true);
                    Process mc = process.start();
                    BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                    String line;
                    while ((line = inStreamReader.readLine()) != null) {
                        System.out.println(line);
                    }

                } else {
                    Process mc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command}, null, null);
                    InputStream is = mc.getInputStream();
                    String line;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    mc.waitFor();
                    is.close();
                    reader.close();
                }

                /*
                  Prevent the original game process from running
                 */
                while (true) {
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected synchronized static boolean isLaunchFucked() {
        try {
            File file1 = new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar");
            File file2 = new File(System.getProperty("user.dir") + "/libraries/launchwrapper-1.12.jar");
            long crc1 = 0, crc2 = 0;
            try {
                crc1 = FileUtils.checksumCRC32(file1);
            } catch (Throwable ignored) {
            }
            try {
                crc2 = FileUtils.checksumCRC32(file2);
            } catch (Throwable ignored) {
            }
            System.out.println(crc1);
            System.out.println(crc2);

            return (crc1 == 0 || crc1 == MikuCore.crc1 || crc1 == MikuCore.crc2) && (crc2 == 0 || crc2 == MikuCore.crc1 || crc2 == MikuCore.crc2);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized static void FuckLaunchWrapper() {
        if (isLaunchFucked()) return;
        try {
            try {
                FileUtils.copyFile(new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"), new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.backup"));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                FileUtils.copyFile(new File(System.getProperty("user.dir") + "/libraries/launchwrapper-1.12.jar"), new File(System.getProperty("user.dir") + "/libraries/launchwrapper-1.12.jar.backup"));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            InputStream MikuLaunch;
            if (win) {
                MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked.win");
            } else MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
            InputStream MIXIN = MikuCore.class.getResourceAsStream("/mixin-0.8.5-SNAPSHOT.jar");
            assert MikuLaunch != null;
            FileUtils.copyInputStreamToFile(MikuLaunch, new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"));
            try {
                FileUtils.copyInputStreamToFile(MikuLaunch, new File(System.getProperty("user.dir") + "/libraries/launchwrapper-1.12.jar"));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            assert MIXIN != null;
            FileUtils.copyInputStreamToFile(MIXIN, new File(System.getProperty("user.dir") + "/libraries/mixin.jar"));
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            File dic = new File(System.getProperty("user.dir"));
            if (dic.isDirectory()) {
                for (File file : Objects.requireNonNull(dic.listFiles())) {
                    if (file.getName().endsWith(".jar")) {
                        boolean changed = false;
                        try (JarFile jar = new JarFile(file)) {
                            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
                            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                                JarEntry entry = entries.nextElement();
                                try (InputStream is = jar.getInputStream(entry)) {
                                    if (entry.getName().equals("libraries.info")) {
                                        changed = true;
                                        //if (win) {
                                        //    try (InputStream fucked = MikuCore.class.getResourceAsStream("/libraries.info.win")) {
                                        //        jos.putNextEntry(new JarEntry(entry.getName()));
                                        //        jos.write(IOUtils.readNBytes(fucked, fucked.available()));
                                        //    }
                                        //} else
                                        //    try (InputStream fucked = MikuCore.class.getResourceAsStream("/libraries.info")) {
                                        //        jos.putNextEntry(new JarEntry(entry.getName()));
                                        //        jos.write(IOUtils.readNBytes(fucked, fucked.available()));
                                        //    }
                                    } else {
                                        jos.putNextEntry(new JarEntry(entry.getName()));
                                        jos.write(IOUtils.readNBytes(is, is.available()));
                                    }
                                }
                            }
                            jos.closeEntry();
                            jos.close();
                        }
                        if (changed) {
                            JarFucker.OverwriteFile(new File(file.getName() + ".fucked"), new File(file.getName()), true);
                        } else {
                            new File(file.getName() + ".fucked").delete();
                        }
                    }
                }
            } else System.out.println("The fuck?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        restart = true;
    }

    //The fuck. LaunchWrapper is also on the server side. I'm an idiot
    public synchronized static void FuckServerCore() {
        List<JarFile> servers = new ArrayList<>();
        for (File file : Objects.requireNonNull(new File(System.getProperty("user.home")).listFiles())) {
            if (!file.isDirectory()) if (file.getName().endsWith(".jar")) {
                try {
                    JarFile jar = new JarFile(file);
                    for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().equals("net/minecraftforge/fml/relauncher/ServerLaunchWrapper.class")) {
                            servers.add(jar);
                            break;
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }

        for (JarFile server : servers) {
            try {
                JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(server.getName() + ".fucked")));
                for (Enumeration<JarEntry> entries = server.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = entries.nextElement();
                    try (InputStream is = server.getInputStream(entry)) {
                        if (entry.getName().equals("net/minecraftforge/fml/relauncher/ServerLaunchWrapper.class")) {
                            //TODO
                        } else {
                            jos.putNextEntry(new JarEntry(entry.getName()));
                            jos.write(IOUtils.readNBytes(is, is.available()));
                        }
                    }
                }
                //TODO
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * I wrote this because I wanted to overwrite the CoreModManager to prevent coremods that are not in the whitelist to be loaded.
     * Otherwise, some coremod like Annihilation will cause the game to crash.
     * But soon I found that I could throw an exception in my transformer to prevent the coremod's class from being loaded.
     * This method may be removed or completed in the future.
     */
    public synchronized static void FuckForge() {
        System.out.println("Fucking Forge.");
        if (ForgeFucked()) return;
        String forge = null;
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (path.contains("net/minecraftforge/forge")) forge = path;
        }
        if (forge == null) {
            throw new RuntimeException("The fuck?I can't find your forge file!");
        }
        try {
            JarFile jar = new JarFile(forge);
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                try (InputStream is = jar.getInputStream(entry)) {
                    System.out.println(entry.getName());
                    if (entry.getName().equals("net/minecraftforge/fml/relauncher/CoreModManager$FMLPluginWrapper.class")) {
                        //TODO
                    }
                    if (entry.getName().equals("net/minecraftforge/fml/relauncher/CoreModManager.class")) {
                        //TODO
                    }
                }
            }
            jos.closeEntry();
            jos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //restart = true;
    }

    public synchronized static boolean ForgeFucked() {
        try {
            Field field = CoreModManager.class.getDeclaredField("Miku1");
            field.setAccessible(true);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static void AddInvalidMod(String mod) {
        if (mod.equals("mikulib") || mod.equals("miku")) return;
        InvalidMods.add(mod);
    }

    public static boolean isModInvalid(String mod) {
        if (mod.equals("mikulib") || mod.equals("miku")) return false;
        return InvalidMods.contains(mod);
    }
}
