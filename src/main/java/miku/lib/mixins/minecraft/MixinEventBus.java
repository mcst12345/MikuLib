package miku.lib.mixins.minecraft;

import miku.lib.util.EntityUtil;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EventBus.class)
public class MixinEventBus {
    @Inject(at = @At("HEAD"), method = "post", cancellable = true, remap = false)
    public void post(Event event, CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.isKilling()) cir.setReturnValue(false);
        if(event instanceof EntityJoinWorldEvent){
            if(EntityUtil.isProtected(((EntityJoinWorldEvent)event).getEntity()))cir.setReturnValue(false);
        }
        if(event instanceof PlayerInteractEvent){
            if(EntityUtil.isProtected(((PlayerInteractEvent)event).getEntityPlayer()) && ((PlayerInteractEvent)event).getEntity() != null)cir.setReturnValue(false);
        }
    }
}
