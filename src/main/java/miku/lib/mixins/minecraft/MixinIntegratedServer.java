package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.api.iServer;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.Proxy;

@Mixin(value = IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer implements iServer {
    @Override
    public void reloadWorld(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        this.initialWorldChunkLoad();
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void IntegratedServer(Minecraft clientIn, String folderNameIn, String worldNameIn, WorldSettings worldSettingsIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn, CallbackInfo ci) {
        TimeStopUtil.folder_name = folderNameIn;
        TimeStopUtil.worldType = worldSettingsIn.getTerrainType();
        TimeStopUtil.generatorOptions = worldSettingsIn.getGeneratorOptions();
    }

    @Shadow
    private boolean isGamePaused;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    @Final
    private WorldSettings worldSettings;

    public MixinIntegratedServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
        super(anvilFileIn, proxyIn, dataFixerIn, authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
    }

    /**
     * @author mcst12345
     * @reason ShitFuck!!!!!!!!!!!!!!!!!
     */
    @Overwrite
    public void tick() {
        if (TimeStopUtil.isSaving()) return;
        boolean flag = this.isGamePaused;
        this.isGamePaused = Minecraft.getMinecraft().getConnection() != null && Minecraft.getMinecraft().isGamePaused();

        if (!flag && this.isGamePaused) {
            LOGGER.info("Saving and pausing game...");
            this.getPlayerList().saveAllPlayerData();
            this.saveAllWorlds(false);
        }

        if (this.isGamePaused) {
            synchronized (this.futureTaskQueue) {
                while (!this.futureTaskQueue.isEmpty()) {
                    Util.runTask(this.futureTaskQueue.poll(), LOGGER);
                }
            }
        } else {
            super.tick();

            if (this.mc.gameSettings.renderDistanceChunks != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", this.mc.gameSettings.renderDistanceChunks, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(this.mc.gameSettings.renderDistanceChunks);
            }

            if (((iMinecraft) this.mc).MikuWorld() != null) {
                WorldInfo worldinfo1 = this.worlds[0].getWorldInfo();
                WorldInfo worldinfo = ((iMinecraft) this.mc).MikuWorld().getWorldInfo();

                if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
                    LOGGER.info("Changing difficulty to {}, from {}", worldinfo.getDifficulty(), worldinfo1.getDifficulty());
                    this.setDifficultyForAllWorlds(worldinfo.getDifficulty());
                } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
                    LOGGER.info("Locking difficulty to {}", worldinfo.getDifficulty());

                    for (WorldServer worldserver : this.worlds) {
                        if (worldserver != null) {
                            worldserver.getWorldInfo().setDifficultyLocked(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    @Nonnull
    public EnumDifficulty getDifficulty() {
        if (((iMinecraft) this.mc).MikuWorld() == null) return this.mc.gameSettings.difficulty; // Fix NPE just in case.
        return ((iMinecraft) this.mc).MikuWorld().getWorldInfo().getDifficulty();
    }

    /**
     * @author mcst12345
     * @reason F!
     */
    @Overwrite
    public void setDifficultyForAllWorlds(@Nonnull EnumDifficulty difficulty) {
        super.setDifficultyForAllWorlds(difficulty);

        if (((iMinecraft) this.mc).MikuWorld() != null) {
            ((iMinecraft) this.mc).MikuWorld().getWorldInfo().setDifficulty(difficulty);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void loadAllWorlds(@Nonnull String saveName, @Nonnull String worldNameIn, long seed, @Nonnull WorldType type, @Nonnull String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null) {
            worldinfo = new WorldInfo(this.worldSettings, worldNameIn);
        } else {
            worldinfo.setWorldName(worldNameIn);
        }

        WorldServer overWorld = (isDemo() ? (WorldServer) (new WorldServerDemo(this, isavehandler, worldinfo, 0, this.profiler)).init() :
                (WorldServer) (new WorldServer(this, isavehandler, worldinfo, 0, this.profiler)).init());
        overWorld.initialize(this.worldSettings);
        for (int dim : net.minecraftforge.common.DimensionManager.getStaticDimensionIDs()) {
            WorldServer world = (dim == 0 ? overWorld : (WorldServer) (new WorldServerMulti(this, isavehandler, dim, overWorld, this.profiler)).init());
            world.addEventListener(new ServerWorldEventHandler(this, world));
            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(getGameType());
            }
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.WorldEvent.Load(world));
        }

        this.getPlayerList().setPlayerManager(new WorldServer[]{overWorld});

        if (overWorld.getWorldInfo().getDifficulty() == null) {
            this.setDifficultyForAllWorlds(this.mc.gameSettings.difficulty);
        }

        this.initialWorldChunkLoad();
    }
}
