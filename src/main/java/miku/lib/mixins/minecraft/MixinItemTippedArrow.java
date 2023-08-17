package miku.lib.mixins.minecraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ItemTippedArrow.class)
public abstract class MixinItemTippedArrow extends Item {
    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (Object o : PotionType.REGISTRY) {
                if (!(o instanceof PotionType)) continue;
                PotionType potiontype = (PotionType) o;
                if (!potiontype.getEffects().isEmpty()) {
                    items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potiontype));
                }
            }
        }
    }
}
