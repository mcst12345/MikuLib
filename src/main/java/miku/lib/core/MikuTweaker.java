package miku.lib.core;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;
import miku.lib.sqlite.Sqlite;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import sun.tools.attach.LinuxAttachProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MikuTweaker implements ITweaker {
    public MikuTweaker() {

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
        try {
            CoreModManager.getIgnoredMods().remove(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName()); // <-- 将自身从 CoreMod 忽略列表中移除
        } catch (Throwable ignored) {}
        System.out.println("Init mixins");
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.minecraft.json");
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.forge.json");
        System.out.println("Add MikuTransformer");
        //classLoader.registerTransformer("miku.lib.core.MikuTransformer");



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
