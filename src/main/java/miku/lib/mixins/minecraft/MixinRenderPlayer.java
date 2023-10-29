package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(value = RenderPlayer.class)
public abstract class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {

    @Shadow
    protected abstract void setModelVisibilities(AbstractClientPlayer clientPlayer);

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!
     */
    @Overwrite
    public void doRender(@Nonnull AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (EntityUtil.isDEAD(entity)) return;
        if (MikuLib.MikuEventBus.post(new net.minecraftforge.client.event.RenderPlayerEvent.Pre(entity, (RenderPlayer) (Object) this, partialTicks, x, y, z)))
            return;
        if (!entity.isUser() || this.renderManager.renderViewEntity == entity) {
            double d0 = y;

            if (entity.isSneaking()) {
                d0 = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            super.doRender(entity, x, d0, z, entityYaw, partialTicks);
            GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
        }
        MikuLib.MikuEventBus.post(new net.minecraftforge.client.event.RenderPlayerEvent.Post(entity, (RenderPlayer) (Object) this, partialTicks, x, y, z));
    }

    @Inject(at = @At("HEAD"), method = "applyRotations(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V", cancellable = true)
    public void applyRotations(AbstractClientPlayer entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (EntityUtil.isProtected(entityLiving)) ci.cancel();
    }
}
