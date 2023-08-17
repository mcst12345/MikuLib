package miku.lib.mixins.minecraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemEnchantedBook.class)
public abstract class MixinItemEnchantedBook extends Item {
    @Shadow
    public static ItemStack getEnchantedItemStack(EnchantmentData p_92111_0_) {
        return null;
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH) {
            for (Object o : Enchantment.REGISTRY) {
                if (!(o instanceof Enchantment)) continue;
                Enchantment enchantment = (Enchantment) o;
                if (enchantment.type != null) {
                    for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
                        items.add(getEnchantedItemStack(new EnchantmentData(enchantment, i)));
                    }
                }
            }
        } else if (tab.getRelevantEnchantmentTypes().length != 0) {
            for (Enchantment enchantment1 : Enchantment.REGISTRY) {
                if (tab.hasRelevantEnchantmentType(enchantment1.type)) {
                    items.add(getEnchantedItemStack(new EnchantmentData(enchantment1, enchantment1.getMaxLevel())));
                }
            }
        }
    }
}
