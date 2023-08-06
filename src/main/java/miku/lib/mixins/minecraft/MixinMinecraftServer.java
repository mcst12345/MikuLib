package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import miku.lib.common.item.SpecialItem;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

@Mixin(value = MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow private int tickCounter;

    @Shadow private boolean startProfiling;

    @Shadow @Final public Profiler profiler;

    @Shadow public abstract void updateTimeLightAndEntities();

    @Shadow private long nanoTimeSinceStatusRefresh;

    @Shadow @Final private ServerStatusResponse statusResponse;

    @Shadow @Final private Random random;

    @Shadow public abstract int getMaxPlayers();

    @Shadow public abstract int getCurrentPlayerCount();

    @Shadow private PlayerList playerList;

    @Shadow public abstract void saveAllWorlds(boolean isSilent);

    @Shadow @Final public long[] tickTimeArray;

    @Shadow @Final private Snooper usageSnooper;

    @Shadow public abstract boolean init();

    @Shadow protected long currentTime;

    @Shadow
    public static long getCurrentTimeMillis() {
        return 0;
    }

    @Shadow public abstract void applyServerIconToResponse(ServerStatusResponse response);

    @Shadow private boolean serverRunning;

    @Shadow private long timeOfLastWarning;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public WorldServer[] worlds;

    @Shadow private boolean serverIsRunning;

    @Shadow public abstract void finalTick(CrashReport report);

    @Shadow public abstract CrashReport addServerInfoToCrashReport(CrashReport report);

    @Shadow public abstract File getDataDirectory();

    @Shadow public abstract void stopServer();

    @Shadow private boolean serverStopped;

    @Shadow public abstract void systemExitNow();

    @Shadow private String motd;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void tick()
    {
        long i = System.nanoTime();
        if (!SpecialItem.isTimeStop()) {
            try {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreServerTick();
            } catch (Throwable e) {
                System.out.println("MikuWarn:Catch exception at onPreServerTick.");
                e.printStackTrace();
            }
        }
        if (!SpecialItem.isTimeStop()) ++this.tickCounter;

        if (this.startProfiling) {
            this.startProfiling = false;
            this.profiler.profilingEnabled = true;
            this.profiler.clearProfiling();
        }

        this.profiler.startSection("root");
        if(!SpecialItem.isTimeStop())this.updateTimeLightAndEntities();

        if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L && !SpecialItem.isTimeStop())
        {
            this.nanoTimeSinceStatusRefresh = i;
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k)
            {
                agameprofile[k] = this.playerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.statusResponse.getPlayers().setPlayers(agameprofile);
            this.statusResponse.invalidateJson();
        }

        if (this.tickCounter % 900 == 0)
        {
            this.profiler.startSection("save");
            this.playerList.saveAllPlayerData();
            this.saveAllWorlds(true);
            this.profiler.endSection();
        }

        this.profiler.startSection("tallying");
        if(!SpecialItem.isTimeStop())this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        this.profiler.endSection();
        this.profiler.startSection("snooper");

        if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100 && !SpecialItem.isTimeStop())
        {
            this.usageSnooper.startSnooper();
        }

        if (this.tickCounter % 6000 == 0 && !SpecialItem.isTimeStop()) {
            this.usageSnooper.addMemoryStatsToSnooper();
        }

        this.profiler.endSection();
        this.profiler.endSection();
        if (!SpecialItem.isTimeStop()) {
            try {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostServerTick();
            } catch (Throwable t) {
                System.out.println("MikuWarn:catch exception at onPostServerTick");
                t.printStackTrace();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void run()
    {
        try
        {
            if (this.init())
            {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarted();
                this.currentTime = getCurrentTimeMillis();
                long i = 0L;
                this.statusResponse.setServerDescription(new TextComponentString(this.motd));
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning)
                {

                    long k = getCurrentTimeMillis();
                    long j = k - this.currentTime;

                    if (j > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L)
                    {
                        LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", j, j / 50L);
                        j = 2000L;
                        this.timeOfLastWarning = this.currentTime;
                    }

                    if (j < 0L)
                    {
                        LOGGER.warn("Time ran backwards! Did the system time change?");
                        j = 0L;
                    }

                    i += j;
                    this.currentTime = k;

                    if (this.worlds[0].areAllPlayersAsleep())
                    {
                        try {
                            this.tick();
                        } catch (Throwable e) {
                            System.out.println("MikuWarn:catch exception when ticking:" + e);
                        }
                        i = 0L;
                    }
                    else
                    {
                        while (i > 50L)
                        {
                            i -= 50L;
                            try {
                                this.tick();
                            } catch (Throwable e) {
                                System.out.println("MikuWarn:catch exception when ticking:" + e);
                            }
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    this.serverIsRunning = true;
                }
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopping();
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            }
            else
            {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
                this.finalTick(null);
            }
        }
        catch (net.minecraftforge.fml.common.StartupQuery.AbortedException e)
        {
            // ignore silently
            net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
        }
        catch (Throwable throwable1)
        {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;

            if (throwable1 instanceof ReportedException)
            {
                crashreport = this.addServerInfoToCrashReport(((ReportedException)throwable1).getCrashReport());
            }
            else
            {
                crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", throwable1));
            }

            File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1))
            {
                LOGGER.error("This crash report has been saved to: {}", file1.getAbsolutePath());
            }
            else
            {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            net.minecraftforge.fml.common.FMLCommonHandler.instance().expectServerStopped(); // has to come before finalTick to avoid race conditions
            this.finalTick(crashreport);
        }
        finally
        {
            try
            {
                this.stopServer();
            }
            catch (Throwable throwable)
            {
                LOGGER.error("Exception stopping the server", throwable);
            }
            finally
            {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }
        }
    }
}
