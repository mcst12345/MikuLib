package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentKeybind;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TextComponentKeybind.class)
public abstract class MixinTextComponentKeybind extends TextComponentBase {
    @Shadow
    @Final
    private String keybind;

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
        return "KeybindComponent{keybind='" + this.keybind + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getKeybind() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.keybind;
    }
}
