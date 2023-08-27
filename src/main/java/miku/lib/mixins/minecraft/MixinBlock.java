package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.block.Block;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Block.class)
public abstract class MixinBlock {
    @Shadow
    public abstract String getTranslationKey();

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getLocalizedName() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return I18n.translateToLocal(this.getTranslationKey() + ".name");
    }
}
