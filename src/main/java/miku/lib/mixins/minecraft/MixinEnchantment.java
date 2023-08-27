package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Enchantment.class)
public abstract class MixinEnchantment extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<Enchantment> {

    @Shadow
    public abstract boolean isCurse();

    @Shadow
    public abstract int getMaxLevel();

    @Shadow
    protected String name;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getTranslatedName(int level) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        String s = I18n.translateToLocal(this.getName());

        if (this.isCurse()) {
            s = TextFormatting.RED + s;
        }

        return level == 1 && this.getMaxLevel() == 1 ? s : s + " " + I18n.translateToLocal("enchantment.level." + level);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getName() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return "enchantment." + this.name;
    }
}
