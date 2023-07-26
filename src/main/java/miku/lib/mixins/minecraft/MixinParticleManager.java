package miku.lib.mixins.minecraft;

import miku.lib.common.api.iMinecraft;
import miku.lib.common.item.SpecialItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ParticleManager.class)
public class MixinParticleManager {
    @Inject(at=@At("HEAD"),method = "updateEffects", cancellable = true)
    public void updateEffects(CallbackInfo ci){
        if(SpecialItem.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop())ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "renderParticles", cancellable = true)
    public void renderParticles(Entity entityIn, float partialTicks, CallbackInfo ci){
        if(SpecialItem.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop())ci.cancel();
    }
}
