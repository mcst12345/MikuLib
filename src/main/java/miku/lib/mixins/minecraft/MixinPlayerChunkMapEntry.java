package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = PlayerChunkMapEntry.class)
public abstract class MixinPlayerChunkMapEntry {
    @Shadow
    @Final
    private List<EntityPlayerMP> players;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private long lastUpdateInhabitedTime;

    @Shadow
    @Final
    private PlayerChunkMap playerChunkMap;

    @Shadow
    private boolean sentToPlayers;

    @Shadow
    public abstract void sendToPlayer(EntityPlayerMP player);

    @Shadow
    @Final
    private ChunkPos pos;

    @Shadow
    @Nullable
    private Chunk chunk;

    @Shadow
    private boolean loading;

    @Shadow
    private Runnable loadedRunnable;

    @Shadow
    private int changes;

    @Shadow
    private int changedSectionFilter;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void addPlayer(EntityPlayerMP player) {
        if (EntityUtil.isDEAD(player)) return;
        if (this.players.contains(player)) {
            LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", player, this.pos.x, this.pos.z);
        } else {
            if (this.players.isEmpty()) {
                this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
            }

            this.players.add(player);

            if (this.sentToPlayers) {
                this.sendToPlayer(player);
                // chunk watch event - the chunk is ready
                assert this.chunk != null;
                MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.ChunkWatchEvent.Watch(this.chunk, player));
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void removePlayer(EntityPlayerMP player) {
        if (this.players.contains(player)) {
            // If we haven't loaded yet don't load the chunk just so we can clean it up
            if (this.chunk == null) {
                this.players.remove(player);

                if (this.players.isEmpty()) {
                    if (this.loading)
                        net.minecraftforge.common.chunkio.ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.x, this.pos.z, this.loadedRunnable);
                    this.playerChunkMap.removeEntry((PlayerChunkMapEntry) (Object) this);
                }

                return;
            }

            if (this.sentToPlayers) {
                player.connection.sendPacket(new SPacketUnloadChunk(this.pos.x, this.pos.z));
            }

            this.players.remove(player);

            MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.ChunkWatchEvent.UnWatch(this.chunk, player));

            if (this.players.isEmpty()) {
                this.playerChunkMap.removeEntry((PlayerChunkMapEntry) (Object) this);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean sendToPlayers() {
        if (this.sentToPlayers) {
            return true;
        } else if (this.chunk == null) {
            return false;
        } else if (!this.chunk.isPopulated()) {
            return false;
        } else {
            this.changes = 0;
            this.changedSectionFilter = 0;
            this.sentToPlayers = true;
            if (this.players.isEmpty()) return true; // Forge: fix MC-120780
            Packet<?> packet = new SPacketChunkData(this.chunk, 65535);

            for (EntityPlayerMP entityplayermp : this.players) {
                entityplayermp.connection.sendPacket(packet);
                this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(entityplayermp, this.chunk);
                // chunk watch event - delayed to here as the chunk wasn't ready in addPlayer
                MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.ChunkWatchEvent.Watch(this.chunk, entityplayermp));
            }

            return true;
        }
    }
}
