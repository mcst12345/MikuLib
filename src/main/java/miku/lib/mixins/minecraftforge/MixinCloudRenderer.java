package miku.lib.mixins.minecraftforge;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.CloudRenderer;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.common.ForgeModContainer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(value = CloudRenderer.class, remap = false)
public abstract class MixinCloudRenderer implements ISelectiveResourceReloadListener {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract boolean isBuilt();

    @Shadow
    private int cloudMode;

    @Shadow
    private int renderDistance;

    @Shadow
    protected abstract void dispose();

    @Shadow
    protected abstract void build();

    @Shadow
    protected abstract int getScale();

    @Shadow
    protected abstract int fullCoord(double coord, int scale);

    @Shadow
    private int texW;

    @Shadow
    private int texH;

    @Shadow
    @Final
    private static float PX_SIZE;

    @Shadow
    private DynamicTexture COLOR_TEX;

    @Shadow
    @Final
    private ResourceLocation texture;

    @Shadow
    private VertexBuffer vbo;

    @Shadow
    @Final
    private static VertexFormat FORMAT;

    @Shadow
    private int displayList;

    @Shadow
    @Final
    private static boolean WIREFRAME;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void checkSettings() {
        boolean newEnabled = ForgeModContainer.forgeCloudsEnabled
                && mc.gameSettings.shouldRenderClouds() != 0
                && ((iMinecraft) mc).MikuWorld() != null
                && ((iMinecraft) mc).MikuWorld().provider.isSurfaceWorld();

        if (isBuilt()
                && (!newEnabled
                || mc.gameSettings.shouldRenderClouds() != cloudMode
                || mc.gameSettings.renderDistanceChunks != renderDistance)) {
            dispose();
        }

        cloudMode = mc.gameSettings.shouldRenderClouds();
        renderDistance = mc.gameSettings.renderDistanceChunks;

        if (newEnabled && !isBuilt()) {
            build();
        }
    }

    /**
     * @author mcst12345
     * @reason F!
     */
    @Overwrite
    public boolean render(int cloudTicks, float partialTicks) {
        boolean result = true;
        if (!isBuilt()) {
            result = false;
        } else {
            Entity entity = mc.getRenderViewEntity();
            double totalOffset = cloudTicks + partialTicks;
            double x = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks
                    + totalOffset * 0.03;
            double y = ((iMinecraft) mc).MikuWorld().provider.getCloudHeight()
                    - (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks)
                    + 0.33;
            double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
            int scale = getScale();
            if (cloudMode == 2)
                z += 0.33 * scale;// Integer UVs to translate the texture matrix by.
            int offU = fullCoord(x, scale);
            int offV = fullCoord(z, scale);
            GlStateManager.pushMatrix();// Translate by the remainder after the UV offset.
            GlStateManager.translate((offU * scale) - x, y, (offV * scale) - z);// Modulo to prevent texture samples becoming inaccurate at extreme offsets.
            offU = offU % texW;
            offV = offV % texH;// Translate the texture.
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.translate(offU * PX_SIZE, offV * PX_SIZE, 0);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);// Color multiplier.
            Vec3d color = ((iMinecraft) mc).MikuWorld().getCloudColour(partialTicks);
            float r = (float) color.x;
            float g = (float) color.y;
            float b = (float) color.z;
            if (mc.gameSettings.anaglyph) {
                float tempR = r * 0.3F + g * 0.59F + b * 0.11F;
                float tempG = r * 0.3F + g * 0.7F;
                float tempB = r * 0.3F + b * 0.7F;
                r = tempR;
                g = tempG;
                b = tempB;
            }
            if (COLOR_TEX == null)
                COLOR_TEX = new DynamicTexture(1, 1);// Apply a color multiplier through a texture upload if shaders aren't supported.
            COLOR_TEX.getTextureData()[0] = 255 << 24
                    | ((int) (r * 255)) << 16
                    | ((int) (g * 255)) << 8
                    | (int) (b * 255);
            COLOR_TEX.updateDynamicTexture();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.bindTexture(COLOR_TEX.getGlTextureId());
            GlStateManager.enableTexture2D();// Bind the clouds texture last so the shader's sampler2D is correct.
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            mc.renderEngine.bindTexture(texture);
            ByteBuffer buffer = Tessellator.getInstance().getBuffer().getByteBuffer();// Set up pointers for the display list/VBO.
            if (OpenGlHelper.useVbo()) {
                vbo.bindBuffer();

                int stride = FORMAT.getSize();
                GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
                GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);
                GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 20);
                GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
            } else {
                buffer.limit(FORMAT.getSize());
                for (int i = 0; i < FORMAT.getElementCount(); i++)
                    FORMAT.getElements().get(i).getUsage().preDraw(FORMAT, i, FORMAT.getSize(), buffer);
                buffer.position(0);
            }// Depth pass to prevent insides rendering from the outside.
            GlStateManager.colorMask(false, false, false, false);
            if (OpenGlHelper.useVbo())
                vbo.drawArrays(GL11.GL_QUADS);
            else
                GlStateManager.callList(displayList);// Full render.
            if (!mc.gameSettings.anaglyph) {
                GlStateManager.colorMask(true, true, true, true);
            } else {
                switch (EntityRenderer.anaglyphField) {
                    case 0:
                        GlStateManager.colorMask(false, true, true, true);
                        break;
                    case 1:
                        GlStateManager.colorMask(true, false, false, true);
                        break;
                }
            }// Wireframe for debug.
            if (WIREFRAME) {
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.disableFog();
                if (OpenGlHelper.useVbo())
                    vbo.drawArrays(GL11.GL_QUADS);
                else
                    GlStateManager.callList(displayList);
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.enableFog();
            }
            if (OpenGlHelper.useVbo()) {
                vbo.drawArrays(GL11.GL_QUADS);
                vbo.unbindBuffer(); // Unbind buffer and disable pointers.
            } else {
                GlStateManager.callList(displayList);
            }
            buffer.limit(0);
            for (int i = 0; i < FORMAT.getElementCount(); i++)
                FORMAT.getElements().get(i).getUsage().postDraw(FORMAT, i, FORMAT.getSize(), buffer);
            buffer.position(0);// Disable our coloring.
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);// Reset texture matrix.
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        }

        return result;
    }
}
