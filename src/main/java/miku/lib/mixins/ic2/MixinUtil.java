package miku.lib.mixins.ic2;

import ic2.core.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Util.class)
public class MixinUtil {
    /**
     * @author mcst12345
     * @reason No you cannot exit
     */
    @Overwrite
    public static void exit(int status) {
    }
}
