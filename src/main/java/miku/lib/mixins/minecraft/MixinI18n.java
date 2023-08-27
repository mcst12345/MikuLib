package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = I18n.class)
public class MixinI18n {
    @Shadow
    @Final
    private static LanguageMap localizedName;

    @Shadow
    @Final
    private static LanguageMap fallbackTranslator;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    @Deprecated
    public static String translateToLocal(String key) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return localizedName.translateKey(key);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    @Deprecated
    public static String translateToLocalFormatted(String key, Object... format) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return localizedName.translateKeyFormat(key, format);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    @Deprecated
    public static String translateToFallback(String key) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return fallbackTranslator.translateKey(key);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    @Deprecated
    public static boolean canTranslate(String key) {
        if (MikuLib.isLAIN()) {
            return true;
        }
        return localizedName.isKeyTranslated(key);
    }

}
