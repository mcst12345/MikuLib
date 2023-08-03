package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityEndPortalRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.Random;

@Mixin(value = TileEntityEndPortalRenderer.class)
public abstract class MixinTileEntityEndPortalRenderer extends TileEntitySpecialRenderer<TileEntityEndPortal> {
    @Shadow
    @Final
    private static Random RANDOM;

    @Shadow
    @Final
    private static FloatBuffer MODELVIEW;

    @Shadow
    @Final
    private static FloatBuffer PROJECTION;

    @Shadow
    protected abstract int getPasses(double p_191286_1_);

    @Shadow
    protected abstract float getOffset();

    @Shadow
    @Final
    private static ResourceLocation END_SKY_TEXTURE;

    @Shadow
    @Final
    private static ResourceLocation END_PORTAL_TEXTURE;

    @Shadow
    protected abstract FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_, float p_147525_4_);

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void render(@Nonnull TileEntityEndPortal te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);
        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);
        double d0 = x * x + y * y + z * z;
        int i = this.getPasses(d0);
        float f = this.getOffset();
        boolean flag = false;

        for (int j = 0; j < i; ++j) {
            GlStateManager.pushMatrix();
            float f1 = 2.0F / (float) (18 - j);

            if (j == 0) {
                this.bindTexture(END_SKY_TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (j >= 1) {
                this.bindTexture(END_PORTAL_TEXTURE);
                flag = true;
                ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer().setupFogColor(true);
            }

            if (j == 1) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
            float f2 = (float) (j + 1);
            GlStateManager.translate(17.0F / f2, (2.0F + f2 / 1.5F) * ((float) Minecraft.getSystemTime() % 800000.0F / 800000.0F), 0.0F);
            GlStateManager.rotate((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
            float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
            float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;

            if (te.shouldRenderFace(EnumFacing.SOUTH)) {
                bufferbuilder.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.NORTH)) {
                bufferbuilder.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.EAST)) {
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.WEST)) {
                bufferbuilder.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.DOWN)) {
                bufferbuilder.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            }

            if (te.shouldRenderFace(EnumFacing.UP)) {
                bufferbuilder.pos(x, y + (double) f, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + (double) f, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + 1.0D, y + (double) f, z).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x, y + (double) f, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();

        if (flag) {
            ((iMinecraft) Minecraft.getMinecraft()).MikuEntityRenderer();
        }
    }
}
