package miku.lib.mixins.minecraft;

import miku.lib.util.EntityUtil;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Render.class)
public abstract class MixinRender<T extends Entity> {
    @Inject(at=@At("HEAD"),method = "shouldRender", cancellable = true)
    public void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isDEAD(livingEntity))cir.setReturnValue(false);
        if(EntityUtil.isProtected(livingEntity) && (livingEntity instanceof EntityPlayer))cir.setReturnValue(true);
    }

    @Inject(at=@At("HEAD"),method = "bindEntityTexture", cancellable = true)
    protected void bindEntityTexture(T entity, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isDEAD(entity))cir.setReturnValue(true);
    }
}
