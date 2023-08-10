package miku.lib.mixins.minecraftforge;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import miku.lib.common.core.MikuLib;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Hashtable;

@Mixin(value = DimensionManager.class, remap = false)
public abstract class MixinDimensionManager {
    @Shadow
    public static WorldServer getWorld(int id) {
        return null;
    }

    @Shadow
    @Final
    private static IntSet unloadQueue;

    @Shadow
    @Final
    private static Int2ObjectMap<DimensionManager.Dimension> dimensions;

    @Shadow
    @Final
    private static Int2ObjectMap<WorldServer> worlds;

    @Shadow
    private static boolean canUnloadWorld(WorldServer world) {
        return false;
    }

    @Shadow
    public static void setWorld(int id, @Nullable WorldServer world, MinecraftServer server) {
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void initDimension(int dim) {
        WorldServer overworld = getWorld(0);
        if (overworld == null) {
            throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
        }
        try {
            DimensionManager.getProviderType(dim);
        } catch (Exception e) {
            FMLLog.log.error("Cannot Hotload Dim: {}", dim, e);
            return; // If a provider hasn't been registered then we can't hotload the dim
        }
        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();
        //WorldSettings worldSettings = new WorldSettings(overworld.getWorldInfo());

        assert mcServer != null;

        WorldServer world = (dim == 0 ? overworld : (WorldServer) (new WorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.profiler).init()));
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MikuLib.MikuEventBus().post(new WorldEvent.Load(world));
        if (!mcServer.isSinglePlayer()) {
            world.getWorldInfo().setGameType(mcServer.getGameType());
        }

        mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
        IntIterator queueIterator = unloadQueue.iterator();
        while (queueIterator.hasNext()) {
            int id = queueIterator.nextInt();
            DimensionManager.Dimension dimension = dimensions.get(id);
            if (dimension.ticksWaited < ForgeModContainer.dimensionUnloadQueueDelay) {
                dimension.ticksWaited++;
                continue;
            }
            WorldServer w = worlds.get(id);
            queueIterator.remove();
            dimension.ticksWaited = 0;
            // Don't unload the world if the status changed
            if (w == null || !canUnloadWorld(w)) {
                FMLLog.log.debug("Aborting unload for dimension {} as status changed", id);
                continue;
            }
            try {
                w.saveAllChunks(true, null);
            } catch (MinecraftException e) {
                FMLLog.log.error("Caught an exception while saving all chunks:", e);
            } finally {
                MikuLib.MikuEventBus().post(new WorldEvent.Unload(w));
                w.flush();
                setWorld(id, null, w.getMinecraftServer());
            }
        }
    }
}
