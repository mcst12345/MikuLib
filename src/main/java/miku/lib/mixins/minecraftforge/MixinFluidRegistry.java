package miku.lib.mixins.minecraftforge;

import com.google.common.collect.BiMap;
import miku.lib.common.core.MikuLib;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = FluidRegistry.class, remap = false)
public abstract class MixinFluidRegistry {
    @Shadow
    static BiMap<String, Fluid> masterFluidReference;

    @Shadow
    private static String uniqueName(Fluid fluid) {
        return null;
    }

    @Shadow
    static Map<Fluid, FluidRegistry.FluidDelegate> delegates;

    @Shadow
    static BiMap<String, Fluid> fluids;

    @Shadow
    static int maxID;

    @Shadow
    static BiMap<Fluid, Integer> fluidIDs;

    @Shadow
    static BiMap<Integer, String> fluidNames;

    @Shadow
    static BiMap<String, String> defaultFluidName;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static boolean registerFluid(Fluid fluid) {
        masterFluidReference.put(uniqueName(fluid), fluid);
        delegates.put(fluid, new FluidRegistry.FluidDelegate(fluid, fluid.getName()));
        if (fluids.containsKey(fluid.getName())) {
            return false;
        }
        fluids.put(fluid.getName(), fluid);
        maxID++;
        fluidIDs.put(fluid, maxID);
        fluidNames.put(maxID, fluid.getName());
        defaultFluidName.put(fluid.getName(), uniqueName(fluid));

        MikuLib.MikuEventBus().post(new FluidRegistry.FluidRegisterEvent(fluid.getName(), maxID));
        return true;
    }
}