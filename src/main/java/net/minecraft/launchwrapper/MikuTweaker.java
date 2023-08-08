package net.minecraft.launchwrapper;

import java.io.File;
import java.util.List;

public class MikuTweaker implements ITweaker {
    public MikuTweaker() {
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return "miku.lib.client.minecraft.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
