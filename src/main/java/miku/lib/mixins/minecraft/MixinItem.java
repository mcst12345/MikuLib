package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Item.class)
public abstract class MixinItem {

    @Shadow
    public abstract String getTranslationKey(ItemStack stack);

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return I18n.translateToLocal(this.getTranslationKey(stack));
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getItemStackDisplayName(ItemStack stack) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }
}
