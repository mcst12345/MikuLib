package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.common.api.iMinecraftServer;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.FieldUtil;
import miku.lib.common.util.TimeStopUtil;
import miku.lib.server.api.iDedicatedServer;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.command.CommandBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerEula;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.io.File;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.FutureTask;

@Mixin(value = MinecraftServer.class)
public abstract class MixinMinecraftServer implements iMinecraftServer {
    @Override
    public void SetStopped(boolean stop) {
        this.serverStopped = stop;
    }

    @Shadow
    private int tickCounter;

    @Shadow
    private boolean startProfiling;

    @Shadow
    @Final
    public Profiler profiler;

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void updateTimeLightAndEntities() {
        this.profiler.startSection("jobs");

        synchronized (this.futureTaskQueue) {
            while (!this.futureTaskQueue.isEmpty()) {
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
                if (!TimeStopUtil.isTimeStop())
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

                if (!TimeStopUtil.isTimeStop())
                    net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostWorldTick(worldserver);
                this.profiler.endSection();
                this.profiler.startSection("tracker");
                worldserver.getEntityTracker().tick();
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
        this.playerList.onTick();
        this.profiler.endStartSection("commandFunctions");
        this.getFunctionManager().update();
        this.profiler.endStartSection("tickables");

        for (ITickable tickable : this.tickables) {
            tickable.update();
        }

        this.profiler.endSection();
    }

    @Shadow
    private long nanoTimeSinceStatusRefresh;

    @Shadow
    @Final
    private ServerStatusResponse statusResponse;

    @Shadow
    @Final
    private Random random;

    @Shadow
    public abstract int getMaxPlayers();

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

    @Shadow
    public WorldServer[] worlds;

    @Shadow
    private boolean serverIsRunning;

    @Shadow
    public abstract void finalTick(CrashReport report);

    @Shadow
    public abstract CrashReport addServerInfoToCrashReport(CrashReport report);

    @Shadow
    public abstract File getDataDirectory();

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void stopServer() {
        LOGGER.info("Stopping server");

        if (this.getNetworkSystem() != null) {
            this.getNetworkSystem().terminateEndpoints();
        }

        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAllPlayerData();
            this.playerList.removeAllPlayers();
        }

        if (this.worlds != null) {
            LOGGER.info("Saving worlds");

            for (WorldServer worldserver : this.worlds) {
                if (worldserver != null) {
                    worldserver.disableLevelSaving = false;
                }
            }

            this.saveAllWorlds(false);

            for (WorldServer worldserver1 : this.worlds) {
                if (worldserver1 != null) {
                    MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.WorldEvent.Unload(worldserver1));
                    worldserver1.flush();
                }
            }

            WorldServer[] tmp = worlds;
            for (WorldServer world : tmp) {
                net.minecraftforge.common.DimensionManager.setWorld(world.provider.getDimension(), null, (MinecraftServer) (Object) this);
            }
        }

        if (this.usageSnooper.isSnooperRunning()) {
            this.usageSnooper.stopSnooper();
        }

        CommandBase.setCommandListener(null); // Forge: fix MC-128561
    }

    @Shadow
    private boolean serverStopped;

    @Shadow
    public abstract void systemExitNow();

    @Shadow
    private String motd;

    @Shadow
    public abstract void initialWorldChunkLoad();

    @Shadow
    public abstract void convertMapIfNeeded(String worldNameIn);

    @Shadow
    protected abstract void setUserMessage(String message);

    @Shadow
    @Final
    private ISaveFormat anvilConverterForAnvilFile;

