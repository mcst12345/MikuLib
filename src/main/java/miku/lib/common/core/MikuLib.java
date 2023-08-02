package miku.lib.common.core;

import miku.lib.common.core.proxy.CommonProxy;
import miku.lib.common.util.ClassUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;


@Mod(
        modid = MikuLib.MODID,
        name = MikuLib.NAME,
        version = MikuLib.VERSION
)
public class MikuLib {
    public static final String MODID = "mikulib";
    public static final String NAME = "MikuLib";
    public static final String VERSION = "1.10";
    @SidedProxy(
            clientSide = "miku.lib.common.core.proxy.ClientProxy",
            serverSide = "miku.lib.common.core.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public MikuLib() throws IOException {
        if (ClassUtil.Init()) {
            MikuCore.FuckLaunchWrapper();
            try {
                StringBuilder LAUNCH = new StringBuilder();
                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (s.contains("=")) {
                        LAUNCH.append('"');
                        LAUNCH.append(s);
                        LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
                }

                LAUNCH.append("-cp ");
                for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
                    LAUNCH.append(path).append(":");
                }
                LAUNCH = new StringBuilder(LAUNCH.substring(0, LAUNCH.length() - 1));

                String USERNAME = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
                String UUID = RandomStringUtils.randomAlphanumeric(32).toLowerCase();

                LAUNCH.append(" net.minecraft.launchwrapper.Launch --tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker --tweakClass optifine.OptiFineForgeTweaker --username ").append(USERNAME);
                LAUNCH.append(" --version 1.12.2");
                LAUNCH.append(" --gameDir ").append(System.getProperty("user.dir"));
                LAUNCH.append(" --assetsDir ").append(System.getProperty("user.dir")).append("/assets");
                LAUNCH.append(" --assetIndex 1.12");
                LAUNCH.append(" --uuid ").append(UUID);
                LAUNCH.append("  --accessToken HatsuneMiku");
                LAUNCH.append(" --userType msa --versionType Forge --width 854 --height 480");
                String JAVA = System.getProperty("java.home");
                if (JAVA.endsWith("jre")) {
                    File jdk = new File(JAVA.substring(0, JAVA.length() - 3) + "bin/java");
                    if (jdk.exists()) {
                        LAUNCH.insert(0, JAVA.substring(0, JAVA.length() - 3) + "bin/java ");
                    } else {
                        LAUNCH.insert(0, JAVA + "/bin/java ");
                    }
                }
                String command = LAUNCH.toString().replace(",", "");
                Runtime.getRuntime().exec(command);
                System.out.println("MikuLib has completed its file injection.Now restarting the game.");
                while (true) {
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        Launch.FuckNative();
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
