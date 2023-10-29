package miku.lib.mixins.minecraft;

import miku.lib.common.api.iAnvilChunkLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IThreadedFileIO;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(value = AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader implements IChunkLoader, IThreadedFileIO, iAnvilChunkLoader {
    @Override
    public void reload() {
        this.chunksBeingSaved.clear();
        this.chunksToSave.clear();
    }

    @Shadow
    @Final
    private Set<ChunkPos> chunksBeingSaved;

    @Shadow
    @Final
    private Map<ChunkPos, NBTTagCompound> chunksToSave;

}
