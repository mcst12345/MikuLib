package net.minecraft.launchwrapper;

import miku.lib.common.sqlite.Sqlite;

import java.io.File;
import java.util.List;

public class MikuTweaker implements ITweaker {
    public MikuTweaker() {
        if (Launch.MikuLibInstalled()) {
            Sqlite.CoreInit();
        }
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
