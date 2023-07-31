package miku.lib.common.item;

import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.UnsafeUtil;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpecialItem extends Item {
    protected static final Map<UUID, SpecialItem> playerList = new ConcurrentHashMap<>();
    protected static final short max_mode = 1;
    protected short mode = 0;

    protected static boolean TimeStop = false;

    public static boolean isTimeStop() {
        return TimeStop;
    }

    public SpecialItem() {
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

    public static boolean isInList(EntityPlayer player){
        return playerList.containsKey(player);
    }

    @Nullable
    public static SpecialItem Get(EntityPlayer player) {
        return playerList.get(player);
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        tooltip.add("§b§o仅供开发者使用");
        tooltip.add("§bHatsuneMiku is the best singer!");
        String s = "§bMode:";
        switch (mode) {
            case 0:
                s += "KillEntity";
                break;
            case 1:
                s += "MemoryFucker";
                break;
            default:
        }
        tooltip.add(s);
        tooltip.add("§fBy mcst12345");
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        if (!hasOwner(stack)) {
            setOwner(stack, player);
            playerList.put(player.getUniqueID(), this);
        } else if (!this.isOwner(stack, player)) {
            EntityUtil.Kill(player);
            return false;
        }
        switch (mode) {
            case 0:
                EntityUtil.Kill(entity);
                break;
            case 1:
                UnsafeUtil.Fuck(entity);
                break;
            default:
        }
        return false;
    }

    @Override
    public int getEntityLifespan(@Nullable ItemStack itemStack, @Nonnull World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
        if (!hasOwner(stack)) {
            setOwner(stack, player);
            playerList.put(player.getUniqueID(), this);
        } else if (!this.isOwner(stack, player)) EntityUtil.Kill(player);
        switch (mode) {
            case 0:
                EntityUtil.Kill(target);
                break;
            case 1:
                UnsafeUtil.Fuck(target);
                break;
            default:
        }
        return true;
    }

    @Override
    public void onUsingTick(@Nonnull ItemStack stack, @Nonnull EntityLivingBase player, int count) {
        if (!(player instanceof EntityPlayer)) return;
        if(!hasOwner(stack)){
            setOwner(stack, (EntityPlayer) player);
            playerList.put(((EntityPlayer) player).getUniqueID(), this);
        }
        else if (!this.isOwner(stack,(EntityPlayer) player))EntityUtil.Kill(player);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if (!hasOwner(stack)) {
                setOwner(stack, player);
                playerList.put(player.getUniqueID(), this);
            } else if (!this.isOwner(stack, player)) {
                EntityUtil.Kill(player);
                return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
            }
        }
        switch (mode) {
            case 0:
                EntityUtil.RangeKill(player, 10000);
                break;
            case 1:
                List<Entity> list = player.getEntityWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(player.posX - 10000, player.posY - 10000, player.posZ - 10000, player.posX + 10000, player.posY + 10000, player.posZ + 10000));
                UnsafeUtil.Fuck(list);
                break;
            default:
        }


        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void onCreated(@Nonnull ItemStack stack, @Nullable World worldIn,@Nonnull EntityPlayer playerIn) {
        if(!hasOwner(stack)){
            setOwner(stack, playerIn);
            playerList.put(playerIn.getUniqueID(), this);
        }
        else if (!this.isOwner(stack, playerIn))EntityUtil.Kill(playerIn);
    }

    public boolean hasOwner(@Nonnull ItemStack stack) {
        return stack.hasTagCompound() && (Objects.requireNonNull(stack.getTagCompound()).hasKey("Owner") || stack.getTagCompound().hasKey("OwnerUUID"));
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) return;
        if (entity instanceof EntityPlayer) {
            if(!hasOwner(stack)) {
                EntityPlayer player = (EntityPlayer) entity;
                setOwner(stack, player);
                playerList.put(player.getUniqueID(), this);
            }
            else if (!this.isOwner(stack,(EntityPlayer) entity))EntityUtil.Kill(entity);
        }
    }

    public boolean isOwner(@Nullable ItemStack stack,@Nonnull EntityPlayer player) {
        if (stack == null) return true;
        if (stack.getTagCompound() == null) return true;
        return stack.getTagCompound().getString("Owner").equals(player.getName()) || stack.getTagCompound().getString("OwnerUUID").equals(player.getUniqueID().toString());
    }

    public void setOwner(ItemStack stack, EntityPlayer player) {
        if (hasOwner(stack)) throw new RuntimeException("Fuck you!");
        stack.setTagInfo("Owner", new NBTTagString(player.getName()));
        stack.setTagInfo("OwnerUUID", new NBTTagString(player.getUniqueID().toString()));
    }

    public void ModeChange() {
        if (mode <= max_mode) {
            mode++;
        } else mode = 0;
    }
}
