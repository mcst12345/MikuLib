package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IThreadedFileIO;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;

@Mixin(value = AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader implements IChunkLoader, IThreadedFileIO {
    @Shadow
    protected abstract void writeChunkToNBT(Chunk chunkIn, World worldIn, NBTTagCompound compound);

    @Shadow
    protected abstract void addChunkToPending(ChunkPos pos, NBTTagCompound compound);

    @Shadow
    @Final
    private static Logger LOGGER;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void saveChunk(World worldIn, @Nonnull Chunk chunkIn) throws MinecraftException {
        worldIn.checkSessionLock();

        try {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound.setTag("Level", nbttagcompound1);
            nbttagcompound.setInteger("DataVersion", 1343);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().getDataFixer().writeVersionData(nbttagcompound);
            this.writeChunkToNBT(chunkIn, worldIn, nbttagcompound1);
            net.minecraftforge.common.ForgeChunkManager.storeChunkNBT(chunkIn, nbttagcompound1);
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.ChunkDataEvent.Save(chunkIn, nbttagcompound));
            this.addChunkToPending(chunkIn.getPos(), nbttagcompound);
        } catch (Exception exception) {
            LOGGER.error("Failed to save chunk", exception);
        }
    }
}
