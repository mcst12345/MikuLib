package miku.lib.mixins.minecraft;

import miku.lib.common.api.iWorld;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = BlockFalling.class)
public abstract class MixinBlockFalling {
    @Shadow
    public abstract int tickRate(World worldIn);


    @Shadow
    public static boolean canFallThrough(IBlockState state) {
        return false;
    }

    @Shadow
    public static boolean fallInstantly;

    @Shadow
    protected abstract void onStartFalling(EntityFallingBlock fallingEntity);

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (((iWorld) worldIn).isTimeStop() || TimeStopUtil.isTimeStop()) return;
        worldIn.scheduleUpdate(pos, (BlockFalling) (Object) this, this.tickRate(worldIn));
    }

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (((iWorld) worldIn).isTimeStop() || TimeStopUtil.isTimeStop()) return;
        if (!worldIn.isRemote) {
            this.checkFallable(worldIn, pos);
        }
    }

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    private void checkFallable(World worldIn, BlockPos pos) {
        if (((iWorld) worldIn).isTimeStop() || TimeStopUtil.isTimeStop()) return;
        if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0) {

            if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                if (!worldIn.isRemote) {
                    EntityFallingBlock entityfallingblock = new EntityFallingBlock(worldIn, (double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D, worldIn.getBlockState(pos));
                    this.onStartFalling(entityfallingblock);
                    worldIn.spawnEntity(entityfallingblock);
                }
            } else {
                IBlockState state = worldIn.getBlockState(pos);
                worldIn.setBlockToAir(pos);
                BlockPos blockpos;

                for (blockpos = pos.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) && blockpos.getY() > 0; blockpos = blockpos.down()) {
                }

                if (blockpos.getY() > 0) {
                    worldIn.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
                }
            }
        }
    }
}
