package miku.lib.common.core;

import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.JarFucker;
import miku.lib.common.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
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

    public MikuCore() throws IOException, NoSuchFieldException {
        FuckLaunchWrapper();
        ClassUtil.Init();

        if (restart) return;

        System.out.println("Add MikuTransformer");

        System.out.println("Init mixins");

        try {
            MixinBootstrap.init();
            //Add Mixin configs.
            Mixins.addConfiguration("mixins.minecraft.json");
            Mixins.addConfiguration("mixins.forge.json");
        } catch (Throwable e) {
            if (isLaunchFucked()) {
                System.out.println("The fuck? MikuLib can't apply mixins.");
            }
        }

        Field transformers = Launch.classLoader.getClass().getDeclaredField("transformers");
        transformers.setAccessible(true);
        long tmp = Launch.UNSAFE.objectFieldOffset(transformers);
        List<IClassTransformer> t = (List<IClassTransformer>) Launch.UNSAFE.getObject(Launch.classLoader, tmp);
        if (!(t instanceof MikuArrayListForTransformer)) {
            MikuArrayListForTransformer<IClassTransformer> fucked = new MikuArrayListForTransformer<IClassTransformer>(2);
            for (IClassTransformer i : t) fucked.add(i);
            System.out.println("Fucking LaunchClassLoader.");
            Launch.UNSAFE.putObjectVolatile(Launch.classLoader, tmp, fucked);//Fuck other transformers.
        }

        System.out.println(Launch.classLoader.getClass().toString());
    }

    protected synchronized static boolean isLaunchFucked() {
        try {
            Field miku = Launch.class.getDeclaredField("Miku");
            miku.setAccessible(true);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public synchronized static void FuckLaunchWrapper() {
        if (isLaunchFucked()) return;
        try {

            FileUtils.copyFile(new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"), new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.backup"));

            InputStream MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
            assert MikuLaunch != null;
            FileUtils.copyInputStreamToFile(MikuLaunch, new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"));
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
