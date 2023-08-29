package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.api.iViewFrustum;
import miku.lib.client.util.ContainerLocalRenderInformation;
import miku.lib.common.api.iWorld;
import miku.lib.common.sqlite.Sqlite;
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
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    @Nullable
    private RenderChunk GetRenderChunkOffset(BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing) {
        BlockPos blockpos = renderChunkBase.getBlockPosOffset16(facing);

        if (MathHelper.abs(playerPos.getX() - blockpos.getX()) > this.renderDistanceChunks * 16) {
            return null;
        } else if (blockpos.getY() >= 0 && blockpos.getY() < 256) {
            return MathHelper.abs(playerPos.getZ() - blockpos.getZ()) > this.renderDistanceChunks * 16 ? null : ((iViewFrustum) this.viewFrustum).GetRenderChunk(blockpos);
        } else {
            return null;
        }
    }

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

    @Shadow
    protected abstract void renderSkyEnd();

    @Shadow
    private boolean vboEnabled;
    @Shadow
    private VertexBuffer skyVBO;
    @Shadow
    private int glSkyList;
    @Shadow
    @Final
    private TextureManager renderEngine;
    @Shadow
    @Final
    private static ResourceLocation SUN_TEXTURES;
    @Shadow
    @Final
    private static ResourceLocation MOON_PHASES_TEXTURES;
    @Shadow
    private VertexBuffer starVBO;
    @Shadow
    private int starGLCallList;
    @Shadow
    private VertexBuffer sky2VBO;
    @Shadow
    private int glSkyList2;
    @Shadow
    private int cloudTickCounter;

    @Shadow
    protected abstract void renderCloudsFancy(float partialTicks, int pass, double x, double y, double z);

    @Shadow
    @Final
    private static ResourceLocation CLOUDS_TEXTURES;
    private List<ContainerLocalRenderInformation> RenderINFOS = Lists.newArrayListWithCapacity(69696);

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public int getRenderedChunks() {
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

            for (Entity e : ((iWorld) this.world).getProtectedEntities()) {
                if (!list1.contains(e)) {
                    if (Sqlite.DEBUG() && !(e instanceof EntityPlayer)) {
                        System.out.println("Adding entity:" + e.getClass());
                    }
                    list1.add(e);
                }
            }

            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.RenderINFOS) {
                Chunk chunk = this.world.getChunk(renderglobal$containerlocalrenderinformation.renderChunk.getPosition());
                ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty()) {
                    for (Entity entity2 : classinheritancemultimap) {
                        if (!entity2.shouldRenderInPass(pass)) {
                            if (Sqlite.DEBUG()) {
                                if (!(entity2 instanceof EntityPlayer))
                                    System.out.println("MikuInfo:ignoring entity:" + entity2.getClass());
                            }
                            continue;
                        }
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
                        } else {
                            if (Sqlite.DEBUG()) {
                                if (!(entity2 instanceof EntityPlayer))
                                    System.out.println("MikuInfo:ignoring entity:" + entity2.getClass());
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
                    RenderChunk renderchunk2 = this.GetRenderChunkOffset(blockpos, renderchunk3, enumfacing1);

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
    public void renderBlockLayer(BlockRenderLayer blockLayerIn) {
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

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void renderSky(float partialTicks, int pass) {
        net.minecraftforge.client.IRenderHandler renderer = this.world.provider.getSkyRenderer();
        if (renderer != null) {
            renderer.render(partialTicks, world, mc);
            return;
        }

        if (((iMinecraft) this.mc).MikuWorld().provider.getDimensionType().getId() == 1) {
            this.renderSkyEnd();
        } else if (((iMinecraft) this.mc).MikuWorld().provider.isSurfaceWorld()) {
            GlStateManager.disableTexture2D();
            Vec3d vec3d = this.world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
            float f = (float) vec3d.x;
            float f1 = (float) vec3d.y;
            float f2 = (float) vec3d.z;

            if (pass != 2) {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }

            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(f, f1, f2);

            if (this.vboEnabled) {
                this.skyVBO.bindBuffer();
                GlStateManager.glEnableClientState(32884);
                GlStateManager.glVertexPointer(3, 5126, 12, 0);
                this.skyVBO.drawArrays(7);
                this.skyVBO.unbindBuffer();
                GlStateManager.glDisableClientState(32884);
            } else {
                GlStateManager.callList(this.glSkyList);
            }

            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = this.world.provider.calcSunriseSunsetColors(this.world.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null) {
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(this.world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float f6 = afloat[0];
                float f7 = afloat[1];
                float f8 = afloat[2];

                if (pass != 2) {
                    float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();

                for (int j2 = 0; j2 <= 16; ++j2) {
                    float f21 = (float) j2 * ((float) Math.PI * 2F) / 16.0F;
                    float f12 = MathHelper.sin(f21);
                    float f13 = MathHelper.cos(f21);
                    bufferbuilder.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            float f16 = 1.0F - this.world.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f16);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
            float f17 = 30.0F;
            this.renderEngine.bindTexture(SUN_TEXTURES);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-f17, 100.0D, -f17).tex(0.0D, 0.0D).endVertex();
            bufferbuilder.pos(f17, 100.0D, -f17).tex(1.0D, 0.0D).endVertex();
            bufferbuilder.pos(f17, 100.0D, f17).tex(1.0D, 1.0D).endVertex();
            bufferbuilder.pos(-f17, 100.0D, f17).tex(0.0D, 1.0D).endVertex();
            tessellator.draw();
            f17 = 20.0F;
            this.renderEngine.bindTexture(MOON_PHASES_TEXTURES);
            int k1 = this.world.getMoonPhase();
            int i2 = k1 % 4;
            int k2 = k1 / 4 % 2;
            float f22 = (float) (i2) / 4.0F;
            float f23 = (float) (k2) / 2.0F;
            float f24 = (float) (i2 + 1) / 4.0F;
            float f14 = (float) (k2 + 1) / 2.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(-f17, -100.0D, f17).tex(f24, f14).endVertex();
            bufferbuilder.pos(f17, -100.0D, f17).tex(f22, f14).endVertex();
            bufferbuilder.pos(f17, -100.0D, -f17).tex(f22, f23).endVertex();
            bufferbuilder.pos(-f17, -100.0D, -f17).tex(f24, f23).endVertex();
            tessellator.draw();
            GlStateManager.disableTexture2D();
            float f15 = this.world.getStarBrightness(partialTicks) * f16;

            if (f15 > 0.0F) {
                GlStateManager.color(f15, f15, f15, f15);

                if (this.vboEnabled) {
                    this.starVBO.bindBuffer();
                    GlStateManager.glEnableClientState(32884);
                    GlStateManager.glVertexPointer(3, 5126, 12, 0);
                    this.starVBO.drawArrays(7);
                    this.starVBO.unbindBuffer();
                    GlStateManager.glDisableClientState(32884);
                } else {
                    GlStateManager.callList(this.starGLCallList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();
            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d3 = this.mc.player.getPositionEyes(partialTicks).y - this.world.getHorizon();

            if (d3 < 0.0D) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (this.vboEnabled) {
                    this.sky2VBO.bindBuffer();
                    GlStateManager.glEnableClientState(32884);
                    GlStateManager.glVertexPointer(3, 5126, 12, 0);
                    this.sky2VBO.drawArrays(7);
                    this.sky2VBO.unbindBuffer();
                    GlStateManager.glDisableClientState(32884);
                } else {
                    GlStateManager.callList(this.glSkyList2);
                }

                GlStateManager.popMatrix();
                float f19 = -((float) (d3 + 65.0D));
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (this.world.provider.isSkyColored()) {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            } else {
                GlStateManager.color(f, f1, f2);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float) (d3 - 16.0D)), 0.0F);
            GlStateManager.callList(this.glSkyList2);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
        }
    }

    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public void renderClouds(float partialTicks, int pass, double x, double y, double z) {
        if (net.minecraftforge.fml.client.FMLClientHandler.instance().renderClouds(this.cloudTickCounter, partialTicks))
            return;
        if (((iMinecraft) this.mc).MikuWorld().provider.isSurfaceWorld()) {
            if (this.mc.gameSettings.shouldRenderClouds() == 2) {
                this.renderCloudsFancy(partialTicks, pass, x, y, z);
            } else {
                GlStateManager.disableCull();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                this.renderEngine.bindTexture(CLOUDS_TEXTURES);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                Vec3d vec3d = this.world.getCloudColour(partialTicks);
                float f = (float) vec3d.x;
                float f1 = (float) vec3d.y;
                float f2 = (float) vec3d.z;

                if (pass != 2) {
                    float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                    float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                    float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                    f = f3;
                    f1 = f4;
                    f2 = f5;
                }

                double d5 = (float) this.cloudTickCounter + partialTicks;
                double d3 = x + d5 * 0.029999999329447746D;
                int i2 = MathHelper.floor(d3 / 2048.0D);
                int j2 = MathHelper.floor(z / 2048.0D);
                d3 = d3 - (double) (i2 * 2048);
                double lvt_22_1_ = z - (double) (j2 * 2048);
                float f6 = this.world.provider.getCloudHeight() - (float) y + 0.33F;
                float f7 = (float) (d3 * 4.8828125E-4D);
                float f8 = (float) (lvt_22_1_ * 4.8828125E-4D);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                for (int k2 = -256; k2 < 256; k2 += 32) {
                    for (int l2 = -256; l2 < 256; l2 += 32) {
                        bufferbuilder.pos(k2, f6, l2 + 32).tex((float) (k2) * 4.8828125E-4F + f7, (float) (l2 + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                        bufferbuilder.pos(k2 + 32, f6, l2 + 32).tex((float) (k2 + 32) * 4.8828125E-4F + f7, (float) (l2 + 32) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                        bufferbuilder.pos(k2 + 32, f6, l2).tex((float) (k2 + 32) * 4.8828125E-4F + f7, (float) (l2) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                        bufferbuilder.pos(k2, f6, l2).tex((float) (k2) * 4.8828125E-4F + f7, (float) (l2) * 4.8828125E-4F + f8).color(f, f1, f2, 0.8F).endVertex();
                    }
                }

                tessellator.draw();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.enableCull();
            }
        }
    }
}
