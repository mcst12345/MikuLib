package miku.lib.item;

import miku.lib.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpecialItem extends Item {
    protected static final List<EntityPlayer> playerList = new ArrayList<>();

    protected static boolean TimeStop = false;
    public static boolean isTimeStop() {
        return TimeStop;
    }

    public SpecialItem(){
        this.setMaxStackSize(1);
        this.setTranslationKey("hidden");
    }

    public static void SetTimeStop() {
        TimeStop=!TimeStop;
    }

    @Override
    public void setDamage(@Nonnull ItemStack stack, int damage) {
        super.setDamage(stack, 0);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        return 0.0F;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState blockIn) {
        return true;
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        if(!hasOwner(stack)){
            setOwner(stack, player);
            playerList.add(player);
        }
        else if (!this.isOwner(stack, player)) {
            EntityUtil.Kill(player);
            return false;
        }
        EntityUtil.Kill(entity);
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
        if(!hasOwner(stack)){
            setOwner(stack, player);
            playerList.add(player);
        }
        else if (!this.isOwner(stack, player))EntityUtil.Kill(player);
        EntityUtil.Kill(target);
        return true;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add("§b§o仅供开发者使用");
        tooltip.add("§bHatsuneMiku is the best singer!");
        tooltip.add("§fBy mcst12345");
    }

    @Override
    public void onUsingTick(@Nonnull ItemStack stack, @Nonnull EntityLivingBase player, int count) {
        if (!(player instanceof EntityPlayer)) return;
        if(!hasOwner(stack)){
            setOwner(stack, (EntityPlayer) player);
            playerList.add((EntityPlayer) player);
        }
        else if (!this.isOwner(stack,(EntityPlayer) player))EntityUtil.Kill(player);
    }

    @Override
    public int getEntityLifespan(@Nullable ItemStack itemStack, @Nonnull World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if(!hasOwner(stack)){
                setOwner(stack, player);
                playerList.add(player);
            }
            else if (!this.isOwner(stack, player)) {
                EntityUtil.Kill(player);
                return new ActionResult<>(EnumActionResult.FAIL,player.getHeldItem(hand));
            }
        }
        EntityUtil.RangeKill(player, 10000);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void onCreated(@Nonnull ItemStack stack, @Nullable World worldIn,@Nonnull EntityPlayer playerIn) {
        if(!hasOwner(stack)){
            setOwner(stack, playerIn);
            playerList.add(playerIn);
        }
        else if (!this.isOwner(stack, playerIn))EntityUtil.Kill(playerIn);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) return;
        if (entity instanceof EntityPlayer) {
            if(!hasOwner(stack)){
                setOwner(stack, (EntityPlayer) entity);
                playerList.add((EntityPlayer) entity);
            }
            else if (!this.isOwner(stack,(EntityPlayer) entity))EntityUtil.Kill(entity);
        }
    }

    public static boolean isInList(EntityPlayer player){
        for(EntityPlayer PLAYER : playerList){
            if(PLAYER.getUniqueID() == player.getUniqueID() && (player.getGameProfile()==null || PLAYER.getName().equals(player.getName())))return true;
        }
        return false;
    }

    public boolean hasOwner(@Nonnull ItemStack stack) {
        return stack.hasTagCompound() && (Objects.requireNonNull(stack.getTagCompound()).hasKey("Owner") || stack.getTagCompound().hasKey("OwnerUUID"));
    }

    public boolean isOwner(@Nullable ItemStack stack,@Nonnull EntityPlayer player) {
        if(stack == null)return true;
        if(stack.getTagCompound()==null)return true;
        return stack.getTagCompound().getString("Owner").equals(player.getName()) || stack.getTagCompound().getString("OwnerUUID").equals(player.getUniqueID().toString());
    }

    public void setOwner(ItemStack stack, EntityPlayer player) {
        if(hasOwner(stack))throw new RuntimeException("Fuck you!");
        stack.setTagInfo("Owner", new NBTTagString(player.getName()));
        stack.setTagInfo("OwnerUUID", new NBTTagString(player.getUniqueID().toString()));
    }
}
