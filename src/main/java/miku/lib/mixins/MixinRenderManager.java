package miku.lib.mixins;

import miku.lib.api.iEntity;
import miku.lib.item.SpecialItem;
import miku.lib.util.EntityUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderManager.class)
public class MixinRenderManager {
    @Inject(at=@At("HEAD"),method = "renderEntityStatic", cancellable = true)
    public void renderEntityStatic(Entity entityIn, float partialTicks, boolean p_188388_3_, CallbackInfo ci){
        if((SpecialItem.isTimeStop() || ((iEntity)entityIn).isTimeStop()) && !EntityUtil.isProtected(entityIn))ci.cancel();
        System.out.println(entityIn.getClass().toString());
    }
}
