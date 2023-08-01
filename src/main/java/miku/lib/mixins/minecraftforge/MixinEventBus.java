package miku.lib.mixins.minecraftforge;

import miku.lib.common.api.iEventBus;
import miku.lib.common.util.EntityUtil;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventExceptionHandler;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EventBus.class, remap = false)
public class MixinEventBus implements iEventBus {
    @Shadow
    private boolean shutdown;

    @Shadow
    private IEventExceptionHandler exceptionHandler;

    @Shadow
    @Final
    private int busID;

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
            if (throwable instanceof NoSuchMethodError || throwable instanceof NoSuchFieldError) return false;
            throwable.printStackTrace();
            try {
                event.setCanceled(true);
            } catch (Throwable ignored) {
            }
            return true;
        }
        return event.isCancelable() && event.isCanceled();
    }

    /**
     * @author mcst12345
     * @reason fuck you all
     */
    @Overwrite
    public void shutdown() {
    }

    @Override
    public void Shutdown() {
        FMLLog.log.warn("EventBus {} shutting down - future events will not be posted.", busID);
        shutdown = true;
    }
}
