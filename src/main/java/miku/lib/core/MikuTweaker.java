package miku.lib.core;

import miku.lib.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class MikuTweaker implements ITweaker {
    public static Map<String, Class<?>> cachedClasses = null;

    public MikuTweaker() throws IOException, NoSuchFieldException, IllegalAccessException {
        InitSqlite();
        Field transformers = Launch.classLoader.getClass().getDeclaredField("transformers");
        transformers.setAccessible(true);
        List<IClassTransformer> t = (List<IClassTransformer>) transformers.get(Launch.classLoader);
        if (!(t instanceof MikuArrayListForTransformer)) {
            MikuArrayListForTransformer<IClassTransformer> fucked = new MikuArrayListForTransformer<IClassTransformer>(2);
            for (IClassTransformer i : t) fucked.add(i);
            transformers.set(Launch.classLoader, fucked);//Fuck other transformers.
        }
        Field cachedClasses = Launch.classLoader.getClass().getDeclaredField("cachedClasses");
        cachedClasses.setAccessible(true);
        MikuTweaker.cachedClasses = (Map<String, Class<?>>) cachedClasses.get(Launch.classLoader);
    }

    protected void InitSqlite() throws IOException {
        boolean flag = true;
        for (File file : Objects.requireNonNull(new File("mods").listFiles())) {
            if (file.getName().equals("MikuLib-SQlite-1.0.jar")) {//Check is the sqlite loader installed.
                flag = false;
                break;
            }
        }
        if (flag) {
            System.out.println("MikuLib's sqlite loader doesn't exists,extract it.");
            InputStream stream = MikuTweaker.class.getResourceAsStream("/MikuLib-SQlite-1.0.jar");
            assert stream != null;
            byte[] file = new byte[stream.available()];

            stream.read(file);
            stream.close();
            FileOutputStream outputStream = new FileOutputStream("mods/MikuLib-SQlite-1.0.jar");
            outputStream.write(file);//extracted the file.
            outputStream.close();
            System.out.println("MikuLib has just extracted the sqlite loader of it. Please restart the game.");
            FMLCommonHandler.instance().exitJava(0, true);
        }
    }

    private String[] args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        String[] additionArgs = {"--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDir.getAbsolutePath(), "--version", profile};
        List<String> fullArgs = new ArrayList<>();
        fullArgs.addAll(args);
        fullArgs.addAll(Arrays.asList(additionArgs));
        this.args = fullArgs.toArray(new String[fullArgs.size()]);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        System.out.println("Add MikuTransformer");

        //Add our transformer
        classLoader.registerTransformer("miku.lib.core.MikuTransformer");
        //classLoader.registerTransformer("miku.lib.core.AccessTransformer");
        try {
            CoreModManager.getIgnoredMods().remove(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName());
            //remove us from the CoreMod ignored list.
        } catch (Throwable ignored) {}
        System.out.println("Init mixins");
        MixinBootstrap.init();
        //Add Mixin configs.
        Mixins.addConfiguration("mixins.minecraft.json");
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.forge.json");
    }

    @Override
    public String getLaunchTarget() {
        return "miku.lib.util.Main";//No usage.
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
