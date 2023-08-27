package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = TextComponentBase.class)
public class MixinTextComponentBase {
    @Shadow
    private Style style;

    @Shadow
    protected List<ITextComponent> siblings;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public final String getUnformattedText() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        StringBuilder stringbuilder = new StringBuilder();

        for (ITextComponent itextcomponent : (TextComponentBase) (Object) this) {
            stringbuilder.append(itextcomponent.getUnformattedComponentText());
        }

        return stringbuilder.toString();
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public final String getFormattedText() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        StringBuilder stringbuilder = new StringBuilder();

        for (ITextComponent itextcomponent : (TextComponentBase) (Object) this) {
            String s = itextcomponent.getUnformattedComponentText();

            if (!s.isEmpty()) {
                stringbuilder.append(itextcomponent.getStyle().getFormattingCode());
                stringbuilder.append(s);
                stringbuilder.append(TextFormatting.RESET);
            }
        }

        return stringbuilder.toString();
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String toString() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
    }
}
