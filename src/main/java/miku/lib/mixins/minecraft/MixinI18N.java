package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = I18n.class)
public class MixinI18N {
    @Shadow
    private static Locale i18nLocale;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public static String format(String translateKey, Object... parameters) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return i18nLocale.formatMessage(translateKey, parameters);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public static boolean hasKey(String key) {
        if (MikuLib.isLAIN()) {
            return true;
        }
        return i18nLocale.hasKey(key);
    }
}
