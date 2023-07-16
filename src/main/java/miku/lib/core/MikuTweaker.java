package miku.lib.core;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MikuTweaker implements ITweaker {
    public MikuTweaker() throws IOException {
       boolean flag = true;
        for(File file : new File("mods").listFiles()){
            if(file.getName().equals("MikuLib-SQlite-1.0.jar")){
                flag = false;
                break;
            }
        }
        if(flag){
            System.out.println("MikuLib's sqlite module doesn't exists,extract it.");
            InputStream stream = MikuTweaker.class.getResourceAsStream("/MikuLib-SQlite-1.0.jar");
            byte[] file = new byte[stream.available()];

            stream.read(file);
            stream.close();
            FileOutputStream outputStream = new FileOutputStream("mods/MikuLib-SQlite-1.0.jar");
            outputStream.write(file);
            outputStream.close();
            System.out.println("MikuLib has just extracted the sqlite module of it. Please restart it.");
            throw new RuntimeException("MikuLib has completed its installation,please restart the game.");
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
        classLoader.registerTransformer("miku.lib.core.MikuTransformer");
        try {
            CoreModManager.getIgnoredMods().remove(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName()); // <-- 将自身从 CoreMod 忽略列表中移除
        } catch (Throwable ignored) {}
        System.out.println("Init mixins");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.minecraft.json");
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.forge.json");

        //classLoader.registerTransformer("miku.lib.core.AccessTransformer");

    }

    @Override
    public String getLaunchTarget() {
        return "miku.lib.util.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
