package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.common.api.iServer;
import miku.lib.common.api.iWorldServer;
import miku.lib.common.util.TimeStopUtil;
import miku.lib.server.api.iDedicatedServer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConThreadMain;
import net.minecraft.network.rcon.RConThreadQuery;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerEula;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.CryptManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Mixin(value = DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer implements IServer, iDedicatedServer, iServer {
    @Override
    public void reloadWorld(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        this.setUserMessage("menu.loadingLevel");
        ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(saveName, true);
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

        WorldServer overworld = getWorld(0);
        overworld.worldInfo = worldinfo;
        overworld.getWorldInfo().setServerInitialized(false);
        overworld.init();
        overworld.initialize(worldsettings);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        for (int dim : DimensionManager.getStaticDimensionIDs()) {
            WorldServer world = getWorld(dim);
            ((iWorldServer) world).reload(isavehandler);
        }
        this.initialWorldChunkLoad();
    }

    @Shadow
    private boolean guiIsEnabled;

    @Shadow
    private PropertyManager settings;

    @Shadow
    private ServerEula eula;

    @Shadow
    public abstract String loadResourcePackSHA();

    @Shadow
    private boolean canSpawnStructures;

    @Shadow
    private GameType gameType;

    @Shadow
    protected abstract boolean convertFiles() throws IOException;

    @Shadow
    private RConThreadQuery rconQueryThread;

    @Shadow
    private RConThreadMain rconThread;

    @Shadow
    public abstract long getMaxTickTime();

    public MixinDedicatedServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
        super(anvilFileIn, proxyIn, dataFixerIn, authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
    }

    public void setGuiDisabled() {
        this.guiIsEnabled = false;
    }

    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public boolean init() throws IOException {
        Thread thread = new Thread("Server console handler") {
            public void run() {
                if (net.minecraftforge.server.console.TerminalHandler.handleCommands((DedicatedServer) (Object) MixinDedicatedServer.this))
                    return;
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                String s4;

                try {
                    while (!MixinDedicatedServer.this.isServerStopped() && MixinDedicatedServer.this.isServerRunning() && (s4 = bufferedreader.readLine()) != null) {
                        ((DedicatedServer) (Object) MixinDedicatedServer.this).addPendingCommand(s4, (DedicatedServer) (Object) MixinDedicatedServer.this);
                    }
                } catch (IOException ioexception1) {
                    DedicatedServer.LOGGER.error("Exception handling console input", ioexception1);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        LOGGER.info("Starting minecraft server version 1.12.2");

        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        net.minecraftforge.fml.common.FMLCommonHandler.instance().onServerStart(this);

        LOGGER.info("Loading properties");
        this.settings = new PropertyManager(new File("server.properties"));
        this.eula = new ServerEula(new File("eula.txt"));

        if (!this.eula.hasAcceptedEULA()) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            this.eula.createEULAFile();
            return false;
        } else {
            if (this.isSinglePlayer()) {
                this.setHostname("127.0.0.1");
            } else {
                this.setOnlineMode(this.settings.getBooleanProperty("online-mode", true));
                this.setPreventProxyConnections(this.settings.getBooleanProperty("prevent-proxy-connections", false));
                this.setHostname(this.settings.getStringProperty("server-ip", ""));
            }

            this.setCanSpawnAnimals(this.settings.getBooleanProperty("spawn-animals", true));
            this.setCanSpawnNPCs(this.settings.getBooleanProperty("spawn-npcs", true));
            this.setAllowPvp(this.settings.getBooleanProperty("pvp", true));
            this.setAllowFlight(this.settings.getBooleanProperty("allow-flight", false));
            this.setResourcePack(this.settings.getStringProperty("resource-pack", ""), this.loadResourcePackSHA());
            this.setMOTD(this.settings.getStringProperty("motd", "A Minecraft Server"));
            this.setForceGamemode(this.settings.getBooleanProperty("force-gamemode", false));
            this.setPlayerIdleTimeout(this.settings.getIntProperty("player-idle-timeout", 0));

            if (this.settings.getIntProperty("difficulty", 1) < 0) {
                this.settings.setProperty("difficulty", 0);
            } else if (this.settings.getIntProperty("difficulty", 1) > 3) {
                this.settings.setProperty("difficulty", 3);
            }

            this.canSpawnStructures = this.settings.getBooleanProperty("generate-structures", true);
            int i = this.settings.getIntProperty("gamemode", GameType.SURVIVAL.getID());
            this.gameType = WorldSettings.getGameTypeById(i);
            LOGGER.info("Default game type: {}", this.gameType);
            InetAddress inetaddress = null;

            if (!this.getServerHostname().isEmpty()) {
                inetaddress = InetAddress.getByName(this.getServerHostname());
            }

            if (this.getServerPort() < 0) {
                this.setServerPort(this.settings.getIntProperty("server-port", 25565));
            }

            LOGGER.info("Generating keypair");
            this.setKeyPair(CryptManager.generateKeyPair());
            LOGGER.info("Starting Minecraft server on {}:{}", this.getServerHostname().isEmpty() ? "*" : this.getServerHostname(), this.getServerPort());

            try {
                this.getNetworkSystem().addEndpoint(inetaddress, this.getServerPort());
            } catch (IOException ioexception) {
                LOGGER.warn("**** FAILED TO BIND TO PORT!");
                LOGGER.warn("The exception was: {}", ioexception.toString());
                LOGGER.warn("Perhaps a server is already running on that port?");
                return false;
            }

            if (!this.isServerInOnlineMode()) {
                LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
                LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
                LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
                LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
            }

            if (this.convertFiles()) {
                this.getPlayerProfileCache().save();
            }

            if (!PreYggdrasilConverter.tryConvert(this.settings)) {
                return false;
            } else {
                net.minecraftforge.fml.common.FMLCommonHandler.instance().onServerStarted();
                this.setPlayerList(new DedicatedPlayerList((DedicatedServer) (Object) this));
                long j = System.nanoTime();

                if (this.getFolderName() == null) {
                    this.setFolderName(this.settings.getStringProperty("level-name", "world"));
                }

                String s = this.settings.getStringProperty("level-seed", "");
                String s1 = this.settings.getStringProperty("level-type", "DEFAULT");
                String s2 = this.settings.getStringProperty("generator-settings", "");
                TimeStopUtil.generatorOptions = s2;
                long k = (new Random()).nextLong();

                if (!s.isEmpty()) {
                    try {
                        long l = Long.parseLong(s);

                        if (l != 0L) {
                            k = l;
                        }
                    } catch (NumberFormatException var16) {
                        k = s.hashCode();
                    }
                }

                WorldType worldtype = WorldType.byName(s1);

                if (worldtype == null) {
                    worldtype = WorldType.DEFAULT;
                }

                TimeStopUtil.worldType = worldtype;

                this.isCommandBlockEnabled();
                this.getOpPermissionLevel();
                this.isSnooperEnabled();
                this.getNetworkCompressionThreshold();
                this.setBuildLimit(this.settings.getIntProperty("max-build-height", 256));
                this.setBuildLimit((this.getBuildLimit() + 8) / 16 * 16);
                this.setBuildLimit(MathHelper.clamp(this.getBuildLimit(), 64, 256));
                this.settings.setProperty("max-build-height", this.getBuildLimit());
                TileEntitySkull.setProfileCache(this.getPlayerProfileCache());
                TileEntitySkull.setSessionService(this.getMinecraftSessionService());
                PlayerProfileCache.setOnlineMode(this.isServerInOnlineMode());
                if (!net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerAboutToStart(this))
                    return false;
                LOGGER.info("Preparing level \"{}\"", this.getFolderName());
                this.loadAllWorlds(this.getFolderName(), this.getFolderName(), k, worldtype, s2);
                long i1 = System.nanoTime() - j;
                String s3 = String.format("%.3fs", (double) i1 / 1.0E9D);
                LOGGER.info("Done ({})! For help, type \"help\" or \"?\"", s3);
                this.currentTime = getCurrentTimeMillis();

                if (this.settings.hasProperty("announce-player-achievements")) {
                    this.worlds[0].getGameRules().setOrCreateGameRule("announceAdvancements", this.settings.getBooleanProperty("announce-player-achievements", true) ? "true" : "false");
                    this.settings.removeProperty("announce-player-achievements");
                    this.settings.saveProperties();
                }

                if (this.settings.getBooleanProperty("enable-query", false)) {
                    LOGGER.info("Starting GS4 status listener");
                    this.rconQueryThread = new RConThreadQuery(this);
                    this.rconQueryThread.startThread();
                }

                if (this.settings.getBooleanProperty("enable-rcon", false)) {
                    LOGGER.info("Starting remote control listener");
                    this.rconThread = new RConThreadMain(this);
                    this.rconThread.startThread();
                }

                if (this.getMaxTickTime() > 0L) {
                    Thread thread1 = new Thread(new ServerHangWatchdog((DedicatedServer) (Object) this));
                    thread1.setName("Server Watchdog");
                    thread1.setDaemon(true);
                    thread1.start();
                }

                Items.AIR.getSubItems(CreativeTabs.SEARCH, NonNullList.create());
                // <3 you Grum for this, saves us ~30 patch files! --^
                return net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarting(this);
            }
        }
    }
}
