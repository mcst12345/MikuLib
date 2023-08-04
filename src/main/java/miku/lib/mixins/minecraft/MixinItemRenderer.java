package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemRenderer.class)
public abstract class MixinItemRenderer {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    @Final
    private static ResourceLocation RES_MAP_BACKGROUND;

    @Shadow
    protected abstract void renderSuffocationOverlay(TextureAtlasSprite sprite);

    @Shadow
    protected abstract void renderWaterOverlayTexture(float partialTicks);

    @Shadow
    protected abstract void renderFireInFirstPerson();

    /**
     * @author mcst12345
     * @reason f
     */
    @Overwrite
    public void renderMapFirstPerson(ItemStack stack) {
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.disableLighting();
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.translate(-0.5F, -0.5F, 0.0F);
        GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = ((net.minecraft.item.ItemMap) stack.getItem()).getMapData(stack, ((iMinecraft) this.mc).MikuWorld());

        if (mapdata != null) {
            ((iMinecraft) this.mc).MikuEntityRenderer().getMapItemRenderer().renderMap(mapdata, false);
        }

        GlStateManager.enableLighting();
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    private void setLightmap() {
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        int i = ((iMinecraft) this.mc).MikuWorld().getCombinedLight(new BlockPos(abstractclientplayer.posX, abstractclientplayer.posY + (double) abstractclientplayer.getEyeHeight(), abstractclientplayer.posZ), 0);
        float f = (float) (i & 65535);
        float f1 = (float) (i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void renderOverlays(float partialTicks) {
        GlStateManager.disableAlpha();

        if (this.mc.player.isEntityInsideOpaqueBlock()) {
            IBlockState iblockstate = ((iMinecraft) this.mc).MikuWorld().getBlockState(new BlockPos(this.mc.player));
            BlockPos overlayPos = new BlockPos(this.mc.player);
            EntityPlayer entityplayer = this.mc.player;

            for (int i = 0; i < 8; ++i) {
                double d0 = entityplayer.posX + (double) (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
                double d1 = entityplayer.posY + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayer.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
                BlockPos blockpos = new BlockPos(d0, d1 + (double) entityplayer.getEyeHeight(), d2);
                IBlockState iblockstate1 = ((iMinecraft) this.mc).MikuWorld().getBlockState(blockpos);

                if (iblockstate1.causesSuffocation()) {
                    iblockstate = iblockstate1;
                    overlayPos = blockpos;
                }
            }

            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                if (!net.minecraftforge.event.ForgeEventFactory.renderBlockOverlay(mc.player, partialTicks, net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType.BLOCK, iblockstate, overlayPos))
                    this.renderSuffocationOverlay(this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
            }
        }

        if (!this.mc.player.isSpectator()) {
            if (this.mc.player.isInsideOfMaterial(Material.WATER)) {
                if (!net.minecraftforge.event.ForgeEventFactory.renderWaterOverlay(mc.player, partialTicks))
                    this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.player.isBurning()) {
                if (!net.minecraftforge.event.ForgeEventFactory.renderFireOverlay(mc.player, partialTicks))
                    this.renderFireInFirstPerson();
            }
        }

        GlStateManager.enableAlpha();
    }
}
