package miku.lib.common.util.timestop;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EmptyTeleporter;
import miku.lib.common.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TimeStopUtil {
    public static int time_point_mode = Integer.MIN_VALUE;
    private static int current_time_point = 0;
    private static boolean TimeStop = false;
    private static boolean saving;
    public static long seed;
    public static String folder_name;
    public static WorldType worldType;
    public static String generatorOptions;
    private static final List<String> savedPoints = new ArrayList<>();
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
                    if (file.isDirectory()) savedPoints.add(file.getAbsolutePath());
                }
            }
        }
    }

    public synchronized static void Record() {
        if (time_point_mode == Integer.MIN_VALUE) {
            time_point_mode = Sqlite.GetIntFromTable("time_point_mode", "CONFIG");
        }
        saving = true;
        if (time_point_mode == 0) {
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
            if (Launch.Client && server instanceof IntegratedServer) {
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
                savedPoints.add(save.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd--HH:mm:ss");
            Date date = new Date();
            String time = sdf.format(date);
            System.out.println("MikuInfo:Saving time point at " + time);
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer[] worlds = server.worlds;
            File save = new File("saved_time_points" + File.separator + time);
            if (!save.mkdir()) {
                throw new RuntimeException("The fuck?");
            }
            try {
                ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get("saved_time_points" + File.separator + time + File.separator + "worlds")));
                oos.writeObject(worlds);
                oos.flush();
                oos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (Launch.Client) {
                Minecraft client = Minecraft.getMinecraft();
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get("saved_time_points" + File.separator + time + File.separator + "client")));
                    oos.writeObject(client);
                    oos.flush();
                    oos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            savedPoints.add("saved_time_points" + File.separator + time);
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
        if (time_point_mode == Integer.MIN_VALUE) {
            time_point_mode = Sqlite.GetIntFromTable("time_point_mode", "CONFIG");
        }
        saving = true;
        if (time_point_mode == 0) {
            File save = new File(savedPoints.get(current_time_point));
            if (!save.exists()) {
                System.out.println("The fuck? A saved time point doesn't exist!");
                savedPoints.remove(current_time_point);
                return;
            }
            File world;

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (Launch.Client && server instanceof IntegratedServer) {
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

            final Map<EntityPlayerMP, Integer> cache = new Object2IntOpenHashMap<>();


            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                cache.put(player, player.dimension);
                player.changeDimension(-114514, EmptyTeleporter.INSTANCE);
            }

            server.loadAllWorlds(server.getFolderName(), server.getWorldName(), seed, worldType, generatorOptions);
            //((iServer) server).reloadWorld(server.getFolderName(), server.getWorldName(), seed, worldType, generatorOptions);
            //for (WorldServer worldServer : server.worlds) {
            //    worldServer.resetUpdateEntityTick();
            //}


            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                player.changeDimension(cache.get(player), EmptyTeleporter.INSTANCE);
            }
        } else {
            File WORLDS = new File(savedPoints.get(current_time_point) + File.separator + "worlds");
            try {
                ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(WORLDS.toPath()));
                WorldServer[] worlds = (WorldServer[]) ois.readObject();
                ois.close();
                FMLCommonHandler.instance().getMinecraftServerInstance().worlds = worlds;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (Launch.Client) {
                File CLIENT = new File(savedPoints.get(current_time_point) + File.separator + "client");
                try {
                    ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(CLIENT.toPath()));
                    Minecraft client = (Minecraft) ois.readObject();
                    ois.close();
                    Minecraft.instance = client;
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        saving = false;
    }

    @Nullable
    public static File SwitchTimePoint() {
        if (time_point_mode == Integer.MIN_VALUE) {
            time_point_mode = Sqlite.GetIntFromTable("time_point_mode", "CONFIG");
        }
        if (savedPoints.isEmpty()) return null;
        if (current_time_point + 1 < savedPoints.size() && savedPoints.get(current_time_point + 1) != null) {
            current_time_point++;
        } else {
            current_time_point = 0;
        }
        return new File(savedPoints.get(current_time_point));
    }
}
