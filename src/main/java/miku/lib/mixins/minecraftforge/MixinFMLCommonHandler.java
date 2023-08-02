package miku.lib.mixins.minecraftforge;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FMLCommonHandler.class)
public abstract class MixinFMLCommonHandler {
    @Shadow
    @Deprecated
    public abstract EventBus bus();

    /**
     * @author mcst12345
     * @reason Holy Fuck
     */
    @Overwrite
    public void onPreClientTick() {
        try {
            bus().post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onPostClientTick() {
        try {
            bus().post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
