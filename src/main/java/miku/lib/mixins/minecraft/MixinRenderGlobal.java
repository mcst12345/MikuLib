package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import miku.lib.client.api.iMinecraft;
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
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
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


    @Inject(at = @At("HEAD"), method = "spawnParticle(IZZDDDDDD[I)V", cancellable = true)
    public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] parameters, CallbackInfo ci) {
        if (!Sqlite.GetBooleanFromTable("particle", "RENDER_CONFIG")) {
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason fastRender :)
     */
    @Overwrite
    public boolean isRenderEntityOutlines() {
        if (Sqlite.GetBooleanFromTable("fast", "RENDER_CONFIG")) {
            return false;
        }
        return this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && ((iMinecraft) this.mc).MikuPlayer() != null;
    }

    @Shadow
    private Framebuffer entityOutlineFramebuffer;
    @Shadow
    private boolean entityOutlinesRendered;
    @Shadow
    private ShaderGroup entityOutlineShader;

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
    private Set<RenderChunk> chunksToUpdate;
    @Shadow
    private ChunkRenderDispatcher renderDispatcher;
    private final List<ContainerLocalRenderInformation> RenderINFOS = Lists.newArrayListWithCapacity(69696);

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
                            if (Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("render_info", "LOG_CONFIG")) {
                                System.out.println("MikuInfo:ignoring entity:" + entity2.getClass());
                            }
                            continue;
                        }
                        boolean flag = this.renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.isRidingOrBeingRiddenBy(((iMinecraft) this.mc).MikuPlayer());

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
                            if (Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("render_info", "LOG_CONFIG")) {
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
}
