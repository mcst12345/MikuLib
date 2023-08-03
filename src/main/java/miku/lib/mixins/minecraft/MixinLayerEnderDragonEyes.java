package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonEyes;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LayerEnderDragonEyes.class)
public class MixinLayerEnderDragonEyes {
    @Shadow
    @Final
    private RenderDragon dragonRenderer;

    @Shadow
    @Final
    private static ResourceLocation TEXTURE;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void doRenderLayer(EntityDragon entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.dragonRenderer.bindTexture(TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.disableLighting();
        GlStateManager.depthFunc(514);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer().setupFogColor(true);
        this.dragonRenderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer().setupFogColor(false);
        this.dragonRenderer.setLightmap(entitylivingbaseIn);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.depthFunc(515);
    }
}
