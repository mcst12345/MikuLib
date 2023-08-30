package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeStopUtil {
    public static String folder_name;
    private static final List<File> saved = new ArrayList<>();
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
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd--HH:mm:ss");
        Date date = new Date();
        String time = sdf.format(date);
        System.out.println("MikuInfo:Saving time point at " + time);
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
        File world;
        File save = new File("saved_time_points" + File.separator + time);
        if (Launch.Client) {
            if (folder_name == null) {
                throw new IllegalStateException("The fuck? Why is folder_name null? Aren't you on client side?");
            }
            world = new File("saves" + File.separator + folder_name);
        } else {
            world = new File("world");

        }
        if (!world.exists()) {
            throw new RuntimeException("The fuck?");
        }
        try {
            System.out.println("MikuInfo:Coping folder");
            FileUtils.copyDir(world, save);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
