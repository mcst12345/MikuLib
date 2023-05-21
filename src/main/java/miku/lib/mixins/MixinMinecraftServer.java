package miku.lib.mixins;

import miku.lib.item.SpecialItem;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(value = MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow @Final public Profiler profiler;

    @Shadow @Final public Queue<FutureTask<?>> futureTaskQueue;

    @Shadow @Final private static Logger LOGGER;

    @Shadow private int tickCounter;

    @Shadow public abstract boolean getAllowNether();

    @Shadow private PlayerList playerList;

    @Shadow public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow public abstract NetworkSystem getNetworkSystem();

    @Shadow public abstract FunctionManager getFunctionManager();

    @Shadow @Final private List<ITickable> tickables;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void updateTimeLightAndEntities()
    {
        this.profiler.startSection("jobs");

        synchronized (this.futureTaskQueue)
        {
            while (!this.futureTaskQueue.isEmpty())
            {
                Util.runTask(this.futureTaskQueue.poll(), LOGGER);
            }
        }

        this.profiler.endStartSection("levels");
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();

        Integer[] ids = net.minecraftforge.common.DimensionManager.getIDs(this.tickCounter % 200 == 0);
        for (int id : ids) {
            long i = System.nanoTime();

            if (id == 0 || this.getAllowNether()) {
                WorldServer worldserver = net.minecraftforge.common.DimensionManager.getWorld(id);
                this.profiler.func_194340_a(() ->
                        worldserver.getWorldInfo().getWorldName());

                if (this.tickCounter % 20 == 0) {
                    this.profiler.startSection("timeSync");
                    this.playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimension());
                    this.profiler.endSection();
                }

                this.profiler.startSection("tick");
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreWorldTick(worldserver);

                try {
                    worldserver.tick();
                } catch (Throwable throwable1) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
                    worldserver.addWorldInfoToCrashReport(crashreport);
                    throw new ReportedException(crashreport);
                }

                try {
                    worldserver.updateEntities();
                } catch (Throwable throwable) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
                    worldserver.addWorldInfoToCrashReport(crashreport1);
                    throw new ReportedException(crashreport1);
                }

                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostWorldTick(worldserver);
                this.profiler.endSection();
                this.profiler.startSection("tracker");
                if(!SpecialItem.isTimeStop())worldserver.getEntityTracker().tick();
                this.profiler.endSection();
                this.profiler.endSection();
            }

            worldTickTimes.get(id)[this.tickCounter % 100] = System.nanoTime() - i;
        }

        this.profiler.endStartSection("dim_unloading");
        net.minecraftforge.common.DimensionManager.unloadWorlds(worldTickTimes);
        this.profiler.endStartSection("connection");
        this.getNetworkSystem().networkTick();
        this.profiler.endStartSection("players");
        if(!SpecialItem.isTimeStop())this.playerList.onTick();
        this.profiler.endStartSection("commandFunctions");
        this.getFunctionManager().update();
        this.profiler.endStartSection("tickables");

        for (ITickable tickable : this.tickables) {
            tickable.update();
        }

        this.profiler.endSection();
    }
}
