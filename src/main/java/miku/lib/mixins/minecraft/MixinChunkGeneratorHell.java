package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.block.BlockFalling;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = ChunkGeneratorHell.class)
public abstract class MixinChunkGeneratorHell implements IChunkGenerator {
    @Shadow
    double[] noiseData4;

    @Shadow
    public NoiseGeneratorOctaves scaleNoise;

    @Shadow
    double[] dr;

    @Shadow
    public NoiseGeneratorOctaves depthNoise;

    @Shadow
    double[] pnr;

    @Shadow
    private NoiseGeneratorOctaves perlinNoise1;

    @Shadow
    double[] ar;

    @Shadow
    private NoiseGeneratorOctaves lperlinNoise1;

    @Shadow
    double[] br;

    @Shadow
    private NoiseGeneratorOctaves lperlinNoise2;

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Random rand;

    @Shadow
    private MapGenNetherBridge genNetherBridge;

    @Shadow
    @Final
    private WorldGenHellLava hellSpringGen;

    @Shadow
    @Final
    private WorldGenFire fireFeature;

    @Shadow
    @Final
    private WorldGenGlowStone1 lightGemGen;

    @Shadow
    @Final
    private WorldGenGlowStone2 hellPortalGen;

    @Shadow
    @Final
    private WorldGenBush brownMushroomFeature;

    @Shadow
    @Final
    private WorldGenBush redMushroomFeature;

    @Shadow
    @Final
    private WorldGenerator quartzGen;

    @Shadow
    @Final
    private WorldGenerator magmaGen;

    @Shadow
    @Final
    private WorldGenHellLava lavaTrapGen;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private double[] getHeights(double[] p_185938_1_, int p_185938_2_, int p_185938_3_, int p_185938_4_, int p_185938_5_, int p_185938_6_, int p_185938_7_) {
        if (p_185938_1_ == null) {
            p_185938_1_ = new double[p_185938_5_ * p_185938_6_ * p_185938_7_];
        }

        net.minecraftforge.event.terraingen.ChunkGeneratorEvent.InitNoiseField event = new net.minecraftforge.event.terraingen.ChunkGeneratorEvent.InitNoiseField(this, p_185938_1_, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, p_185938_6_, p_185938_7_);
        MikuLib.MikuEventBus().post(event);
        if (event.getResult() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
            return event.getNoisefield();

        this.noiseData4 = this.scaleNoise.generateNoiseOctaves(this.noiseData4, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, 1, p_185938_7_, 1.0D, 0.0D, 1.0D);
        this.dr = this.depthNoise.generateNoiseOctaves(this.dr, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, 1, p_185938_7_, 100.0D, 0.0D, 100.0D);
        this.pnr = this.perlinNoise1.generateNoiseOctaves(this.pnr, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, p_185938_6_, p_185938_7_, 8.555150000000001D, 34.2206D, 8.555150000000001D);
        this.ar = this.lperlinNoise1.generateNoiseOctaves(this.ar, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, p_185938_6_, p_185938_7_, 684.412D, 2053.236D, 684.412D);
        this.br = this.lperlinNoise2.generateNoiseOctaves(this.br, p_185938_2_, p_185938_3_, p_185938_4_, p_185938_5_, p_185938_6_, p_185938_7_, 684.412D, 2053.236D, 684.412D);
        int i = 0;
        double[] adouble = new double[p_185938_6_];

        for (int j = 0; j < p_185938_6_; ++j) {
            adouble[j] = Math.cos((double) j * Math.PI * 6.0D / (double) p_185938_6_) * 2.0D;
            double d2 = j;

            if (j > p_185938_6_ / 2) {
                d2 = p_185938_6_ - 1 - j;
            }

            if (d2 < 4.0D) {
                d2 = 4.0D - d2;
                adouble[j] -= d2 * d2 * d2 * 10.0D;
            }
        }

        for (int l = 0; l < p_185938_5_; ++l) {
            for (int i1 = 0; i1 < p_185938_7_; ++i1) {

                for (int k = 0; k < p_185938_6_; ++k) {
                    double d4 = adouble[k];
                    double d5 = this.ar[i] / 512.0D;
                    double d6 = this.br[i] / 512.0D;
                    double d7 = (this.pnr[i] / 10.0D + 1.0D) / 2.0D;
                    double d8;

                    if (d7 < 0.0D) {
                        d8 = d5;
                    } else if (d7 > 1.0D) {
                        d8 = d6;
                    } else {
                        d8 = d5 + (d6 - d5) * d7;
                    }

                    d8 = d8 - d4;

                    if (k > p_185938_6_ - 4) {
                        double d9 = (float) (k - (p_185938_6_ - 4)) / 3.0F;
                        d8 = d8 * (1.0D - d9) + -10.0D * d9;
                    }

                    p_185938_1_[i] = d8;
                    ++i;
                }
            }
        }

        return p_185938_1_;
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;
        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, x, z, false);
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));
        ChunkPos chunkpos = new ChunkPos(x, z);
        this.genNetherBridge.generateStructure(this.world, this.rand, chunkpos);

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA))
            for (int k = 0; k < 8; ++k) {
                this.hellSpringGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.FIRE))
            for (int i1 = 0; i1 < this.rand.nextInt(this.rand.nextInt(10) + 1) + 1; ++i1) {
                this.fireFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.GLOWSTONE)) {
            for (int j1 = 0; j1 < this.rand.nextInt(this.rand.nextInt(10) + 1); ++j1) {
                this.lightGemGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
            }

            for (int k1 = 0; k1 < 10; ++k1) {
                this.hellPortalGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }
        }//Forge: End doGLowstone

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, x, z, false);
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.terraingen.DecorateBiomeEvent.Pre(this.world, this.rand, chunkpos));

        if (net.minecraftforge.event.terraingen.TerrainGen.decorate(this.world, this.rand, chunkpos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.SHROOM)) {
            if (this.rand.nextBoolean()) {
                this.brownMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }

            if (this.rand.nextBoolean()) {
                this.redMushroomFeature.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
            }
        }


        if (net.minecraftforge.event.terraingen.TerrainGen.generateOre(this.world, this.rand, quartzGen, blockpos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.QUARTZ))
            for (int l1 = 0; l1 < 16; ++l1) {
                this.quartzGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), this.rand.nextInt(108) + 10, this.rand.nextInt(16)));
            }

        int i2 = this.world.getSeaLevel() / 2 + 1;

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_MAGMA))
            for (int l = 0; l < 4; ++l) {
                this.magmaGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16), i2 - 5 + this.rand.nextInt(10), this.rand.nextInt(16)));
            }

        if (net.minecraftforge.event.terraingen.TerrainGen.populate(this, this.world, this.rand, x, z, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA2))
            for (int j2 = 0; j2 < 16; ++j2) {
                int offset = net.minecraftforge.common.ForgeModContainer.fixVanillaCascading ? 8 : 0; // MC-117810
                this.lavaTrapGen.generate(this.world, this.rand, blockpos.add(this.rand.nextInt(16) + offset, this.rand.nextInt(108) + 10, this.rand.nextInt(16) + offset));
            }

        biome.decorate(this.world, this.rand, new BlockPos(i, 0, j));

        MikuLib.MikuEventBus().post(new net.minecraftforge.event.terraingen.DecorateBiomeEvent.Post(this.world, this.rand, blockpos));

        BlockFalling.fallInstantly = false;
    }
}
