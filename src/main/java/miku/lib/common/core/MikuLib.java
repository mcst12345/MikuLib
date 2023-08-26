package miku.lib.common.core;

import miku.lib.common.core.proxy.CommonProxy;
import miku.lib.common.util.MikuEventBus;
import miku.lib.common.util.Misc;
import miku.lib.common.util.Platform;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.security.CodeSource;

import static miku.lib.common.core.MikuCore.Client;


@Mod(
        modid = MikuLib.MODID,
        name = MikuLib.NAME,
        version = MikuLib.VERSION,
        acceptedMinecraftVersions = "1.12.2"
)
public class MikuLib {
    public static boolean isLAIN() {
        return LAIN;
    }

    public static void setLAIN() {
        MikuLib.LAIN = true;
    }

    private static boolean LAIN;
    public static final String MODID = "mikulib";
    public static final String NAME = "MikuLib";
    public static final String VERSION = "1.11.3";
    @SidedProxy(
            clientSide = "miku.lib.common.core.proxy.ClientProxy",
            serverSide = "miku.lib.common.core.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    static final boolean win = Platform.isWindows();
    static final boolean Linux = Platform.isLinux();
    static final boolean MacOS = Platform.isMac();
    static final boolean BSD = Platform.isFreeBSD() || Platform.isNetBSD() || Platform.isOpenBSD() || Platform.iskFreeBSD();
    static final boolean Android = Platform.isAndroid();

    private static final EventBus MikuEventBus = new MikuEventBus();

    public static EventBus MikuEventBus() {
        return MikuEventBus;
    }

    public static final boolean MikuModInstalled;

    static {
        boolean temp;
        try {
            Class<?> Miku = Class.forName("miku.Miku.Miku");
            temp = true;
        } catch (ClassNotFoundException e) {
            temp = false;
        }
        MikuModInstalled = temp;
    }

    public MikuLib() {
        boolean flag = MikuCore.isLaunchFucked();
        System.out.println("LaunchWrapperFucked:" + flag);
        if (!flag) {
            MikuCore.FuckLaunchWrapper();
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

    @Mod.Instance
    public static MikuLib INSTANCE;

    protected Logger log;//I never use this.I prefer System.out.println.That's better for stacktracing.


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        this.log = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
