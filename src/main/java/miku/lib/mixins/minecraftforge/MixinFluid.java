package miku.lib.mixins.minecraftforge;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Fluid.class, remap = false)
public class MixinFluid {

    @Shadow
    protected String unlocalizedName;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getLocalizedName(FluidStack stack) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        String s = this.getUnlocalizedName();
        return s == null ? "" : I18n.translateToLocal(s);
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getUnlocalizedName(FluidStack stack) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.getUnlocalizedName();
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getUnlocalizedName() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return "fluid." + this.unlocalizedName;
    }
}