    @Shadow
    public abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);

    @Shadow
    public abstract String getFolderName();

    @Shadow
    public abstract GameType getGameType();

    @Shadow
    public abstract boolean canStructuresSpawn();

    @Shadow
    public abstract boolean isHardcore();

    @Shadow
    private boolean enableBonusChest;

    @Shadow
    public abstract boolean isSinglePlayer();

    @Shadow
    public abstract void setDifficultyForAllWorlds(EnumDifficulty difficulty);

    @Shadow
    public abstract EnumDifficulty getDifficulty();

    @Shadow
    public abstract NetworkSystem getNetworkSystem();

    @Shadow
    @Final
    public static File USER_CACHE_FILE;

    @Shadow
    @Final
    public Queue<FutureTask<?>> futureTaskQueue;

    @Shadow
    public abstract boolean getAllowNether();

    @Shadow
    public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow
    public abstract FunctionManager getFunctionManager();

    @Shadow
    @Final
    private List<ITickable> tickables;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void tick() {
        long i = System.nanoTime();
        if (!TimeStopUtil.isTimeStop()) {
            try {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreServerTick();
            } catch (Throwable e) {
                System.out.println("MikuWarn:Catch exception at onPreServerTick.");
                e.printStackTrace();
            }
        }
        if (!TimeStopUtil.isTimeStop()) ++this.tickCounter;

        if (this.startProfiling) {
            this.startProfiling = false;
            this.profiler.profilingEnabled = true;
            this.profiler.clearProfiling();
        }

        this.profiler.startSection("root");
        this.updateTimeLightAndEntities();

        if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L && !TimeStopUtil.isTimeStop()) {
            this.nanoTimeSinceStatusRefresh = i;
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
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
        if (!TimeStopUtil.isTimeStop()) this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        this.profiler.endSection();
        this.profiler.startSection("snooper");

        if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100 && !TimeStopUtil.isTimeStop()) {
            this.usageSnooper.startSnooper();
        }

        if (this.tickCounter % 6000 == 0 && !TimeStopUtil.isTimeStop()) {
            this.usageSnooper.addMemoryStatsToSnooper();
        }

        this.profiler.endSection();
        this.profiler.endSection();
        if (!TimeStopUtil.isTimeStop()) {
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
    public void run() {
        FieldUtil.Init();
        System.out.println("Successfully fucked Minecraft Server.");
        try {
            if (this.init()) {
                MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarted();
                this.currentTime = getCurrentTimeMillis();
                long i = 0L;
                this.statusResponse.setServerDescription(new TextComponentString(this.motd));
                this.statusResponse.setVersion(new ServerStatusResponse.Version("1.12.2", 340));
                this.applyServerIconToResponse(this.statusResponse);

                while (this.serverRunning)
                {
                    MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();

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
                            if (e instanceof ReportedException) {
                                ((ReportedException) e).getCause().printStackTrace();
                            } else e.printStackTrace();
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
                                if (e instanceof ReportedException) {
                                    ((ReportedException) e).getCause().printStackTrace();
                                } else e.printStackTrace();
                            }
                        }
                    }

                    Thread.sleep(Math.max(1L, 50L - i));
                    this.serverIsRunning = true;
                    MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
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
            } finally {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStopped();
                this.serverStopped = true;
                this.systemExitNow();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason HolyShit
     */
    @Overwrite
    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        this.setUserMessage("menu.loadingLevel");
        ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        WorldSettings worldsettings;

        if (worldinfo == null) {
            worldsettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
            worldsettings.setGeneratorOptions(generatorOptions);

            if (this.enableBonusChest) {
                worldsettings.enableBonusChest();
            }

            worldinfo = new WorldInfo(worldsettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
            worldsettings = new WorldSettings(worldinfo);
        }

        WorldServer overWorld = (WorldServer) (new WorldServer((MinecraftServer) (Object) this, isavehandler, worldinfo, 0, profiler).init());
        overWorld.initialize(worldsettings);
        for (int dim : net.minecraftforge.common.DimensionManager.getStaticDimensionIDs()) {
            WorldServer world = (dim == 0 ? overWorld : (WorldServer) new WorldServerMulti((MinecraftServer) (Object) this, isavehandler, dim, overWorld, profiler).init());
            world.addEventListener(new ServerWorldEventHandler((MinecraftServer) (Object) this, world));

            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(this.getGameType());
            }
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.WorldEvent.Load(world));
        }

        this.playerList.setPlayerManager(new WorldServer[]{overWorld});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    /**
     * @author mcst12345
     * @reason The fuck?
     */
    @SideOnly(Side.SERVER)
    @Overwrite(remap = false)
    public static void main(String[] p_main_0_) {
        //Forge: Copied from DedicatedServer.init as to run as early as possible, Old code left in place intentionally.
        //Done in good faith with permission: https://github.com/MinecraftForge/MinecraftForge/issues/3659#issuecomment-390467028
        ServerEula eula = new ServerEula(new File("eula.txt"));
        if (!eula.hasAcceptedEULA()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            eula.createEULAFile();
            return;
        }

        Bootstrap.register();

        try {
            boolean flag = true;
            String s = null;
            String s1 = ".";
            String s2 = null;
            boolean flag1 = false;
            boolean flag2 = false;
            int l = -1;

            for (int i1 = 0; i1 < p_main_0_.length; ++i1) {
                String s3 = p_main_0_[i1];
                String s4 = i1 == p_main_0_.length - 1 ? null : p_main_0_[i1 + 1];
                boolean flag3 = false;

                if (!"nogui".equals(s3) && !"--nogui".equals(s3)) {
                    if ("--port".equals(s3) && s4 != null) {
                        flag3 = true;

                        try {
                            l = Integer.parseInt(s4);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if ("--singleplayer".equals(s3) && s4 != null) {
                        flag3 = true;
                        s = s4;
                    } else if ("--universe".equals(s3) && s4 != null) {
                        flag3 = true;
                        s1 = s4;
                    } else if ("--world".equals(s3) && s4 != null) {
                        flag3 = true;
                        s2 = s4;
                    } else if ("--demo".equals(s3)) {
                        flag1 = true;
                    } else if ("--bonusChest".equals(s3)) {
                        flag2 = true;
                    }
                } else {
                    flag = false;
                }

                if (flag3) {
                    ++i1;
                }
            }

            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(s1, USER_CACHE_FILE.getName()));
            final DedicatedServer dedicatedserver = new DedicatedServer(new File(s1), DataFixesManager.createFixer(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, playerprofilecache);

            if (s != null) {
                dedicatedserver.setServerOwner(s);
            }

            if (s2 != null) {
                dedicatedserver.setFolderName(s2);
            }

            if (l >= 0) {
                dedicatedserver.setServerPort(l);
            }

            if (flag1) {
                dedicatedserver.setDemo(true);
            }

            if (flag2) {
                dedicatedserver.canCreateBonusChest(true);
            }

            if (flag && !GraphicsEnvironment.isHeadless()) {
                try {
                    dedicatedserver.setGuiEnabled();
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Failed to create server gui.");
                    t.printStackTrace();
                    ((iDedicatedServer) dedicatedserver).setGuiDisabled();
                }
            }

            dedicatedserver.startServerThread();
            Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
                public void run() {
                    dedicatedserver.stopServer();
                }
            });
        } catch (Exception exception) {
            LOGGER.fatal("Failed to start the minecraft server", exception);
        }
    }
}
