package miku.lib.common.core;

import com.sun.jna.Platform;
import miku.lib.common.Native.NativeUtil;
import miku.lib.common.util.JarFucker;
import miku.lib.common.util.Md5Utils;
import miku.lib.common.util.Misc;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import sun.misc.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("MikuLibCore")
public class MikuCore implements IFMLLoadingPlugin {
    public static final boolean Client = System.getProperty("minecraft.client.jar") != null;
    private static final String md5;

    static {
        if (Platform.isWindows()) {
            try (InputStream is = MikuCore.class.getResourceAsStream("/launch.win.md5")) {
                assert is != null;
                byte[] dat = new byte[is.available()];
                is.read(dat);
                is.close();
                char[] text = new char[dat.length];
                for (int i = 0; i < dat.length; i++)
                    text[i] = (char) dat[i];
                md5 = String.copyValueOf(text);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream is = MikuCore.class.getResourceAsStream("/launch.md5")) {
                assert is != null;
                byte[] dat = new byte[is.available()];
                is.read(dat);
                is.close();
                char[] text = new char[dat.length];
                for (int i = 0; i < dat.length; i++)
                    text[i] = (char) dat[i];
                md5 = String.copyValueOf(text);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static boolean restart = false;
    protected static final List<String> InvalidMods = new ArrayList<>();

    static final boolean win = Platform.isWindows();
    static final boolean Linux = Platform.isLinux();
    static final boolean MacOS = Platform.isMac();
    static final boolean BSD = Platform.isFreeBSD() || Platform.isNetBSD() || Platform.isOpenBSD() || Platform.iskFreeBSD();
    static final boolean Android = Platform.isAndroid();

    public MikuCore() {


        FuckLaunchWrapper();
        if (!restart) {
            if (!win) {
                File f = new File("libJNI.so");
                System.load(f.getAbsolutePath());
            } else {
                File f = new File("libJNI.dll");
                System.load(f.getAbsolutePath());
            }
            System.out.println(NativeUtil.TEST());
        }

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
                    CodeSource cs = Misc.deduceMainApplicationClass().getProtectionDomain().getCodeSource();
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
            String md5_1 = null, md5_2 = null;
            try {
                md5_1 = Md5Utils.getFileMD5String(file1);
            } catch (Throwable ignored) {
            }
            try {
                md5_2 = Md5Utils.getFileMD5String(file2);
            } catch (Throwable ignored) {
            }
            System.out.println(md5_1);
            System.out.println(md5_2);

            return (md5_1 == null || md5_1.equals(MikuCore.md5)) && (md5_2 == null || md5_2.equals(MikuCore.md5));
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
                if (win) {
                    MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked.win");
                } else MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
                assert MikuLaunch != null;
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
                                    if (entry.getName().equals("libraries.info") || entry.getName().equals("mohist_libraries.txt")) {
                                        changed = true;
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
