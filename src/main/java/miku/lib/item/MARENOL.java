package miku.lib.item;

import miku.lib.api.iWorld;
import miku.lib.effect.MikuEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;



import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MARENOL extends ItemFood {
    public MARENOL() {
        super(0, 0, true);
        this.setCreativeTab(null);
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack,@Nonnull World worldIn,@Nonnull EntityLivingBase entityLiving){
        boolean flag = true;
        for(MikuEffect effect : ((iWorld)entityLiving.world).GetEntityEffects(entityLiving)){
            if(effect instanceof miku.lib.effect.MARENOL ) {
                effect.level_up();
                flag = false;
                break;
            }
        }
        if(flag)((iWorld)entityLiving.world).AddEffect(new miku.lib.effect.MARENOL(entityLiving,1));
        stack.shrink(1);
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, EntityPlayer playerIn,@Nonnull EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    @Nonnull
    public ItemFood setPotionEffect(@Nullable PotionEffect effect, float probability){return this;}
}
