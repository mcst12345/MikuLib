package miku.lib.mixins.minecraft;

import com.google.common.base.Optional;
import miku.lib.common.core.MikuLib;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityShulker.class)
public abstract class MixinEntityShulker extends EntityGolem implements IMob {
    @Shadow
    @Final
    protected static DataParameter<EnumFacing> ATTACHED_FACE;

    @Shadow
    @Final
    protected static DataParameter<Optional<BlockPos>> ATTACHED_BLOCK_POS;

    @Shadow
    @Final
    protected static DataParameter<Byte> PEEK_TICK;

    public MixinEntityShulker(World worldIn) {
        super(worldIn);
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public boolean tryTeleportToNewPosition() {
        if (!this.isAIDisabled() && this.isEntityAlive()) {
            BlockPos blockpos = new BlockPos(this);

            for (int i = 0; i < 5; ++i) {
                BlockPos blockpos1 = blockpos.add(8 - this.rand.nextInt(17), 8 - this.rand.nextInt(17), 8 - this.rand.nextInt(17));

                if (blockpos1.getY() > 0 && this.world.isAirBlock(blockpos1) && this.world.isInsideWorldBorder(this) && this.world.getCollisionBoxes(this, new AxisAlignedBB(blockpos1)).isEmpty()) {
                    boolean flag = false;

                    for (EnumFacing enumfacing : EnumFacing.values()) {
                        if (this.world.isBlockNormalCube(blockpos1.offset(enumfacing), false)) {
                            this.dataManager.set(ATTACHED_FACE, enumfacing);
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(this, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), 0);
                        if (MikuLib.MikuEventBus().post(event)) flag = false;
                        blockpos1 = new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                    }

                    if (flag) {
                        this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0F, 1.0F);
                        this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(blockpos1));
                        this.dataManager.set(PEEK_TICK, (byte) 0);
                        this.setAttackTarget(null);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return true;
        }
    }
}
