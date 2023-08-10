package miku.lib.mixins.minecraftforge;

import miku.lib.common.core.MikuLib;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = FluidEvent.class)
public abstract class MixinFluidEvent extends Event {
    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public static final void fireEvent(FluidEvent event) {
        MikuLib.MikuEventBus().post(event);
    }
}
