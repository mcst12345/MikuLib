package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;

@Mixin(value = TextComponentTranslation.class)
public abstract class MixinTextComponentTranslation extends TextComponentBase {
    @Shadow
    abstract void ensureInitialized();

    @Shadow
    List<ITextComponent> children;

    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private Object[] formatArgs;

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
        this.ensureInitialized();
        StringBuilder stringbuilder = new StringBuilder();

        for (ITextComponent itextcomponent : this.children) {
            stringbuilder.append(itextcomponent.getUnformattedComponentText());
        }

        return stringbuilder.toString();
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @NotNull
    @Overwrite
    public String toString() {
        return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.formatArgs) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getKey() {
        return this.key;
    }
}
