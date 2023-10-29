package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.core.MikuLib;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = EntityRenderer.class)
public abstract class MixinEntityRenderer implements IResourceManagerReloadListener {
    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    private float smoothCamYaw;
    @Shadow
    private float smoothCamPitch;
    @Shadow
    private float smoothCamPartialTicks;
    @Shadow
    private float smoothCamFilterX;
    @Shadow
    private float smoothCamFilterY;
    @Shadow
    private float bossColorModifier;

    @Shadow
    private float bossColorModifierPrev;


    @Shadow
    public abstract void disableLightmap();

    @Shadow
    public abstract void enableLightmap();

    @Shadow
    protected abstract void updateFovModifierHand();

    @Shadow
    protected abstract void updateTorchFlicker();

    @Shadow
    private float fogColor2;

    @Shadow
    private float fogColor1;

    @Shadow
    private float thirdPersonDistancePrev;

    @Shadow
    @Final
    private MouseFilter mouseFilterXAxis;

    @Shadow
    @Final
    private MouseFilter mouseFilterYAxis;

    @Shadow
    private int rendererUpdateCount;

    @Shadow
    @Final
    public ItemRenderer itemRenderer;

    @Shadow
    private int itemActivationTicks;

    @Shadow
    private ItemStack itemActivationItem;

    @Shadow
    private boolean cloudFog;

    @Shadow
    @Final
    private Random random;

    @Shadow
    private int rainSoundCounter;

    @Shadow
    @Final
    private float[] rainXCoords;

    @Shadow
    @Final
    private float[] rainYCoords;

    @Shadow
    @Final
    private static ResourceLocation RAIN_TEXTURES;

