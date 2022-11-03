package miku.lib.mixins;

import miku.lib.api.iChunk;
import miku.lib.api.iWorld;
import miku.lib.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(value = World.class)
public abstract class MixinWorld implements iWorld {
    @Shadow protected List<IWorldEventListener> eventListeners;

    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);

    @Shadow @Final public List<Entity> loadedEntityList;

    @Shadow @Final public Profiler profiler;

    @Shadow @Final public List<Entity> weatherEffects;

    @Shadow public abstract void removeEntity(Entity entityIn);

    @Shadow @Final protected List<Entity> unloadedEntityList;

    @Shadow public abstract void updateEntity(Entity ent);

    @Shadow public abstract void onEntityRemoved(Entity entityIn);

    @Shadow private boolean processingLoadedTiles;

    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;

    @Shadow @Final public List<TileEntity> tickableTileEntities;

    @Shadow @Final public List<TileEntity> loadedTileEntityList;

    @Shadow public abstract boolean isBlockLoaded(BlockPos pos, boolean allowEmpty);

    @Shadow @Final private WorldBorder worldBorder;

    @Shadow public abstract void removeTileEntity(BlockPos pos);

    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);

    @Shadow public abstract Chunk getChunk(BlockPos pos);

    @Shadow @Final private List<TileEntity> addedTileEntityList;

    @Shadow public abstract boolean addTileEntity(TileEntity tile);

    @Shadow public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);

    @Shadow protected abstract void tickPlayers();

    public void remove(Entity entity){
        this.loadedEntityList.remove(entity);
        for (IWorldEventListener eventListener : this.eventListeners) {
            eventListener.onEntityRemoved(entity);
        }
        int i = entity.chunkCoordX;
        int j = entity.chunkCoordZ;
        if (this.isChunkLoaded(i, j, true))
        {
            ((iChunk)this.getChunk(i, j)).remove(entity);
        }
    }

    @Inject(at=@At("HEAD"),method = "spawnEntity", cancellable = true)
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isKilling() || EntityUtil.isDEAD(entityIn))cir.setReturnValue(false);
    }

    @Inject(at=@At("HEAD"),method = "onEntityAdded", cancellable = true)
    public void onEntityAdded(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isKilling() || EntityUtil.isDEAD(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "onEntityRemoved", cancellable = true)
    public void onEntityRemoved(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "removeEntity", cancellable = true)
    public void removeEntity(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "removeEntityDangerously", cancellable = true)
    public void removeEntityDangerously(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void updateEntities()
    {
        this.profiler.startSection("entities");
        this.profiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i)
        {
            Entity entity = this.weatherEffects.get(i);

            try
            {
                if(entity.updateBlocked) continue;
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable2)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null)
                {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                }
                else
                {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                if (net.minecraftforge.common.ForgeModContainer.removeErroringEntities)
                {
                    net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport.getCompleteReport());
                    removeEntity(entity);
                }
                else
                    throw new ReportedException(crashreport);
            }

            if (entity.isDead)
            {
                this.weatherEffects.remove(i--);
            }
        }

        this.profiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (Entity entity1 : this.unloadedEntityList) {
            int j = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true)) {
                this.getChunk(j, k1).removeEntity(entity1);
            }
        }

        for (Entity entity : this.unloadedEntityList) {
            this.onEntityRemoved(entity);
        }

        this.unloadedEntityList.clear();
        this.tickPlayers();
        this.profiler.endStartSection("regular");

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            Entity entity2 = this.loadedEntityList.get(i1);
            Entity entity3 = entity2.getRidingEntity();

            if (entity3 != null)
            {
                if (!entity3.isDead && entity3.isPassenger(entity2))
                {
                    continue;
                }

                entity2.dismountRidingEntity();
            }

            this.profiler.startSection("tick");

            if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP))
            {
                try
                {
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackStart(entity2);
                    this.updateEntity(entity2);
                    net.minecraftforge.server.timings.TimeTracker.ENTITY_UPDATE.trackEnd(entity2);
                }
                catch (Throwable throwable1)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory1);
                    if (net.minecraftforge.common.ForgeModContainer.removeErroringEntities)
                    {
                        net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport1.getCompleteReport());
                        removeEntity(entity2);
                    }
                    else
                        throw new ReportedException(crashreport1);
                }
            }

            this.profiler.endSection();
            this.profiler.startSection("remove");

            if (entity2.isDead)
            {
                int l1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                if (entity2.addedToChunk && this.isChunkLoaded(l1, i2, true))
                {
                    this.getChunk(l1, i2).removeEntity(entity2);
                }

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.profiler.endSection();
        }

        this.profiler.endStartSection("blockEntities");

        this.processingLoadedTiles = true; //FML Move above remove to prevent CMEs

        if (!this.tileEntitiesToBeRemoved.isEmpty())
        {
            for (TileEntity tile : tileEntitiesToBeRemoved)
            {
                tile.onChunkUnload();
            }

            // forge: faster "contains" makes this removal much more efficient
            java.util.Set<TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorld())
            {
                BlockPos blockpos = tileentity.getPos();

                if (this.isBlockLoaded(blockpos, false) && this.worldBorder.contains(blockpos)) //Forge: Fix TE's getting an extra tick on the client side....
                {
                    try
                    {
                        this.profiler.func_194340_a(() ->
                                String.valueOf(TileEntity.getKey(tileentity.getClass())));
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                        ((ITickable)tileentity).update();
                        net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
                        this.profiler.endSection();
                    }
                    catch (Throwable throwable)
                    {
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory2);
                        if (net.minecraftforge.common.ForgeModContainer.removeErroringTileEntities)
                        {
                            net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport2.getCompleteReport());
                            tileentity.invalidate();
                            this.removeTileEntity(tileentity.getPos());
                        }
                        else
                            throw new ReportedException(crashreport2);
                    }
                }
            }

            if (tileentity.isInvalid())
            {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);

                if (this.isBlockLoaded(tileentity.getPos()))
                {
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                    Chunk chunk = this.getChunk(tileentity.getPos());
                    if (chunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity)
                        chunk.removeTileEntity(tileentity.getPos());
                }
            }
        }

        this.processingLoadedTiles = false;
        this.profiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty())
        {
            for (TileEntity tileentity1 : this.addedTileEntityList) {
                if (!tileentity1.isInvalid()) {
                    if (!this.loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos())) {
                        Chunk chunk = this.getChunk(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            this.addedTileEntityList.clear();
        }

        this.profiler.endSection();
        this.profiler.endSection();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void unloadEntities(Collection<Entity> entityCollection){
        for(Entity en : entityCollection){
            if(!EntityUtil.isProtected(en))unloadedEntityList.add(en);
        }
    }
}
