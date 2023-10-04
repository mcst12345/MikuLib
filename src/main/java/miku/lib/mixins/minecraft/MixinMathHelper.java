package miku.lib.mixins.minecraft;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = MathHelper.class)
public class MixinMathHelper {
    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public static float cos(float value) {
        return (float) Math.cos(value);
    }
}
