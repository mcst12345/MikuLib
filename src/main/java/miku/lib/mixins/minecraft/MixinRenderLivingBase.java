package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RenderLivingBase.class)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
    @Shadow
    protected ModelBase mainModel;

    @Shadow
    protected abstract float getSwingProgress(T livingBase, float partialTickTime);

    @Shadow
    protected abstract float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks);

    /**
     * @author mcst12345
     * @reason Holy s
     */
    @Overwrite
    public void renderLivingAt(T entityLivingBaseIn, double x, double y, double z) {
        if (EntityUtil.isDEAD(entityLivingBaseIn)) return;
        GlStateManager.translate((float) x, (float) y, (float) z);
    }

    @Shadow
    protected abstract float handleRotationFloat(T livingBase, float partialTicks);

    @Shadow
    protected abstract void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks);

    @Shadow
    public abstract float prepareScale(T entitylivingbaseIn, float partialTicks);

    @Shadow
    protected abstract boolean setScoreTeamColor(T entityLivingBaseIn);

    @Shadow
    protected boolean renderMarker;


    /**
     * @author mcst12345
     * @reason Holy Shit
     */
    @Overwrite
    public void renderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        if (EntityUtil.isDEAD(entitylivingbaseIn)) return;
        boolean flag = this.isVisible(entitylivingbaseIn);
        boolean flag1 = !flag && !entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().player);

        if (flag || flag1) {
            if (!this.bindEntityTexture(entitylivingbaseIn)) {
                return;
            }

            if (flag1) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            this.mainModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

            if (flag1) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!!
     */
    @Overwrite
    public void renderLayers(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
        if (EntityUtil.isDEAD(entitylivingbaseIn)) return;
        for (LayerRenderer<T> layerrenderer : this.layerRenderers) {
            boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures());
            layerrenderer.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scaleIn);

            if (flag) {
                this.unsetBrightness();
            }
        }
    }

    @Shadow
    protected abstract void unsetScoreTeamColor();

    @Shadow
    protected abstract boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks);

    @Shadow
    protected abstract void unsetBrightness();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract boolean isVisible(T p_193115_1_);

    @Shadow
    protected List<LayerRenderer<T>> layerRenderers;

    @Shadow
    protected abstract boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures);

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(at = @At("HEAD"), method = "applyRotations", cancellable = true)
    public void applyRotations(T entityLiving, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci) {
        if (EntityUtil.isProtected(entityLiving)) {
            GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer) entityLiving).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (EntityUtil.isDEAD(entity)) return;
        if (MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<T>(entity, (RenderLivingBase<T>) (Object) this, partialTicks, x, y, z)))
            return;
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        this.mainModel.isRiding = shouldSit;
        this.mainModel.isChild = entity.isChild();

        try {
            float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
                EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
                f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapDegrees(f2);

                if (f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F) {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F) {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            this.renderLivingAt(entity, x, y, z);
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.applyRotations(entity, f8, f, partialTicks);
            float f4 = this.prepareScale(entity, partialTicks);
            float f5 = 0.0F;
            float f6 = 0.0F;

            if (!entity.isRiding()) {
                f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

                if (entity.isChild()) {
                    f6 *= 3.0F;
                }

                if (f5 > 1.0F) {
                    f5 = 1.0F;
                }
                f2 = f1 - f; // Forge: Fix MC-1207
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, f4, entity);

            if (this.renderOutlines) {
                boolean flag1 = this.setScoreTeamColor(entity);
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(this.getTeamColor(entity));

                if (!this.renderMarker) {
                    this.renderModel(entity, f6, f5, f8, f2, f7, f4);
                }

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) {
                    this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                }

                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();

                if (flag1) {
                    this.unsetScoreTeamColor();
                }
            } else {
                boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                this.renderModel(entity, f6, f5, f8, f2, f7, f4);

                if (flag) {
                    this.unsetBrightness();
                }

                GlStateManager.depthMask(true);

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) {
                    this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, f4);
                }
            }

            GlStateManager.disableRescaleNormal();
        } catch (Exception exception) {
            LOGGER.error("Couldn't render entity", exception);
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T>(entity, (RenderLivingBase<T>) (Object) this, partialTicks, x, y, z));
    }
}
