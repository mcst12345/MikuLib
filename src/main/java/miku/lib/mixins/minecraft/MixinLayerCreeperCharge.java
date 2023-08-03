package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderCreeper;
import net.minecraft.client.renderer.entity.layers.LayerCreeperCharge;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LayerCreeperCharge.class)
public abstract class MixinLayerCreeperCharge {
    @Shadow
    @Final
    private RenderCreeper creeperRenderer;

    @Shadow
    @Final
    private static ResourceLocation LIGHTNING_TEXTURE;

    @Shadow
    @Final
    private ModelCreeper creeperModel;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void doRenderLayer(EntityCreeper entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (entitylivingbaseIn.getPowered()) {
            boolean flag = entitylivingbaseIn.isInvisible();
            GlStateManager.depthMask(!flag);
            this.creeperRenderer.bindTexture(LIGHTNING_TEXTURE);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float f = (float) entitylivingbaseIn.ticksExisted + partialTicks;
            GlStateManager.translate(f * 0.01F, f * 0.01F, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            this.creeperModel.setModelAttributes(this.creeperRenderer.getMainModel());
            ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer().setupFogColor(true);
            this.creeperModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer().setupFogColor(false);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(flag);
        }
    }
}
