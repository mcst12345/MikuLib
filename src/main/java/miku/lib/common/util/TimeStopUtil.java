package miku.lib.common.util;

import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.ClientReloadWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeStopUtil {
    private static int current_time_point = 0;
    private static boolean TimeStop = false;
    private static boolean saving;
    public static long seed;
    public static String folder_name;
    public static WorldType worldType;
    public static String generatorOptions;
    private static final List<File> saved = new ArrayList<>();
    private static final File saves;

    static {
        saves = new File("saved_time_points");
        if (!saves.isDirectory()) {
            if (!saves.mkdir()) {
                throw new RuntimeException("MikuFATAL:Failed to create directory:saved_time_points");
            }
        } else {
            File[] files = saves.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) saved.add(file);
                }
            }
        }
    }

    public synchronized static void Record() {
        saving = true;
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
        server.saveAllWorlds(true);
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
            saved.add(save);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saving = false;
    }

    public static boolean isSaving() {
        return saving;
    }

    public static boolean isTimeStop() {
        return TimeStop;
    }

    public static void SetTimeStop() {
        TimeStop = !TimeStop;
    }

    public synchronized static void BackToPoint() {
        saving = true;
        File save = saved.get(current_time_point);
        if (!save.exists()) {
            System.out.println("The fuck? A saved time point doesn't exist!");
            saved.remove(current_time_point);
            return;
        }
        File world;
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
            FileUtils.deleteDir(world);
            FileUtils.copyDir(save, world);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (worldType == null) {
            throw new RuntimeException("The fuck? worldType is null?");
        }
        if (generatorOptions == null) {
            throw new RuntimeException("The fuck? generatorOptions is null?");
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.loadAllWorlds(server.getFolderName(), server.getWorldName(), seed, worldType, generatorOptions);
        for (WorldServer worldServer : server.worlds) {
            for (EntityPlayer entity : worldServer.playerEntities) {
                if (entity instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) entity;
                    NetworkHandler.INSTANCE.sendMessageToPlayer(new ClientReloadWorld(), player);
                }
            }
        }
        for (WorldServer worldServer : server.worlds) {
            worldServer.resetUpdateEntityTick();
        }
        saving = false;
    }

    @Nullable
    public static File SwitchTimePoint() {
        if (saved.isEmpty()) return null;
        if (current_time_point + 1 < saved.size() && saved.get(current_time_point + 1) != null) {
            current_time_point++;
        } else {
            current_time_point = 0;
        }
        return saved.get(current_time_point);
    }
}
