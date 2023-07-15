package miku.lib.mixins.minecraft;

import com.google.common.collect.ImmutableSetMultimap;
import miku.lib.api.*;
import miku.lib.effect.MikuEffect;
import miku.lib.item.SpecialItem;
import miku.lib.sqlite.Sqlite;
import miku.lib.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mixin(value = World.class)
public abstract class MixinWorld implements iWorld {
    private static final List<MikuEffect> effects = new ArrayList<>();
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

    @Shadow @Final public boolean isRemote;

    @Shadow(remap = false) public abstract ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks();

    @Shadow protected abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);

    public void AddEffect(MikuEffect effect){
        effects.add(effect);
    }

    public void remove(Entity entity){
        this.loadedEntityList.remove(entity);
        weatherEffects.remove(entity);
        for (IWorldEventListener eventListener : this.eventListeners) {
            eventListener.onEntityRemoved(entity);
        }
        int i = entity.chunkCoordX;
        int j = entity.chunkCoordZ;
        if (this.isChunkLoaded(i, j, true))
        {
            ((iChunk)this.getChunk(i, j)).remove(entity);
        }
        if(isRemote){
            ((iWorldClient)FMLClientHandler.instance().getWorldClient()).REMOVE(entity);
        }
    }

    @Inject(at=@At("HEAD"),method = "spawnEntity", cancellable = true)
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir){
        if((Sqlite.IS_MOB_BANNED(entityIn) || EntityUtil.isKilling() || EntityUtil.isDEAD(entityIn) || (SpecialItem.isTimeStop()) && !EntityUtil.isProtected(entityIn)))cir.setReturnValue(false);
        if((boolean)Sqlite.GetValueFromTable("debug","CONFIG",0))System.out.println(entityIn.getClass().toString());
    }

    @Inject(at=@At("HEAD"),method = "onEntityAdded", cancellable = true)
    public void onEntityAdded(Entity entityIn, CallbackInfo ci){
        if((Sqlite.IS_MOB_BANNED(entityIn) || EntityUtil.isKilling() || EntityUtil.isDEAD(entityIn) || (SpecialItem.isTimeStop()) && !EntityUtil.isProtected(entityIn)))ci.cancel();
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

    @Inject(at=@At("TAIL"),method = "getEntityByID", cancellable = true)
    public void getEntityByID(int id, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isDEAD(cir.getReturnValue()))cir.setReturnValue(null);
    }



    public List<MikuEffect> GetEntityEffects(EntityLivingBase entity){;
        List<MikuEffect> result = new ArrayList<>();
        for(MikuEffect effect : effects){
            if(effect.entity == entity)result.add(effect);
        }
        return result;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void updateEntities()
    {
        EntityUtil.REMOVE((World)(Object)this);
        if(EntityUtil.isKilling())return;
        this.profiler.startSection("entities");
        this.profiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i)
        {
            Entity entity = this.weatherEffects.get(i);

            try
            {
                if(entity.updateBlocked || ((iEntity)entity).isTimeStop() || (SpecialItem.isTimeStop() && !EntityUtil.isProtected(entity))) continue;
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

            if (entity.isDead && !EntityUtil.isProtected(entity) && !SpecialItem.isTimeStop() && !(entity instanceof ProtectedEntity))
            {
                this.weatherEffects.remove(i--);
            }
        }

        this.profiler.endStartSection("remove");
        if(!SpecialItem.isTimeStop())this.loadedEntityList.removeAll(this.unloadedEntityList);

        if(!SpecialItem.isTimeStop())for (Entity entity1 : this.unloadedEntityList) {
            int j = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true)) {
                this.getChunk(j, k1).removeEntity(entity1);
            }
        }

        if(!SpecialItem.isTimeStop())for (Entity entity : this.unloadedEntityList) {
            this.onEntityRemoved(entity);
        }

        if(!SpecialItem.isTimeStop())this.unloadedEntityList.clear();
        this.tickPlayers();
        this.profiler.endStartSection("regular");

        for(MikuEffect effect : effects){
            if(effect.shouldRemove())effects.remove(effect);
            if(effect.shouldPerform())effect.perform();
        }

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            Entity entity2 = this.loadedEntityList.get(i1);
            if(SpecialItem.isTimeStop() && !EntityUtil.isProtected(entity2))continue;
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
                    throwable1.printStackTrace();
                    if (net.minecraftforge.common.ForgeModContainer.removeErroringEntities)
                    {
                        removeEntity(entity2);
                    }
                }
            }

            this.profiler.endSection();
            this.profiler.startSection("remove");

            if (entity2.isDead && !EntityUtil.isProtected(entity2))
            {
                int l1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                if (entity2.addedToChunk && this.isChunkLoaded(l1, i2, true))
                {
                    this.getChunk(l1, i2).removeEntity(entity2);
                }

                this.loadedEntityList.remove(entity2);
                this.onEntityRemoved(entity2);
            }

            this.profiler.endSection();
        }

        if(!SpecialItem.isTimeStop()){
            this.profiler.endStartSection("blockEntities");

            this.processingLoadedTiles = true; //FML Move above remove to prevent CMEs

            if (!this.tileEntitiesToBeRemoved.isEmpty()) {
                for (TileEntity tile : tileEntitiesToBeRemoved) {
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

            while (iterator.hasNext()) {
                TileEntity tileentity = iterator.next();

                if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                    BlockPos blockpos = tileentity.getPos();

                    if (this.isBlockLoaded(blockpos, false) && this.worldBorder.contains(blockpos)) //Forge: Fix TE's getting an extra tick on the client side....
                    {
                        try {
                            this.profiler.func_194340_a(() ->
                                    String.valueOf(TileEntity.getKey(tileentity.getClass())));
                            net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
                            ((ITickable) tileentity).update();
                            net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
                            this.profiler.endSection();
                        } catch (Throwable throwable) {
                            CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                            CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                            tileentity.addInfoToCrashReport(crashreportcategory2);
                            if (net.minecraftforge.common.ForgeModContainer.removeErroringTileEntities) {
                                net.minecraftforge.fml.common.FMLLog.log.fatal("{}", crashreport2.getCompleteReport());
                                tileentity.invalidate();
                                this.removeTileEntity(tileentity.getPos());
                            } else
                                throw new ReportedException(crashreport2);
                        }
                    }
                }

                if (tileentity.isInvalid()) {
                    iterator.remove();
                    this.loadedTileEntityList.remove(tileentity);

                    if (this.isBlockLoaded(tileentity.getPos())) {
                        //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                        Chunk chunk = this.getChunk(tileentity.getPos());
                        if (chunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity)
                            chunk.removeTileEntity(tileentity.getPos());
                    }
                }
            }

            this.processingLoadedTiles = false;
            this.profiler.endStartSection("pendingBlockEntities");

            if (!this.addedTileEntityList.isEmpty()) {
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
        }
        this.profiler.endSection();
        //if(MikuCore.RescueMode)EntityUtil.ClearBadEntities(((World) (Object) this));
        EntityUtil.REMOVE((World)(Object)this);
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

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate)
    {
        if(EntityUtil.isDEAD(entityIn))return;
        if (!(entityIn instanceof EntityPlayer))
        {
            int j2 = MathHelper.floor(entityIn.posX);
            int k2 = MathHelper.floor(entityIn.posZ);

            boolean isForced = !this.isRemote && getPersistentChunks().containsKey(new net.minecraft.util.math.ChunkPos(j2 >> 4, k2 >> 4));
            int range = isForced ? 0 : 32;
            boolean canUpdate = !forceUpdate || this.isAreaLoaded(j2 - range, 0, k2 - range, j2 + range, 0, k2 + range, true);
            if (!canUpdate) canUpdate = net.minecraftforge.event.ForgeEventFactory.canEntityUpdate(entityIn);

            if (!canUpdate)
            {
                return;
            }
        }

        entityIn.lastTickPosX = entityIn.posX;
        entityIn.lastTickPosY = entityIn.posY;
        entityIn.lastTickPosZ = entityIn.posZ;
        entityIn.prevRotationYaw = entityIn.rotationYaw;
        entityIn.prevRotationPitch = entityIn.rotationPitch;

        if (forceUpdate && entityIn.addedToChunk)
        {
            ++entityIn.ticksExisted;

            if (entityIn.isRiding())
            {
                entityIn.updateRidden();
            }
            else
            {
                if(!entityIn.updateBlocked) {
                    try {
                        entityIn.onUpdate();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        this.profiler.startSection("chunkCheck");

        if (Double.isNaN(entityIn.posX) || Double.isInfinite(entityIn.posX))
        {
            entityIn.posX = entityIn.lastTickPosX;
        }

        if (Double.isNaN(entityIn.posY) || Double.isInfinite(entityIn.posY))
        {
            entityIn.posY = entityIn.lastTickPosY;
        }

        if (Double.isNaN(entityIn.posZ) || Double.isInfinite(entityIn.posZ))
        {
            entityIn.posZ = entityIn.lastTickPosZ;
        }

        if (Double.isNaN(entityIn.rotationPitch) || Double.isInfinite(entityIn.rotationPitch))
        {
            entityIn.rotationPitch = entityIn.prevRotationPitch;
        }

        if (Double.isNaN(entityIn.rotationYaw) || Double.isInfinite(entityIn.rotationYaw))
        {
            entityIn.rotationYaw = entityIn.prevRotationYaw;
        }

        int i3 = MathHelper.floor(entityIn.posX / 16.0D);
        int j3 = MathHelper.floor(entityIn.posY / 16.0D);
        int k3 = MathHelper.floor(entityIn.posZ / 16.0D);

        if (!entityIn.addedToChunk || entityIn.chunkCoordX != i3 || entityIn.chunkCoordY != j3 || entityIn.chunkCoordZ != k3)
        {
            if (entityIn.addedToChunk && this.isChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true))
            {
                this.getChunk(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }

            if (!entityIn.setPositionNonDirty() && !this.isChunkLoaded(i3, k3, true))
            {
                entityIn.addedToChunk = false;
            }
            else
            {
                this.getChunk(i3, k3).addEntity(entityIn);
            }
        }

        this.profiler.endSection();

        if (forceUpdate && entityIn.addedToChunk)
        {
            for (Entity entity4 : entityIn.getPassengers())
            {
                if (!entity4.isDead && entity4.getRidingEntity() == entityIn)
                {
                    this.updateEntity(entity4);
                }
                else
                {
                    entity4.dismountRidingEntity();
                }
            }
        }
    }
}
