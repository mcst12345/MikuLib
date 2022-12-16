package miku.lib.item;

import miku.lib.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpecialItem extends Item {
    protected static List<EntityPlayer> playerList = new ArrayList<>();
    protected static HashMap<EntityPlayer,SpecialItem> list = new HashMap<>();

    protected static boolean TimeStop = false;
    public static boolean isTimeStop() {
        return TimeStop;
    }
    protected EntityPlayer owner = null;

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
        if(owner == null) {
            owner = player;
            playerList.add(player);
            list.put(player,this);
        }
        else if(owner!=player){
            EntityUtil.Kill(player);
        }
        EntityUtil.Kill(entity);
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
        if(owner == null) {
            owner = player;
            playerList.add(player);
            list.put(player,this);
        }
        else if(owner!=player){
            EntityUtil.Kill(player);
        }
        EntityUtil.Kill(target);
        return true;
    }

    public boolean hasOwner(){
        return owner!=null;
    }

    public boolean isOwner(EntityPlayer player){
        if(!hasOwner()){
            owner=player;
            playerList.add(player);
            list.put(player,this);
            return true;
        }
        return owner.getName().equals(player.getName())&&owner.getUniqueID()==player.getUniqueID();
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
        if (owner == null) {
            owner = (EntityPlayer) player;
            playerList.add((EntityPlayer) player);
            list.put((EntityPlayer) player,this);
        } else if(owner!=player)EntityUtil.Kill(player);
    }

    @Override
    public int getEntityLifespan(@Nullable ItemStack itemStack, @Nonnull World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        if (!world.isRemote) {
            if (owner == null) {
                owner = player;
                playerList.add(player);
                list.put(player,this);
            }
            else if (owner!=player)EntityUtil.Kill(player);
        }
        EntityUtil.RangeKill(player, 10000);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public void onCreated(@Nullable ItemStack stack, @Nullable World worldIn,@Nonnull EntityPlayer playerIn) {
        if (owner == null) {
            owner = playerIn;
            playerList.add(playerIn);
            list.put(playerIn,this);
        }
        else if (owner!=playerIn)EntityUtil.Kill(playerIn);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote) return;
        if (entity instanceof EntityPlayer) {
            if (owner == null) {
                owner = (EntityPlayer) entity;
                playerList.add((EntityPlayer) entity);
                list.put((EntityPlayer) entity,this);
            }
            else if (owner != entity)EntityUtil.Kill(entity);
        }
    }

    public static boolean isInList(EntityPlayer player){
        return playerList.contains(player);
    }

    public static SpecialItem GetItem(EntityPlayer player){
        if(list.get(player)!=null)return list.get(player);
        else throw new RuntimeException("WTF?");
    }
}
