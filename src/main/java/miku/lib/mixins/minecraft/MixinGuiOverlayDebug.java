package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import miku.lib.client.api.iMinecraft;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(value = GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug extends Gui {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void renderDebugInfoLeft();

    @Shadow
    protected abstract void renderDebugInfoRight(ScaledResolution scaledRes);

    @Shadow
    public abstract void renderLagometer();

    @Shadow
    private static long bytesToMb(long bytes) {
        return 0;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("debug");
        GlStateManager.pushMatrix();
        this.renderDebugInfoLeft();
        this.renderDebugInfoRight(scaledResolutionIn);
        GlStateManager.popMatrix();

        if (this.mc.gameSettings.showLagometer) {
            this.renderLagometer();
        }

        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    @SuppressWarnings("incomplete-switch")
    protected List<String> call() {
        BlockPos blockpos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);

        if (this.mc.isReducedDebug()) {
            return Lists.newArrayList("Minecraft 1.12.2 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + ((iMinecraft) (this.mc)).MikuWorld().getDebugLoadedEntities(), ((iMinecraft) (this.mc)).MikuWorld().getProviderName(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
        } else {
            Entity entity = this.mc.getRenderViewEntity();
            EnumFacing enumfacing = entity.getHorizontalFacing();
            String s = "Invalid";

            switch (enumfacing) {
                case NORTH:
                    s = "Towards negative Z";
                    break;
                case SOUTH:
                    s = "Towards positive Z";
                    break;
                case WEST:
                    s = "Towards negative X";
                    break;
                case EAST:
                    s = "Towards positive X";
            }

            List<String> list = Lists.newArrayList("Minecraft 1.12.2 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType()) + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + ((iMinecraft) (this.mc)).MikuWorld().getDebugLoadedEntities(), ((iMinecraft) (this.mc)).MikuWorld().getProviderName(), "", String.format("XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ), String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()), String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4), String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, MathHelper.wrapDegrees(entity.rotationYaw), MathHelper.wrapDegrees(entity.rotationPitch)));

            if (((iMinecraft) (this.mc)).MikuWorld() != null) {
                Chunk chunk = ((iMinecraft) (this.mc)).MikuWorld().getChunk(blockpos);

                if (((iMinecraft) (this.mc)).MikuWorld().isBlockLoaded(blockpos) && blockpos.getY() >= 0 && blockpos.getY() < 256) {
                    if (!chunk.isEmpty()) {
                        list.add("Biome: " + chunk.getBiome(blockpos, ((iMinecraft) (this.mc)).MikuWorld().getBiomeProvider()).getBiomeName());
                        list.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
                        DifficultyInstance difficultyinstance = ((iMinecraft) (this.mc)).MikuWorld().getDifficultyForLocation(blockpos);

                        if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
                            EntityPlayerMP entityplayermp = this.mc.getIntegratedServer().getPlayerList().getPlayerByUUID(this.mc.player.getUniqueID());

                            if (entityplayermp != null) {
                                difficultyinstance = entityplayermp.world.getDifficultyForLocation(new BlockPos(entityplayermp));
                            }
                        }

                        list.add(String.format("Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getAdditionalDifficulty(), difficultyinstance.getClampedAdditionalDifficulty(), ((iMinecraft) (this.mc)).MikuWorld().getWorldTime() / 24000L));
                    } else {
                        list.add("Waiting for chunk...");
                    }
                } else {
                    list.add("Outside of world...");
                }
            }

            if (((iMinecraft) this.mc).MikuEntityRenderer() != null && ((iMinecraft) this.mc).MikuEntityRenderer().isShaderActive()) {
                list.add("Shader: " + ((iMinecraft) this.mc).MikuEntityRenderer().getShaderGroup().getShaderGroupName());
            }

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
                list.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
            }

            return list;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected <T extends Comparable<T>> List<String> getDebugInfoRight() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        List<String> list = Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), this.mc.isJava64bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMb(l), bytesToMb(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMb(j)), "", String.format("CPU: %s", OpenGlHelper.getCpu()), "", String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GlStateManager.glGetString(7936)), GlStateManager.glGetString(7937), GlStateManager.glGetString(7938));

        list.add("");
        list.addAll(net.minecraftforge.fml.common.FMLCommonHandler.instance().getBrandings(false));

        if (!this.mc.isReducedDebug()) {
            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
                BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
                IBlockState iblockstate = ((iMinecraft) (this.mc)).MikuWorld().getBlockState(blockpos);

                if (((iMinecraft) (this.mc)).MikuWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    iblockstate = iblockstate.getActualState(((iMinecraft) (this.mc)).MikuWorld(), blockpos);
                }

                list.add("");
                list.add(String.valueOf(Block.REGISTRY.getNameForObject(iblockstate.getBlock())));
                IProperty iproperty;
                String s;

                for (UnmodifiableIterator<Map.Entry<IProperty<?>, Comparable<?>>> unmodifiableiterator = iblockstate.getProperties().entrySet().iterator(); unmodifiableiterator.hasNext(); list.add(iproperty.getName() + ": " + s)) {
                    Map.Entry<IProperty<?>, Comparable<?>> entry = unmodifiableiterator.next();
                    iproperty = entry.getKey();
                    T t = (T) entry.getValue();
                    s = iproperty.getName(t);

                    if (Boolean.TRUE.equals(t)) {
                        s = TextFormatting.GREEN + s;
                    } else if (Boolean.FALSE.equals(t)) {
                        s = TextFormatting.RED + s;
                    }
                }
            }

        }
        return list;
    }
}
