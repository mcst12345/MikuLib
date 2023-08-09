package miku.lib.mixins.minecraft;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import miku.lib.common.core.MikuLib;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChunkProviderClient.class)
public class MixinChunkProviderClient {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Long2ObjectMap<Chunk> loadedChunks;

    /**
     * @author mcst12345
     * @reason FUCK!!!!!
     */
    @Overwrite
    public Chunk loadChunk(int chunkX, int chunkZ) {
        Chunk chunk = new Chunk(this.world, chunkX, chunkZ);
        this.loadedChunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
        chunk.markLoaded(true);
        return chunk;
    }
}
