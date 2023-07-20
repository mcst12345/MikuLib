package miku.lib.mixins.minecraftforge;

import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.event.FMLEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Method;

@Mixin(value = FMLModContainer.class, remap = false)
public class MixinFMLModContainer {
    @Shadow
    private ListMultimap<Class<? extends FMLEvent>, Method> eventMethods;

    @Shadow
    private Object modInstance;


    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Subscribe
    public void handleModStateEvent(FMLEvent event) {
        if (!eventMethods.containsKey(event.getClass())) {
            return;
        }
        try {
            for (Method m : eventMethods.get(event.getClass())) {
                try {
                    m.invoke(modInstance, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
