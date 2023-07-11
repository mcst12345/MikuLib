package miku.lib.mixins.minecraft;

import com.google.common.base.Throwables;
import miku.lib.util.EntityUtil;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EventBus.class,remap = false)
public class MixinEventBus {
    @Shadow private boolean shutdown;

    @Shadow private IEventExceptionHandler exceptionHandler;

    @Shadow @Final private int busID;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public boolean post(Event event) {
        if (EntityUtil.isKilling()) return false;
        if(event instanceof EntityJoinWorldEvent){
            if(EntityUtil.isProtected(((EntityJoinWorldEvent)event).getEntity()))return false;
        }
        if(event instanceof PlayerInteractEvent){
            if(EntityUtil.isProtected(((PlayerInteractEvent)event).getEntityPlayer()) && ((PlayerInteractEvent)event).getEntity() != null)return false;
        }
        if (shutdown) return false;

        IEventListener[] listeners = event.getListenerList().getListeners(busID);
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                listeners[index].invoke(event);
            }
        }
        catch (Throwable throwable)
        {
            exceptionHandler.handleException((EventBus) (Object)this, event, listeners, index, throwable);
            Throwables.throwIfUnchecked(throwable);
            throw new RuntimeException(throwable);
        }
        return event.isCancelable() && event.isCanceled();
    }
}
