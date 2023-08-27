package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.client.resources.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Language.class)
public class MixinLanguage {

    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private String region;

    @Shadow
    @Final
    private String languageCode;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String toString() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return String.format("%s (%s)", this.name, this.region);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getLanguageCode() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.languageCode;
    }
}
