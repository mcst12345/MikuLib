package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentSelector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextComponentSelector.class)
public abstract class MixinTextComponentSelector extends TextComponentBase {
    @Shadow
    @Final
    private String selector;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getSelector() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.selector;
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @NotNull
    @Overwrite
    public String getUnformattedComponentText() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.selector;
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @NotNull
    @Overwrite
    public String toString() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return "SelectorComponent{pattern='" + this.selector + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
