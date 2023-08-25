package miku.lib.mixins.minecraft;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(value = WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner {
    @Shadow
    @Final
    private Set<ChunkPos> eligibleChunksForSpawning;

    @Shadow
    @Final
    private static int MOB_COUNT_DIV;

    @Shadow
    private static BlockPos getRandomChunkPosition(World worldIn, int x, int z) {
        return null;
    }

    @Shadow
    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType spawnPlacementTypeIn, World worldIn, BlockPos pos) {
        return false;
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return 0;
        } else {
            this.eligibleChunksForSpawning.clear();
            int i = 0;

            for (EntityPlayer entityplayer : worldServerIn.playerEntities) {
                if (!entityplayer.isSpectator()) {
                    int j = MathHelper.floor(entityplayer.posX / 16.0D);
                    int k = MathHelper.floor(entityplayer.posZ / 16.0D);

                    for (int i1 = -8; i1 <= 8; ++i1) {
                        for (int j1 = -8; j1 <= 8; ++j1) {
                            boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                            ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

                            if (!this.eligibleChunksForSpawning.contains(chunkpos)) {
                                ++i;

                                if (!flag && worldServerIn.getWorldBorder().contains(chunkpos)) {
                                    PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap().getEntry(chunkpos.x, chunkpos.z);

                                    if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
                                        this.eligibleChunksForSpawning.add(chunkpos);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int j4 = 0;
            BlockPos blockpos1 = worldServerIn.getSpawnPoint();

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.values()) {
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnOnSetTickRate)) {
                    int k4 = worldServerIn.countEntities(enumcreaturetype, true);
                    int l4 = enumcreaturetype.getMaxNumberOfCreature() * i / MOB_COUNT_DIV;

                    if (k4 <= l4) {
                        java.util.ArrayList<ChunkPos> shuffled = com.google.common.collect.Lists.newArrayList(this.eligibleChunksForSpawning);
                        java.util.Collections.shuffle(shuffled);
                        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
                        label134:

                        for (ChunkPos chunkpos1 : shuffled) {
                            BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkpos1.x, chunkpos1.z);
                            if (blockpos == null) continue;
                            int k1 = blockpos.getX();
                            int l1 = blockpos.getY();
                            int i2 = blockpos.getZ();
                            IBlockState iblockstate = worldServerIn.getBlockState(blockpos);

                            if (!iblockstate.isNormalCube()) {
                                int j2 = 0;

                                for (int k2 = 0; k2 < 3; ++k2) {
                                    int l2 = k1;
                                    int j3 = i2;
                                    Biome.SpawnListEntry biome$spawnlistentry = null;
                                    IEntityLivingData ientitylivingdata = null;
                                    int l3 = MathHelper.ceil(Math.random() * 4.0D);

                                    for (int i4 = 0; i4 < l3; ++i4) {
                                        l2 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        worldServerIn.rand.nextInt(1);
                                        worldServerIn.rand.nextInt(1);
                                        j3 += worldServerIn.rand.nextInt(6) - worldServerIn.rand.nextInt(6);
                                        blockpos$mutableblockpos.setPos(l2, l1, j3);
                                        float f = (float) l2 + 0.5F;
                                        float f1 = (float) j3 + 0.5F;

                                        if (!worldServerIn.isAnyPlayerWithinRangeAt(f, l1, f1, 24.0D) && blockpos1.distanceSq(f, l1, f1) >= 576.0D) {
                                            if (biome$spawnlistentry == null) {
                                                biome$spawnlistentry = worldServerIn.getSpawnListEntryForTypeAt(enumcreaturetype, blockpos$mutableblockpos);

                                                if (biome$spawnlistentry == null) {
                                                    break;
                                                }
                                            }

                                            if (worldServerIn.canCreatureTypeSpawnHere(enumcreaturetype, biome$spawnlistentry, blockpos$mutableblockpos) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(biome$spawnlistentry.entityClass), worldServerIn, blockpos$mutableblockpos)) {
                                                EntityLiving entityliving;

                                                try {
                                                    entityliving = biome$spawnlistentry.newInstance(worldServerIn);
                                                } catch (Exception exception) {
                                                    exception.printStackTrace();
                                                    return j4;
                                                }

                                                entityliving.setLocationAndAngles(f, l1, f1, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);

                                                net.minecraftforge.fml.common.eventhandler.Event.Result canSpawn = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(entityliving, worldServerIn, f, l1, f1, false);
                                                if (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW || (canSpawn == net.minecraftforge.fml.common.eventhandler.Event.Result.DEFAULT && (entityliving.getCanSpawnHere() && entityliving.isNotColliding()))) {
                                                    if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(entityliving, worldServerIn, f, l1, f1))
                                                        ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);

                                                    if (entityliving.isNotColliding()) {
                                                        ++j2;
                                                        worldServerIn.spawnEntity(entityliving);
                                                    } else {
                                                        entityliving.setDead();
                                                    }

                                                    if (j2 >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(entityliving)) {
                                                        continue label134;
                                                    }
                                                }

                                                j4 += j2;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return j4;
        }
    }
}
