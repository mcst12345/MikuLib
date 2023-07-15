package miku.lib.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class MARENOL extends ItemFood {
    public MARENOL() {
        super(0, 0, true);
        this.setCreativeTab(null);
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack,@Nonnull World worldIn,@Nonnull EntityLivingBase entityLiving){
        if(entityLiving instanceof EntityPlayer){

        }
        stack.shrink(1);
        return stack;
    }
}
