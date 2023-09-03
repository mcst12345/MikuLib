package miku.lib.mixins.minecraft;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import miku.lib.common.api.iAnvilChunkLoader;
import miku.lib.common.api.iChunkProviderServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(value = ChunkProviderServer.class)
public class MixinChunkProviderServer implements iChunkProviderServer {

    @Shadow
    @Final
    public Long2ObjectMap<Chunk> loadedChunks;

    @Shadow
    @Final
    private Set<Long> droppedChunks;

    @Shadow
    @Final
    private Set<Long> loadingChunks;

    @Shadow
    @Final
    public IChunkLoader chunkLoader;

    @Mutable
    @Shadow
    @Final
    public IChunkGenerator chunkGenerator;

    @Shadow
    @Final
    public WorldServer world;

    @Override
    public void reload() {
        this.chunkGenerator = this.world.worldInfo.getTerrainType().getChunkGenerator(world, world.worldInfo.getGeneratorOptions());
        List<Long> cache = new LongArrayList();
        cache.addAll(loadedChunks.keySet());
        this.loadedChunks.clear();
        this.droppedChunks.clear();
        ((iAnvilChunkLoader) this.chunkLoader).reload();
        loadingChunks.addAll(cache);
    }
}
