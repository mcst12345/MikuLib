package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.api.iViewFrustum;
import miku.lib.client.util.ContainerLocalRenderInformation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Mixin(value = RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Shadow
    private int renderEntitiesStartupCounter;

    @Shadow
    private WorldClient world;

    @Shadow
    @Final
    private RenderManager renderManager;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private int countEntitiesTotal;

    @Shadow
    private int countEntitiesRendered;

    @Shadow
    private int countEntitiesHidden;

    @Shadow
    protected abstract void postRenderDamagedBlocks();

    @Shadow
    protected abstract void preRenderDamagedBlocks();

    @Shadow
    @Final
    private Map<Integer, DestroyBlockProgress> damagedBlocks;

    @Shadow
    @Final
    private Set<TileEntity> setTileEntities;

    @Shadow
    protected abstract boolean isOutlineActive(Entity entityIn, Entity viewer, ICamera camera);

    @Shadow
    protected abstract boolean isRenderEntityOutlines();

    @Shadow
    private Framebuffer entityOutlineFramebuffer;
    @Shadow
    private boolean entityOutlinesRendered;
    @Shadow
    private ShaderGroup entityOutlineShader;
    @Shadow
    private int renderDistanceChunks;

    @Shadow
    public abstract void loadRenderers();

    @Shadow
    private double frustumUpdatePosX;
    @Shadow
    private double frustumUpdatePosY;
    @Shadow
    private double frustumUpdatePosZ;
    @Shadow
    private int frustumUpdatePosChunkX;
    @Shadow
    private int frustumUpdatePosChunkY;
    @Shadow
    private int frustumUpdatePosChunkZ;
    @Shadow
    private ViewFrustum viewFrustum;
    @Shadow
    private ChunkRenderContainer renderContainer;
    @Shadow
    private ClippingHelper debugFixedClippingHelper;
    @Shadow
    @Final
    private Vector3d debugTerrainFrustumPosition;
    @Shadow
    private boolean displayListEntitiesDirty;
    @Shadow
    private Set<RenderChunk> chunksToUpdate;
    @Shadow
    private double lastViewEntityX;
    @Shadow
    private ChunkRenderDispatcher renderDispatcher;
    @Shadow
    private double lastViewEntityY;
    @Shadow
    private double lastViewEntityZ;
    @Shadow
    private double lastViewEntityPitch;
    @Shadow
    private double lastViewEntityYaw;

    @Shadow
    protected abstract Set<EnumFacing> getVisibleFacings(BlockPos pos);

    @Shadow
    protected abstract Vector3f getViewVector(Entity entityIn, double partialTicks);

    @Shadow
    @Nullable
    protected abstract RenderChunk getRenderChunkOffset(BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing);

    @Shadow
    private boolean debugFixTerrainFrustum;

    @Shadow
    protected abstract void fixTerrainFrustum(double x, double y, double z);

    @Shadow
    private double prevRenderSortX;
    @Shadow
    private double prevRenderSortY;
    @Shadow
    private double prevRenderSortZ;

    private List<ContainerLocalRenderInformation> RenderINFOS = Lists.newArrayListWithCapacity(69696);

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected int getRenderedChunks() {
        int i = 0;

        for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.RenderINFOS) {
            CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;

            if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty()) {
                ++i;
            }
        }

        return i;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
        int pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();
        if (this.renderEntitiesStartupCounter > 0) {
            if (pass > 0) return;
            --this.renderEntitiesStartupCounter;
        } else {
            double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
            double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
            double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
            this.world.profiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.prepare(this.world, this.mc.getTextureManager(), this.mc.fontRenderer, this.mc.getRenderViewEntity(), this.mc.objectMouseOver, partialTicks);
            this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRenderer, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);
            if (pass == 0) {
                this.countEntitiesTotal = 0;
                this.countEntitiesRendered = 0;
                this.countEntitiesHidden = 0;
            }
            Entity entity = this.mc.getRenderViewEntity();
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = d3;
            TileEntityRendererDispatcher.staticPlayerY = d4;
            TileEntityRendererDispatcher.staticPlayerZ = d5;
            this.renderManager.setRenderPosition(d3, d4, d5);
            ((iMinecraft) this.mc).MikuEntityRenderer().enableLightmap();
            this.world.profiler.endStartSection("global");
            List<Entity> list = this.world.getLoadedEntityList();
            if (pass == 0) {
                this.countEntitiesTotal = list.size();
            }

            for (int i = 0; i < this.world.weatherEffects.size(); ++i) {
                Entity entity1 = this.world.weatherEffects.get(i);
                if (!entity1.shouldRenderInPass(pass)) continue;
                ++this.countEntitiesRendered;

                if (entity1.isInRangeToRender3d(d0, d1, d2)) {
                    this.renderManager.renderEntityStatic(entity1, partialTicks, false);
                }
            }

            this.world.profiler.endStartSection("entities");
            List<Entity> list1 = Lists.newArrayList();
            List<Entity> list2 = Lists.newArrayList();
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.RenderINFOS) {
                Chunk chunk = this.world.getChunk(renderglobal$containerlocalrenderinformation.renderChunk.getPosition());
                ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty()) {
                    for (Entity entity2 : classinheritancemultimap) {
                        if (!entity2.shouldRenderInPass(pass)) continue;
                        boolean flag = this.renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.isRidingOrBeingRiddenBy(this.mc.player);

                        if (flag) {
                            boolean flag1 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping();

                            if ((entity2 != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || flag1) && (entity2.posY < 0.0D || entity2.posY >= 256.0D || this.world.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(entity2)))) {
                                ++this.countEntitiesRendered;
                                this.renderManager.renderEntityStatic(entity2, partialTicks, false);

                                if (this.isOutlineActive(entity2, entity, camera)) {
                                    list1.add(entity2);
                                }

                                if (this.renderManager.isRenderMultipass(entity2)) {
                                    list2.add(entity2);
                                }
                            }
                        }
                    }
                }
            }

            blockpos$pooledmutableblockpos.release();

            if (!list2.isEmpty()) {
                for (Entity entity3 : list2) {
                    this.renderManager.renderMultipass(entity3, partialTicks);
                }
            }

            if (pass == 0)
                if (this.isRenderEntityOutlines() && (!list1.isEmpty() || this.entityOutlinesRendered)) {
                    this.world.profiler.endStartSection("entityOutlines");
                    this.entityOutlineFramebuffer.framebufferClear();
                    this.entityOutlinesRendered = !list1.isEmpty();

                    if (!list1.isEmpty()) {
                        GlStateManager.depthFunc(519);
                        GlStateManager.disableFog();
                        this.entityOutlineFramebuffer.bindFramebuffer(false);
                        RenderHelper.disableStandardItemLighting();
                        this.renderManager.setRenderOutlines(true);

                        for (Entity value : list1) {
                            this.renderManager.renderEntityStatic(value, partialTicks, false);
                        }

                        this.renderManager.setRenderOutlines(false);
                        RenderHelper.enableStandardItemLighting();
                        GlStateManager.depthMask(false);
                        this.entityOutlineShader.render(partialTicks);
                        GlStateManager.enableLighting();
                        GlStateManager.depthMask(true);
                        GlStateManager.enableFog();
                        GlStateManager.enableBlend();
                        GlStateManager.enableColorMaterial();
                        GlStateManager.depthFunc(515);
                        GlStateManager.enableDepth();
                        GlStateManager.enableAlpha();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(false);
                }

            this.world.profiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            TileEntityRendererDispatcher.instance.preDrawBatch();
            for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 : this.RenderINFOS) {
                List<TileEntity> list3 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();

                if (!list3.isEmpty()) {
                    for (TileEntity tileentity2 : list3) {
                        if (!tileentity2.shouldRenderInPass(pass) || !camera.isBoundingBoxInFrustum(tileentity2.getRenderBoundingBox()))
                            continue;
                        TileEntityRendererDispatcher.instance.render(tileentity2, partialTicks, -1);
                    }
                }
            }

            synchronized (this.setTileEntities) {
                for (TileEntity tileentity : this.setTileEntities) {
                    if (!tileentity.shouldRenderInPass(pass) || !camera.isBoundingBoxInFrustum(tileentity.getRenderBoundingBox()))
                        continue;
                    TileEntityRendererDispatcher.instance.render(tileentity, partialTicks, -1);
                }
            }
            TileEntityRendererDispatcher.instance.drawBatch(pass);

            this.preRenderDamagedBlocks();

            for (DestroyBlockProgress destroyblockprogress : this.damagedBlocks.values()) {
                BlockPos blockpos = destroyblockprogress.getPosition();

                if (this.world.getBlockState(blockpos).getBlock().hasTileEntity()) {
                    TileEntity tileentity1 = this.world.getTileEntity(blockpos);

                    if (tileentity1 instanceof TileEntityChest) {
                        TileEntityChest tileentitychest = (TileEntityChest) tileentity1;

                        if (tileentitychest.adjacentChestXNeg != null) {
                            blockpos = blockpos.offset(EnumFacing.WEST);
                            tileentity1 = this.world.getTileEntity(blockpos);
                        } else if (tileentitychest.adjacentChestZNeg != null) {
                            blockpos = blockpos.offset(EnumFacing.NORTH);
                            tileentity1 = this.world.getTileEntity(blockpos);
                        }
                    }

                    IBlockState iblockstate = this.world.getBlockState(blockpos);

                    if (tileentity1 != null && iblockstate.hasCustomBreakingProgress()) {
                        TileEntityRendererDispatcher.instance.render(tileentity1, partialTicks, destroyblockprogress.getPartialBlockDamage());
                    }
                }
            }

            this.postRenderDamagedBlocks();
            ((iMinecraft) this.mc).MikuEntityRenderer().disableLightmap();
            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
            this.loadRenderers();
        }

        this.world.profiler.startSection("camera");
        double d0 = viewEntity.posX - this.frustumUpdatePosX;
        double d1 = viewEntity.posY - this.frustumUpdatePosY;
        double d2 = viewEntity.posZ - this.frustumUpdatePosZ;

        if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
            this.frustumUpdatePosX = viewEntity.posX;
            this.frustumUpdatePosY = viewEntity.posY;
            this.frustumUpdatePosZ = viewEntity.posZ;
            this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        this.world.profiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        this.renderContainer.initialize(d3, d4, d5);
        this.world.profiler.endStartSection("cull");

        if (this.debugFixedClippingHelper != null) {
            Frustum frustum = new Frustum(this.debugFixedClippingHelper);
            frustum.setPosition(this.debugTerrainFrustumPosition.x, this.debugTerrainFrustumPosition.y, this.debugTerrainFrustumPosition.z);
            camera = frustum;
        }

        ((iMinecraft) this.mc).MikuProfiler().endStartSection("culling");
        BlockPos blockpos1 = new BlockPos(d3, d4 + (double) viewEntity.getEyeHeight(), d5);
        RenderChunk renderchunk = ((iViewFrustum) this.viewFrustum).GetRenderChunk(blockpos1);
        BlockPos blockpos = new BlockPos(MathHelper.floor(d3 / 16.0D) * 16, MathHelper.floor(d4 / 16.0D) * 16, MathHelper.floor(d5 / 16.0D) * 16);
        this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || viewEntity.posX != this.lastViewEntityX || viewEntity.posY != this.lastViewEntityY || viewEntity.posZ != this.lastViewEntityZ || (double) viewEntity.rotationPitch != this.lastViewEntityPitch || (double) viewEntity.rotationYaw != this.lastViewEntityYaw;
        this.lastViewEntityX = viewEntity.posX;
        this.lastViewEntityY = viewEntity.posY;
        this.lastViewEntityZ = viewEntity.posZ;
        this.lastViewEntityPitch = viewEntity.rotationPitch;
        this.lastViewEntityYaw = viewEntity.rotationYaw;
        boolean flag = this.debugFixedClippingHelper != null;
        ((iMinecraft) this.mc).MikuProfiler().endStartSection("update");

        if (!flag && this.displayListEntitiesDirty) {
            this.displayListEntitiesDirty = false;
            this.RenderINFOS = Lists.newArrayList();
            Queue<ContainerLocalRenderInformation> queue = Queues.newArrayDeque();
            Entity.setRenderDistanceWeight(MathHelper.clamp((double) this.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D));
            boolean flag1 = this.mc.renderChunksMany;

            if (renderchunk != null) {
                boolean flag2 = false;
                ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 = new ContainerLocalRenderInformation(renderchunk, null, 0);
                Set<EnumFacing> set1 = this.getVisibleFacings(blockpos1);

                if (set1.size() == 1) {
                    Vector3f vector3f = this.getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    set1.remove(enumfacing);
                }

                if (set1.isEmpty()) {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator) {
                    this.RenderINFOS.add(renderglobal$containerlocalrenderinformation3);
                } else {
                    if (playerSpectator && this.world.getBlockState(blockpos1).isOpaqueCube()) {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    queue.add(renderglobal$containerlocalrenderinformation3);
                }
            } else {
                int i = blockpos1.getY() > 0 ? 248 : 8;

                for (int j = -this.renderDistanceChunks; j <= this.renderDistanceChunks; ++j) {
                    for (int k = -this.renderDistanceChunks; k <= this.renderDistanceChunks; ++k) {
                        RenderChunk renderchunk1 = ((iViewFrustum) this.viewFrustum).GetRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));

                        if (renderchunk1 != null && camera.isBoundingBoxInFrustum(renderchunk1.boundingBox.expand(0.0, blockpos1.getY() > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY, 0.0))) // Forge: fix MC-73139
                        {
                            renderchunk1.setFrameIndex(frameCount);
                            queue.add(new ContainerLocalRenderInformation(renderchunk1, null, 0));
                        }
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().startSection("iteration");

            while (!queue.isEmpty()) {
                ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = queue.poll();
                RenderChunk renderchunk3 = renderglobal$containerlocalrenderinformation1.renderChunk;
                EnumFacing enumfacing2 = renderglobal$containerlocalrenderinformation1.facing;
                this.RenderINFOS.add(renderglobal$containerlocalrenderinformation1);

                for (EnumFacing enumfacing1 : EnumFacing.values()) {
                    RenderChunk renderchunk2 = this.getRenderChunkOffset(blockpos, renderchunk3, enumfacing1);

                    if ((!flag1 || !renderglobal$containerlocalrenderinformation1.hasDirection(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk3.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)) && renderchunk2 != null && renderchunk2.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(renderchunk2.boundingBox)) {
                        ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = new ContainerLocalRenderInformation(renderchunk2, enumfacing1, renderglobal$containerlocalrenderinformation1.counter + 1);
                        renderglobal$containerlocalrenderinformation.setDirection(renderglobal$containerlocalrenderinformation1.setFacing, enumfacing1);
                        queue.add(renderglobal$containerlocalrenderinformation);
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }

        ((iMinecraft) this.mc).MikuProfiler().endStartSection("captureFrustum");

        if (this.debugFixTerrainFrustum) {
            this.fixTerrainFrustum(d3, d4, d5);
            this.debugFixTerrainFrustum = false;
        }

        ((iMinecraft) this.mc).MikuProfiler().endStartSection("rebuildNear");
        Set<RenderChunk> set = this.chunksToUpdate;
        this.chunksToUpdate = Sets.newLinkedHashSet();

        for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 : this.RenderINFOS) {
            RenderChunk renderchunk4 = renderglobal$containerlocalrenderinformation2.renderChunk;

            if (renderchunk4.needsUpdate() || set.contains(renderchunk4)) {
                this.displayListEntitiesDirty = true;
                BlockPos blockpos2 = renderchunk4.getPosition().add(8, 8, 8);
                boolean flag3 = blockpos2.distanceSq(blockpos1) < 768.0D;

                if (net.minecraftforge.common.ForgeModContainer.alwaysSetupTerrainOffThread || (!renderchunk4.needsImmediateUpdate() && !flag3)) {
                    this.chunksToUpdate.add(renderchunk4);
                } else {
                    ((iMinecraft) this.mc).MikuProfiler().startSection("build near");
                    this.renderDispatcher.updateChunkNow(renderchunk4);
                    renderchunk4.clearNeedsUpdate();
                    ((iMinecraft) this.mc).MikuProfiler().endSection();
                }
            }
        }

        this.chunksToUpdate.addAll(set);
        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void setWorldAndLoadRenderers(@Nullable WorldClient worldClientIn) {
        if (this.world != null) {
            try {
                this.world.removeEventListener((RenderGlobal) (Object) this);
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at method:setWorldAndLoadRenderers:removeEventListener");
                t.printStackTrace();
            }
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        try {
            this.renderManager.setWorld(worldClientIn);
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at method:setWorldAndLoadRenderers:setWorld");
            t.printStackTrace();
        }
        this.world = worldClientIn;

        if (worldClientIn != null) {
            try {
                worldClientIn.addEventListener((RenderGlobal) (Object) this);
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at method:setWorldAndLoadRenderers:addEventListener");
                t.printStackTrace();
            }
            try {
                this.loadRenderers();
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at method:setWorldAndLoadRenderers:loadRenderers");
                t.printStackTrace();
            }
        } else {
            try {
                this.chunksToUpdate.clear();
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at method:chunksToUpdate.clear()");
                t.printStackTrace();
            }
            try {
                this.RenderINFOS.clear();
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at method:RenderINFOS.clear()");
                t.printStackTrace();
            }

            if (this.viewFrustum != null) {
                try {
                    this.viewFrustum.deleteGlResources();
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Catch exception at method:viewFrustum.deleteGlResources()");
                    t.printStackTrace();
                }
                this.viewFrustum = null;
            }

            if (this.renderDispatcher != null) {
                try {
                    this.renderDispatcher.stopWorkerThreads();
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Catch exception at method:renderDispatcher.stopWorkerThreads()");
                    t.printStackTrace();
                }
            }

            this.renderDispatcher = null;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public int renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        RenderHelper.disableStandardItemLighting();

        if (blockLayerIn == BlockRenderLayer.TRANSLUCENT) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("translucent_sort");
            double d0 = entityIn.posX - this.prevRenderSortX;
            double d1 = entityIn.posY - this.prevRenderSortY;
            double d2 = entityIn.posZ - this.prevRenderSortZ;

            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D) {
                this.prevRenderSortX = entityIn.posX;
                this.prevRenderSortY = entityIn.posY;
                this.prevRenderSortZ = entityIn.posZ;
                int k = 0;

                for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.RenderINFOS) {
                    if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15) {
                        this.renderDispatcher.updateTransparencyLater(renderglobal$containerlocalrenderinformation.renderChunk);
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }

        ((iMinecraft) this.mc).MikuProfiler().startSection("filterempty");
        int l = 0;
        boolean flag = blockLayerIn == BlockRenderLayer.TRANSLUCENT;
        int i1 = flag ? this.RenderINFOS.size() - 1 : 0;
        int i = flag ? -1 : this.RenderINFOS.size();
        int j1 = flag ? -1 : 1;

        for (int j = i1; j != i; j += j1) {
            RenderChunk renderchunk = (this.RenderINFOS.get(j)).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn)) {
                ++l;
                this.renderContainer.addRenderChunk(renderchunk, blockLayerIn);
            }
        }

        ((iMinecraft) this.mc).MikuProfiler().func_194339_b(() ->
                "render_" + blockLayerIn);
        this.renderBlockLayer(blockLayerIn);
        ((iMinecraft) this.mc).MikuProfiler().endSection();
        return l;
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(BlockRenderLayer blockLayerIn) {
        ((iMinecraft) this.mc).MikuEntityRenderer().enableLightmap();

        if (OpenGlHelper.useVbo()) {
            GlStateManager.glEnableClientState(32884);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(32888);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glEnableClientState(32888);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glEnableClientState(32886);
        }

        this.renderContainer.renderChunkLayer(blockLayerIn);

        if (OpenGlHelper.useVbo()) {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements()) {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int k1 = vertexformatelement.getIndex();

                switch (vertexformatelement$enumusage) {
                    case POSITION:
                        GlStateManager.glDisableClientState(32884);
                        break;
                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                        GlStateManager.glDisableClientState(32888);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;
                    case COLOR:
                        GlStateManager.glDisableClientState(32886);
                        GlStateManager.resetColor();
                }
            }
        }

        ((iMinecraft) this.mc).MikuEntityRenderer().disableLightmap();
    }
}
