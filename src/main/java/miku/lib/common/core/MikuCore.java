package miku.lib.common.core;

import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.JarFucker;
import miku.lib.common.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MikuCore implements IFMLLoadingPlugin {
    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    protected static boolean restart = false;

    public MikuCore() throws IOException {
        FuckLaunchWrapper();
        ClassUtil.Init();

        if (restart || JarFucker.shouldRestart()) {
            try {
                StringBuilder LAUNCH = new StringBuilder(ManagementFactory.getRuntimeMXBean().getInputArguments().toString());
                LAUNCH = new StringBuilder(LAUNCH.substring(1, LAUNCH.length() - 1));
                for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
                    LAUNCH.insert(0, path + ":");
                }
                LAUNCH.insert(0, "-cp ");

                String USERNAME = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
                String UUID = RandomStringUtils.randomAlphanumeric(32).toLowerCase();

                System.out.println(System.getProperty("username"));
                LAUNCH.append(" net.minecraft.launchwrapper.Launch --tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker --tweakClass optifine.OptiFineForgeTweaker --username " + USERNAME);
                LAUNCH.append(" --version 1.12.2");
                LAUNCH.append(" --gameDir " + System.getProperty("user.dir"));
                LAUNCH.append(" --assetsDir " + System.getProperty("user.dir") + "/assets");
                LAUNCH.append(" --assetIndex 1.12");
                LAUNCH.append(" --uuid " + UUID);
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
                System.out.println(LAUNCH);
                System.out.println(System.getProperty("java.home"));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return;
        }

        System.out.println("Add MikuTransformer");

        System.out.println("Init mixins");

        Launch.Transformers.setAccessible(true);
        long tmp = Launch.UNSAFE.objectFieldOffset(Launch.Transformers);
        List<IClassTransformer> t = (List<IClassTransformer>) Launch.UNSAFE.getObject(Launch.classLoader, tmp);
        if (!(t instanceof MikuArrayListForTransformer)) {
            MikuArrayListForTransformer<IClassTransformer> fucked = new MikuArrayListForTransformer<IClassTransformer>(2);
            for (IClassTransformer i : t) fucked.add(i);
            System.out.println("Fucking LaunchClassLoader.");
            Launch.UNSAFE.putObjectVolatile(Launch.classLoader, tmp, fucked);//Fuck other transformers.
        }

        System.out.println(Launch.classLoader.getClass().toString());
    }

    public static void InitMixin(){
        try {
            //Add Mixin configs.
            Mixins.addConfiguration("mixins.minecraft.json");
            Mixins.addConfiguration("mixins.forge.json");
        } catch (Throwable e) {
            if (isLaunchFucked()) {
                System.out.println("The fuck? MikuLib can't apply mixins.");
            }
        }
    }
    protected synchronized static boolean isLaunchFucked() {
        try {
            Field miku = Launch.class.getDeclaredField("Miku");
            miku.setAccessible(true);
            return Launch.version.equals("1.0");
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public synchronized static void FuckLaunchWrapper() {
        if (isLaunchFucked()) return;
        try {

            FileUtils.copyFile(new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"), new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.backup"));

            InputStream MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
            InputStream MIXIN = MikuCore.class.getResourceAsStream("/mixin-0.8.5-SNAPSHOT.jar");
            assert MikuLaunch != null;
            FileUtils.copyInputStreamToFile(MikuLaunch, new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"));
            FileUtils.copyInputStreamToFile(MIXIN,new File(System.getProperty("user.dir")+"/libraries/mixin.jar"));
        } catch (IOException ignored) {
        }
        restart = true;
    }

    @Override
    public String[] getASMTransformerClass() {
        if (JarFucker.shouldRestart() || restart) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            try {
                Runtime.getRuntime().exec("kill -9 " + PID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Runtime.getRuntime().exit(-39);
        }
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        if (JarFucker.shouldRestart() || restart) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            try {
                Runtime.getRuntime().exec("kill -9 " + PID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        if (JarFucker.shouldRestart() || restart) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            try {
                Runtime.getRuntime().exec("kill -9 " + PID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if (JarFucker.shouldRestart() || restart) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            try {
                Runtime.getRuntime().exec("kill -9 " + PID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Runtime.getRuntime().exit(-39);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        if (JarFucker.shouldRestart() || restart) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            try {
                Runtime.getRuntime().exec("kill -9 " + PID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

}
