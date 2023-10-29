package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.api.iServer;
import miku.lib.common.api.iWorldServer;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.net.Proxy;

@Mixin(value = IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer implements iServer {
    @Override
    public void reloadWorld(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
        this.convertMapIfNeeded(saveName);
        ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(saveName, true);
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
        }
        for (int dim : DimensionManager.getStaticDimensionIDs()) {

            WorldServer world = getWorld(dim);
            world.worldInfo = worldinfo;
            ((iWorldServer) world).reload(isavehandler);
            world.getWorldInfo().setServerInitialized(false);
            world.init();
            world.initialize(this.worldSettings);
        }
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
}
