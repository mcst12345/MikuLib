package miku.lib.mixins;

import miku.lib.util.EntityUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(at=@At("HEAD"),method = "applyRotations")
    protected void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci){
        if(EntityUtil.isProtected(entityLiving)){
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer) entityLiving).isWearing(EnumPlayerModelParts.CAPE)))
            {
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Inject(at=@At("HEAD"),method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", cancellable = true)
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci){
        if(EntityUtil.isDEAD(entity))ci.cancel();
    }
}
