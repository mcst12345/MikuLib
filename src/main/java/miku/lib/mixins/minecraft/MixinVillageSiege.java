package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

@Mixin(value = VillageSiege.class)
public abstract class MixinVillageSiege {
    @Shadow
    @Final
    private World world;

    @Shadow
    private Village village;

    @Shadow
    private int spawnX;

    @Shadow
    private int spawnY;

    @Shadow
    private int spawnZ;

    @Shadow
    @Nullable
    protected abstract Vec3d findRandomSpawnPos(BlockPos pos);

    @Shadow
    private int nextSpawnTime;

    @Shadow
    private int siegeCount;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean trySetupSiege() {
        List<EntityPlayer> list = this.world.playerEntities;
        Iterator<EntityPlayer> iterator = list.iterator();

        while (true) {
            if (!iterator.hasNext()) {
                return false;
            }

            EntityPlayer entityplayer = iterator.next();

            if (!entityplayer.isSpectator()) {
                this.village = this.world.getVillageCollection().getNearestVillage(new BlockPos(entityplayer), 1);

                if (this.village != null && this.village.getNumVillageDoors() >= 10 && this.village.getTicksSinceLastDoorAdding() >= 20 && this.village.getNumVillagers() >= 20) {
                    BlockPos blockpos = this.village.getCenter();
                    float f = (float) this.village.getVillageRadius();
                    boolean flag = false;

                    for (int i = 0; i < 10; ++i) {
                        float f1 = this.world.rand.nextFloat() * ((float) Math.PI * 2F);
                        this.spawnX = blockpos.getX() + (int) ((double) (MathHelper.cos(f1) * f) * 0.9D);
                        this.spawnY = blockpos.getY();
                        this.spawnZ = blockpos.getZ() + (int) ((double) (MathHelper.sin(f1) * f) * 0.9D);
                        flag = false;

                        for (Village village : this.world.getVillageCollection().getVillageList()) {
                            if (village != this.village && village.isBlockPosWithinSqVillageRadius(new BlockPos(this.spawnX, this.spawnY, this.spawnZ))) {
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            break;
                        }
                    }

                    if (flag) {
                        return false;
                    }

                    Vec3d vec3d = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));

                    if (vec3d != null) {
                        if (MikuLib.MikuEventBus().post(new net.minecraftforge.event.village.VillageSiegeEvent((VillageSiege) (Object) this, world, entityplayer, village, vec3d)))
                            return false;
                        break;
                    }
                }
            }
        }

        this.nextSpawnTime = 0;
        this.siegeCount = 20;
        return true;
    }
}
