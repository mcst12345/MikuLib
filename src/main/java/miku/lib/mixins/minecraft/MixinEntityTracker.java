package miku.lib.mixins.minecraft;

import miku.lib.item.SpecialItem;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityTracker.class)
public class MixinEntityTracker {

    @Inject(at=@At("HEAD"),method = "track(Lnet/minecraft/entity/Entity;IIZ)V",cancellable = true)
    public void track(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates, CallbackInfo ci){
        if(SpecialItem.isTimeStop() || EntityUtil.isKilling()){
            if(!EntityUtil.isProtected(entityIn))ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"),method = "track(Lnet/minecraft/entity/Entity;)V",cancellable = true)
    public void track(Entity entityIn, CallbackInfo ci){
        if(SpecialItem.isTimeStop() || EntityUtil.isKilling()){
            if(!EntityUtil.isProtected(entityIn))ci.cancel();
        }
    }

    @Inject(at=@At("HEAD"),method = "tick",cancellable = true)
    public void tick(CallbackInfo ci){
        if(SpecialItem.isTimeStop() || EntityUtil.isKilling())ci.cancel();
    }
}
