package miku.lib.mixins.minecraftforge;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.QueuedChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraftforge.common.chunkio.ChunkIOProvider", remap = false)
public abstract class MixinChunkIOProvider implements Runnable {
    @Shadow
    private Chunk chunk;

    @Shadow
    public abstract void runCallbacks();

    @Shadow
    @Final
    private AnvilChunkLoader loader;

    @Shadow
    @Final
    private QueuedChunk chunkInfo;

    @Shadow
    private NBTTagCompound nbt;

    @Shadow
    @Final
    private ChunkProviderServer provider;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public void syncCallback() {
        if (chunk == null) {
            this.runCallbacks();
            return;
        }

        // Load Entities
        this.loader.loadEntities(this.chunkInfo.world, this.nbt.getCompoundTag("Level"), this.chunk);

        MinecraftForge.EVENT_BUS.post(new ChunkDataEvent.Load(this.chunk, this.nbt)); // Don't call ChunkDataEvent.Load async

        this.chunk.setLastSaveTime(provider.world.getTotalWorldTime());
        this.provider.chunkGenerator.recreateStructures(this.chunk, this.chunkInfo.x, this.chunkInfo.z);

        provider.loadedChunks.put(ChunkPos.asLong(this.chunkInfo.x, this.chunkInfo.z), this.chunk);
        this.chunk.onLoad();
        this.chunk.populate(provider, provider.chunkGenerator);

        this.runCallbacks();
    }
}
