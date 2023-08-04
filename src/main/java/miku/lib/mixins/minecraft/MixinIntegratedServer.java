package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.Proxy;

@Mixin(value = IntegratedServer.class)
public abstract class MixinIntegratedServer extends MinecraftServer {

    @Shadow
    private boolean isGamePaused;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Minecraft mc;

    public MixinIntegratedServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
        super(anvilFileIn, proxyIn, dataFixerIn, authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
    }

    /**
     * @author mcst12345
     * @reason ShitFuck!!!!!!!!!!!!!!!!!
     */
    @Overwrite
    public void tick() {
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
}