    @Shadow
    @Final
    private static ResourceLocation SNOW_TEXTURES;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void updateRenderer() {
        boolean time_stop = TimeStopUtil.isTimeStop() || ((iMinecraft) this.mc).isTimeStop();

        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        this.updateFovModifierHand();
        this.updateTorchFlicker();
        this.fogColor2 = this.fogColor1;
        this.thirdPersonDistancePrev = 4.0F;

        if (!time_stop) {
            if (this.mc.gameSettings.smoothCamera) {
                float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                float f1 = f * f * f * 8.0F;
                this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * f1);
                this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * f1);
                this.smoothCamPartialTicks = 0.0F;
                this.smoothCamYaw = 0.0F;
                this.smoothCamPitch = 0.0F;
            } else {
                this.smoothCamFilterX = 0.0F;
                this.smoothCamFilterY = 0.0F;
                this.mouseFilterXAxis.reset();
                this.mouseFilterYAxis.reset();
            }
        }

        if (this.mc.getRenderViewEntity() == null) {
            this.mc.setRenderViewEntity(((iMinecraft) this.mc).MikuPlayer());
        }

        float f3 = ((iMinecraft) this.mc).MikuWorld().getLightBrightness(new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + this.mc.getRenderViewEntity().getEyeHeight(), this.mc.getRenderViewEntity().posZ)); // Forge: fix MC-51150
        float f4 = (float) this.mc.gameSettings.renderDistanceChunks / 32.0F;
        float f2 = f3 * (1.0F - f4) + f4;
        this.fogColor1 += (f2 - this.fogColor1) * 0.1F;
        ++this.rendererUpdateCount;
        this.itemRenderer.updateEquippedItem();
        this.addRainParticles();
        this.bossColorModifierPrev = this.bossColorModifier;

        if (this.mc.ingameGUI.getBossOverlay().shouldDarkenSky()) {
            this.bossColorModifier += 0.05F;

            if (this.bossColorModifier > 1.0F) {
                this.bossColorModifier = 1.0F;
            }
        } else if (this.bossColorModifier > 0.0F) {
            this.bossColorModifier -= 0.0125F;
        }

        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;

            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void orientCamera(float partialTicks) {
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            f = (float) ((double) f + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = ((iMinecraft) this.mc).MikuWorld().getBlockState(blockpos);
                net.minecraftforge.client.ForgeHooksClient.orientBedCamera(((iMinecraft) this.mc).MikuWorld(), blockpos, iblockstate, entity);

                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.mc.gameSettings.thirdPersonView > 0) {
            double d3 = this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * partialTicks;

            if (this.mc.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
            } else {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    f2 += 180.0F;
                }

                double d4 = (double) (-MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d5 = (double) (MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f2 * 0.017453292F)) * d3;
                double d6 = (double) (-MathHelper.sin(f2 * 0.017453292F)) * d3;

                for (int i = 0; i < 8; ++i) {
                    float f3 = (float) ((i & 1) * 2 - 1);
                    float f4 = (float) ((i >> 1 & 1) * 2 - 1);
                    float f5 = (float) ((i >> 2 & 1) * 2 - 1);
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;
                    RayTraceResult raytraceresult = ((iMinecraft) this.mc).MikuWorld().rayTraceBlocks(new Vec3d(d0 + (double) f3, d1 + (double) f4, d2 + (double) f5), new Vec3d(d0 - d4 + (double) f3 + (double) f5, d1 - d6 + (double) f4, d2 - d5 + (double) f5));

                    if (raytraceresult != null) {
                        double d7 = raytraceresult.hitVec.distanceTo(new Vec3d(d0, d1, d2));

                        if (d7 < d3) {
                            d3 = d7;
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        } else {
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
        }

        if (!this.mc.gameSettings.debugCamEnable) {
            float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float roll = 0.0F;
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal) entity;
                yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
            }
            IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(((iMinecraft) this.mc).MikuWorld(), entity, partialTicks);
            net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup event = new net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup((EntityRenderer) (Object) this, entity, state, partialTicks, yaw, pitch, roll);
            if (!EntityUtil.isProtected(mc)) {
                MikuLib.MikuEventBus.post(event);
            }
            GlStateManager.rotate(event.getRoll(), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(event.getPitch(), 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(event.getYaw(), 0.0F, 1.0F, 0.0F);
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void addRainParticles() {
        float f = ((iMinecraft) this.mc).MikuWorld().getRainStrength(1.0F);

        if (!this.mc.gameSettings.fancyGraphics) {
            f /= 2.0F;
        }

        if (f != 0.0F && Sqlite.GetBooleanFromTable("rain_splash", "RENDER_CONFIG")) {
            this.random.setSeed((long) this.rendererUpdateCount * 312987231L);
            Entity entity = this.mc.getRenderViewEntity();
            World world = ((iMinecraft) this.mc).MikuWorld();
            BlockPos blockpos = new BlockPos(entity);
            double d0 = 0.0D;
            double d1 = 0.0D;
            double d2 = 0.0D;
            int j = 0;
            int k = (int) (100.0F * f * f);

            if (this.mc.gameSettings.particleSetting == 1) {
                k >>= 1;
            } else if (this.mc.gameSettings.particleSetting == 2) {
                k = 0;
            }

            for (int l = 0; l < k; ++l) {
                BlockPos blockpos1 = world.getPrecipitationHeight(blockpos.add(this.random.nextInt(10) - this.random.nextInt(10), 0, this.random.nextInt(10) - this.random.nextInt(10)));
                Biome biome = world.getBiome(blockpos1);
                BlockPos blockpos2 = blockpos1.down();
                IBlockState iblockstate = world.getBlockState(blockpos2);

                if (blockpos1.getY() <= blockpos.getY() + 10 && blockpos1.getY() >= blockpos.getY() - 10 && biome.canRain() && biome.getTemperature(blockpos1) >= 0.15F) {
                    double d3 = this.random.nextDouble();
                    double d4 = this.random.nextDouble();
                    AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, blockpos2);

                    if (iblockstate.getMaterial() != Material.LAVA && iblockstate.getBlock() != Blocks.MAGMA) {
                        if (iblockstate.getMaterial() != Material.AIR) {
                            ++j;

                            if (this.random.nextInt(j) == 0) {
                                d0 = blockpos2.getX() + d3;
                                d1 = blockpos2.getY() + 0.1F + axisalignedbb.maxY - 1.0D;
                                d2 = blockpos2.getZ() + d4;
                            }

                            ((iMinecraft) this.mc).MikuWorld().spawnParticle(EnumParticleTypes.WATER_DROP, blockpos2.getX() + d3, (blockpos2.getY() + 0.1F) + axisalignedbb.maxY, blockpos2.getZ() + d4, 0.0D, 0.0D, 0.0D);
                        }
                    } else {
                        ((iMinecraft) this.mc).MikuWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, blockpos1.getX() + d3, (blockpos1.getY() + 0.1F) - axisalignedbb.minY, blockpos1.getZ() + d4, 0.0D, 0.0D, 0.0D);
                    }
                }
            }

            if (j > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
                this.rainSoundCounter = 0;

                if (d1 > (blockpos.getY() + 1) && world.getPrecipitationHeight(blockpos).getY() > MathHelper.floor(blockpos.getY())) {
                    ((iMinecraft) this.mc).MikuWorld().playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
                } else {
                    ((iMinecraft) this.mc).MikuWorld().playSound(d0, d1, d2, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void renderRainSnow(float partialTicks) {
        net.minecraftforge.client.IRenderHandler renderer = ((iMinecraft) this.mc).MikuWorld().provider.getWeatherRenderer();
        if (renderer != null) {
            renderer.render(partialTicks, ((iMinecraft) this.mc).MikuWorld(), mc);
            return;
        }

        float f = ((iMinecraft) this.mc).MikuWorld().getRainStrength(partialTicks);

        if (f > 0.0F && Sqlite.GetBooleanFromTable("rain", "RENDER_CONFIG")) {
            this.enableLightmap();
            Entity entity = this.mc.getRenderViewEntity();
            World world = ((iMinecraft) this.mc).MikuWorld();
            assert entity != null;
            int i = MathHelper.floor(entity.posX);
            int j = MathHelper.floor(entity.posY);
            int k = MathHelper.floor(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.disableCull();
            GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.alphaFunc(516, 0.1F);
            double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            int l = MathHelper.floor(d1);
            int i1 = 5;

            if (this.mc.gameSettings.fancyGraphics) {
                i1 = 10;
            }

            int j1 = -1;
            float f1 = this.rendererUpdateCount + partialTicks;
            bufferbuilder.setTranslation(-d0, -d1, -d2);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k1 = k - i1; k1 <= k + i1; ++k1) {
                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                    double d3 = this.rainXCoords[i2] * 0.5D;
                    double d4 = this.rainYCoords[i2] * 0.5D;
                    blockpos$mutableblockpos.setPos(l1, 0, k1);
                    Biome biome = world.getBiome(blockpos$mutableblockpos);

                    if (biome.canRain() || biome.getEnableSnow()) {
                        int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int k2 = j - i1;
                        int l2 = j + i1;

                        if (k2 < j2) {
                            k2 = j2;
                        }

                        if (l2 < j2) {
                            l2 = j2;
                        }

                        int i3 = Math.max(j2, l);

                        if (k2 != l2) {
                            this.random.setSeed((long) l1 * l1 * 3121 + l1 * 45238971L ^ (long) k1 * k1 * 418711 + k1 * 13761L);
                            blockpos$mutableblockpos.setPos(l1, k2, k1);
                            float f2 = biome.getTemperature(blockpos$mutableblockpos);

                            if (world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F) {
                                if (j1 != 0) {
                                    if (j1 >= 0) {
                                        tessellator.draw();
                                    }

                                    j1 = 0;
                                    this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d5 = -((this.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                double d6 = l1 + 0.5F - entity.posX;
                                double d7 = k1 + 0.5F - entity.posZ;
                                float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / i1;
                                float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
                                blockpos$mutableblockpos.setPos(l1, i3, k1);
                                int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
                                int k3 = j3 >> 16 & 65535;
                                int l3 = j3 & 65535;
                                bufferbuilder.pos(l1 - d3 + 0.5D, l2, k1 - d4 + 0.5D).tex(0.0D, k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos(l1 + d3 + 0.5D, l2, k1 + d4 + 0.5D).tex(1.0D, k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos(l1 + d3 + 0.5D, k2, k1 + d4 + 0.5D).tex(1.0D, l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                bufferbuilder.pos(l1 - d3 + 0.5D, k2, k1 - d4 + 0.5D).tex(0.0D, l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                            } else {
                                if (j1 != 1) {
                                    if (j1 == 0) {
                                        tessellator.draw();
                                    }

                                    j1 = 1;
                                    this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d8 = -(this.rendererUpdateCount & 511) + partialTicks / 512.0F;
                                double d9 = this.random.nextDouble() + f1 * 0.01D * this.random.nextGaussian();
                                double d10 = this.random.nextDouble() + (f1 * this.random.nextGaussian()) * 0.001D;
                                double d11 = l1 + 0.5F - entity.posX;
                                double d12 = k1 + 0.5F - entity.posZ;
                                float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / i1;
                                float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                                blockpos$mutableblockpos.setPos(l1, i3, k1);
                                int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int j4 = i4 >> 16 & 65535;
                                int k4 = i4 & 65535;
                                bufferbuilder.pos(l1 - d3 + 0.5D, l2, k1 - d4 + 0.5D).tex(0.0D + d9, k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos(l1 + d3 + 0.5D, l2, k1 + d4 + 0.5D).tex(1.0D + d9, k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos(l1 + d3 + 0.5D, k2, k1 + d4 + 0.5D).tex(1.0D + d9, l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                bufferbuilder.pos(l1 - d3 + 0.5D, k2, k1 - d4 + 0.5D).tex(0.0D + d9, l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            }
                        }
                    }
                }
            }

            if (j1 >= 0) {
                tessellator.draw();
            }

            bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            this.disableLightmap();
        }
    }
}
