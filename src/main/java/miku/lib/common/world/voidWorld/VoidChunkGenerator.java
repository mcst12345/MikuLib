package miku.lib.common.world.voidWorld;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VoidChunkGenerator implements IChunkGenerator {
    private final World world;
    
    
    public VoidChunkGenerator(World world){

        this.world = world;
    }
    @Override
    @Nonnull
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        for(int i=0; i<16; i++){
            for(int j=0; j<16; j++){
                primer.setBlockState(i,0,j, Blocks.BARRIER.getDefaultState());
            }
        }
        return new Chunk(this.world,primer,x,z);
    }

    @Override
    public void populate(int x, int z) {

    }

    @Override
    public boolean generateStructures(@Nonnull Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    @Nonnull
    public List<Biome.SpawnListEntry> getPossibleCreatures(@Nonnull EnumCreatureType creatureType,@Nonnull BlockPos pos) {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(@Nonnull World worldIn,@Nonnull String structureName,@Nonnull BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(@Nonnull Chunk chunkIn, int x, int z) {

    }

    @Override
    public boolean isInsideStructure(@Nonnull World worldIn,@Nonnull String structureName,@Nonnull BlockPos pos) {
        return false;
    }
}
