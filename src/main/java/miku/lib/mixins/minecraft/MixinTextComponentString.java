package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TextComponentString.class)
public abstract class MixinTextComponentString extends TextComponentBase {
    @Shadow
    @Final
    private String text;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getText() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.text;
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
        return this.text;
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
        return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
