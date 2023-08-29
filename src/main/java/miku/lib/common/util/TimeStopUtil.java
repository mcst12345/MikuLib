package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStopUtil {
    private static final File saves;

    static {
        saves = new File("saved_time_points");
        if (!saves.isDirectory()) {
            if (!saves.mkdir()) {
                throw new RuntimeException("MikuFATAL:Failed to create directory:saved_time_points");
            }
        }
    }

    public synchronized static void Record() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.getPlayerList().saveAllPlayerData();
        for (WorldServer worldserver : server.worlds) {
            if (worldserver != null) {
                worldserver.disableLevelSaving = false;
            }
        }
        server.saveAllWorlds(false);
        for (WorldServer worldserver : server.worlds) {
            if (worldserver != null) {
                worldserver.flushToDisk();
            }
        }
        if (Launch.Client) {
            //TODO
        } else {
            File world = new File("world");
            if (!world.exists()) {
                throw new RuntimeException("The fuck?");
            }
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd--HH:mm:ss");
            Date date = new Date();
            String time = sdf.format(date);
            File save = new File("saved_time_points/" + time);
            try {
                FileUtils.CopyDir(world, save);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
