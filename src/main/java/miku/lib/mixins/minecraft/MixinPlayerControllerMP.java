package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    @Shadow
    private GameType currentGameType;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private BlockPos currentBlock;

    @Shadow
    @Final
    private NetHandlerPlayClient connection;

    @Shadow
    private int blockHitDelay;

    @Shadow
    private boolean isHittingBlock;

    @Shadow
    protected abstract boolean isHittingPosition(BlockPos pos);

    @Shadow
    private float curBlockDamageMP;

    @Shadow
    private ItemStack currentItemHittingBlock;

    @Shadow
    private float stepSoundTickCounter;

    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public float getBlockReachDistance() {
        if (EntityUtil.isProtected(mc)) {
            return Float.MAX_VALUE;
        }
        try {
            float attrib = (float) mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            return this.currentGameType.isCreative() ? attrib : attrib - 0.5F;
        } catch (Throwable t) {
            t.printStackTrace();
            return Float.MAX_VALUE;
        }
    }

    @Shadow
    protected abstract void syncCurrentPlayItem();

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing) {
        if (!((iMinecraft) mcIn).MikuWorld().extinguishFire(mcIn.player, pos, facing)) {
            playerController.onPlayerDestroyBlock(pos);
        }
    }

    /**
     * @author mcst12345
     * @reason Shit!
     */
    @Overwrite
    public boolean onPlayerDestroyBlock(BlockPos pos) {
        if (this.currentGameType.hasLimitedInteractions()) {
            if (this.currentGameType == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(((iMinecraft) this.mc).MikuWorld().getBlockState(pos).getBlock())) {
                    return false;
                }
            }
        }

        ItemStack stack = mc.player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem().onBlockStartBreak(stack, pos, mc.player)) {
            return false;
        }

        if (this.currentGameType.isCreative() && !stack.isEmpty() && !stack.getItem().canDestroyBlockInCreative(((iMinecraft) this.mc).MikuWorld(), pos, stack, mc.player)) {
            return false;
        } else {
            World world = ((iMinecraft) this.mc).MikuWorld();
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.mc.player.canUseCommandBlock()) {
                return false;
            } else if (iblockstate.getMaterial() == Material.AIR) {
                return false;
            } else {
                world.playEvent(2001, pos, Block.getStateId(iblockstate));

                this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());

                if (!this.currentGameType.isCreative()) {
                    ItemStack itemstack1 = this.mc.player.getHeldItemMainhand();
                    ItemStack copyBeforeUse = itemstack1.copy();

                    if (!itemstack1.isEmpty()) {
                        itemstack1.onBlockDestroyed(world, iblockstate, pos, this.mc.player);

                        if (itemstack1.isEmpty()) {
                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(this.mc.player, copyBeforeUse, EnumHand.MAIN_HAND);
                            this.mc.player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }

                boolean flag = block.removedByPlayer(iblockstate, world, pos, mc.player, false);

                if (flag) {
                    block.onPlayerDestroy(world, pos, iblockstate);
                }
                return flag;
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        if (this.currentGameType.hasLimitedInteractions()) {
            if (this.currentGameType == GameType.SPECTATOR) {
                return false;
            }

            if (!this.mc.player.isAllowEdit()) {
                ItemStack itemstack = this.mc.player.getHeldItemMainhand();

                if (itemstack.isEmpty()) {
                    return false;
                }

                if (!itemstack.canDestroy(((iMinecraft) (this.mc)).MikuWorld().getBlockState(loc).getBlock())) {
                    return false;
                }
            }
        }

        if (!((iMinecraft) (this.mc)).MikuWorld().getWorldBorder().contains(loc)) {
            return false;
        } else {
            if (this.currentGameType.isCreative()) {
                this.mc.getTutorial().onHitBlock(((iMinecraft) (this.mc)).MikuWorld(), loc, ((iMinecraft) (this.mc)).MikuWorld().getBlockState(loc), 1.0F);
                this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1)).isCanceled())
                    clickBlockCreative(this.mc, (PlayerControllerMP) (Object) this, loc, face);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
                if (this.isHittingBlock) {
                    this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
                }
                net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(this.mc.player, getBlockReachDistance() + 1));

                IBlockState iblockstate = ((iMinecraft) (this.mc)).MikuWorld().getBlockState(loc);
                this.mc.getTutorial().onHitBlock(((iMinecraft) (this.mc)).MikuWorld(), loc, iblockstate, 0.0F);
                this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
                boolean flag = iblockstate.getMaterial() != Material.AIR;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        iblockstate.getBlock().onBlockClicked(((iMinecraft) (this.mc)).MikuWorld(), loc, this.mc.player);
                }

                if (event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) return true;
                if (flag && iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, loc) >= 1.0F) {
                    this.onPlayerDestroyBlock(loc);
                } else {
                    this.isHittingBlock = true;
                    this.currentBlock = loc;
                    this.currentItemHittingBlock = this.mc.player.getHeldItemMainhand();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    ((iMinecraft) (this.mc)).MikuWorld().sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }

            return true;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            this.mc.getTutorial().onHitBlock(((iMinecraft) (this.mc)).MikuWorld(), this.currentBlock, ((iMinecraft) (this.mc)).MikuWorld().getBlockState(this.currentBlock), -1.0F);
            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
            this.isHittingBlock = false;
            this.curBlockDamageMP = 0.0F;
            ((iMinecraft) (this.mc)).MikuWorld().sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
            this.mc.player.resetCooldown();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
            return true;
        } else if (this.currentGameType.isCreative() && ((iMinecraft) (this.mc)).MikuWorld().getWorldBorder().contains(posBlock)) {
            this.blockHitDelay = 5;
            this.mc.getTutorial().onHitBlock(((iMinecraft) (this.mc)).MikuWorld(), posBlock, ((iMinecraft) (this.mc)).MikuWorld().getBlockState(posBlock), 1.0F);
            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
            clickBlockCreative(this.mc, (PlayerControllerMP) (Object) this, posBlock, directionFacing);
            return true;
        } else if (this.isHittingPosition(posBlock)) {
            IBlockState iblockstate = ((iMinecraft) (this.mc)).MikuWorld().getBlockState(posBlock);
            Block block = iblockstate.getBlock();

            if (iblockstate.getMaterial() == Material.AIR) {
                this.isHittingBlock = false;
                return false;
            } else {
                this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, posBlock);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    SoundType soundtype = block.getSoundType(iblockstate, ((iMinecraft) mc).MikuWorld(), posBlock, mc.player);
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
                }

                ++this.stepSoundTickCounter;
                this.mc.getTutorial().onHitBlock(((iMinecraft) (this.mc)).MikuWorld(), posBlock, iblockstate, MathHelper.clamp(this.curBlockDamageMP, 0.0F, 1.0F));

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
                    this.onPlayerDestroyBlock(posBlock);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                ((iMinecraft) (this.mc)).MikuWorld().sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
                return true;
            }
        } else {
            return this.clickBlock(posBlock, directionFacing);
        }
    }

    /**
     * @author mcst12345
     * @reason HolyFuck!
     */
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {
        this.syncCurrentPlayItem();
        ItemStack itemstack = player.getHeldItem(hand);
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());
        boolean flag = false;

        if (!((iMinecraft) (this.mc)).MikuWorld().getWorldBorder().contains(pos)) {
            return EnumActionResult.FAIL;
        } else {
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(player, hand, pos, direction, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, getBlockReachDistance() + 1));
            if (event.isCanceled()) {
                // Give the server a chance to fire event as well. That way server event is not dependent on client event.
                this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
                return event.getCancellationResult();
            }
            EnumActionResult result = EnumActionResult.PASS;

            if (this.currentGameType != GameType.SPECTATOR) {
                EnumActionResult ret = itemstack.onItemUseFirst(player, worldIn, pos, hand, direction, f, f1, f2);
                if (ret != EnumActionResult.PASS) {
                    // The server needs to process the item use as well. Otherwise, onItemUseFirst won't ever be called on the server without causing weird bugs
                    this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
                    return ret;
                }

                IBlockState iblockstate = worldIn.getBlockState(pos);
                boolean bypass = player.getHeldItemMainhand().doesSneakBypassUse(worldIn, pos, player) && player.getHeldItemOffhand().doesSneakBypassUse(worldIn, pos, player);

                if ((!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW)) {
                    if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                        flag = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, direction, f, f1, f2);
                    if (flag) result = EnumActionResult.SUCCESS;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock) {
                    ItemBlock itemblock = (ItemBlock) itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, pos, direction, player, itemstack)) {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));

            if (!flag && this.currentGameType != GameType.SPECTATOR || event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                if (itemstack.isEmpty()) {
                    return EnumActionResult.PASS;
                } else if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
                    return EnumActionResult.PASS;
                } else {
                    if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                        Block block = ((ItemBlock) itemstack.getItem()).getBlock();

                        if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                            return EnumActionResult.FAIL;
                        }
                    }

                    if (this.currentGameType.isCreative()) {
                        int i = itemstack.getMetadata();
                        int j = itemstack.getCount();
                        if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                            EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
                            itemstack.setItemDamage(i);
                            itemstack.setCount(j);
                            return enumactionresult;
                        } else return result;
                    } else {
                        ItemStack copyForUse = itemstack.copy();
                        if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                            result = itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
                        if (itemstack.isEmpty())
                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyForUse, hand);
                        return result;
                    }
                }
            } else {
                return EnumActionResult.SUCCESS;
            }
        }
    }
}
