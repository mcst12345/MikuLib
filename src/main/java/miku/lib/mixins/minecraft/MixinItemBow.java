package miku.lib.mixins.minecraft;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemBow.class)
public abstract class MixinItemBow extends Item {
    @Shadow
    protected abstract ItemStack findAmmo(EntityPlayer player);


    /**
     * @author mcst12345
     * @reason infinity
     */
    @NotNull
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, EntityPlayer playerIn, @NotNull EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        boolean flag = !this.findAmmo(playerIn).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.capabilities.isCreativeMode && !flag && !isInfinity(itemstack)) {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
    }

    private static boolean isInfinity(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBow)) {
            return false;
        }
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
    }
}
