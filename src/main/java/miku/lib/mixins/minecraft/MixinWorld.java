package miku.lib.mixins.minecraft;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import miku.lib.client.api.iWorldClient;
import miku.lib.common.api.iChunk;
import miku.lib.common.api.iEntity;
import miku.lib.common.api.iWorld;
import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.effect.MikuEffect;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.FieldUtil;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

@Mixin(value = World.class)
public abstract class MixinWorld implements iWorld {
    /*
     * WARNING:Should only be called from server side.
     */
    @Override
    public void reload(ISaveHandler saveHandler) {
        if (this.mapStorage != null) {
            this.mapStorage = new MapStorage(saveHandler);
        }
        if (this.perWorldStorage != null) {
            this.perWorldStorage = new MapStorage(new net.minecraftforge.common.WorldSpecificSaveHandler((WorldServer) (Object) this, saveHandler));
        }
    }

    private static final List<Entity> toSpawn = new ArrayList<>();
    private boolean timeStop = false;

    public void SetTimeStop() {
        timeStop = !timeStop;
    }

    public boolean isTimeStop() {
        return timeStop;
    }

    @Override
    public void summonEntity(Entity entity) {
        int i = MathHelper.floor(entity.posX / 16.0D);
        int j = MathHelper.floor(entity.posZ / 16.0D);
        this.getChunk(i, j).addEntity(entity);
        this.loadedEntityList.add(entity);
        this.onEntityAdded(entity);
    }

    @Override
    public List<Entity> getProtectedEntities() {
        return ImmutableList.copyOf(protected_entities);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
        if (MikuInsaneMode.isMikuInsaneMode() || TimeStopUtil.isTimeStop()) return;
        this.scheduledUpdatesAreImmediate = true;
        state.getBlock().updateTick((World) (Object) this, pos, state, random);
        this.scheduledUpdatesAreImmediate = false;
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public boolean addWeatherEffect(Entity entityIn) {
        if (EntityUtil.isDEAD(entityIn)) return false;
        if (MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (World) (Object) this)))
            return false;
        this.weatherEffects.add(entityIn);
        return true;
    }

    private final HashSet<Entity> protected_entities = new HashSet<>();

    public boolean HasEffect(EntityLivingBase entity) {
        for (MikuEffect effect : effects) {
            if (effect.entity == entity) return true;
        }
        return false;
    }

    private static final List<MikuEffect> effects = new ArrayList<>();
    @Shadow
    protected List<IWorldEventListener> eventListeners;

    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);

    @Mutable
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

    @Shadow
    public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);

    @Shadow
    protected abstract void tickPlayers();

    @Shadow
    @Final
    public boolean isRemote;

    @Shadow(remap = false)
    public abstract ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks();

    @Shadow
    protected abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);

    @Shadow
    protected boolean scheduledUpdatesAreImmediate;

    @Shadow(remap = false)
    public boolean restoringBlockSnapshots;

    @Shadow
    @Final
    public List<EntityPlayer> playerEntities;

    @Shadow
    public abstract void updateAllPlayersSleepingFlag();

    @Shadow
    public abstract List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb);

    @Shadow
    protected abstract boolean getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb, boolean p_191504_3_, @Nullable List<AxisAlignedBB> outList);

    @Shadow
    protected MapStorage mapStorage;

    @Shadow
    protected MapStorage perWorldStorage;

    public void AddEffect(MikuEffect effect) {
        effects.add(effect);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB aabb) {
        List<AxisAlignedBB> list = Lists.newArrayList();
        this.getCollisionBoxes(entityIn, aabb, false, list);

        if (entityIn != null) {
            List<Entity> list1 = this.getEntitiesWithinAABBExcludingEntity(entityIn, aabb.grow(0.25D));

            for (Entity entity : list1) {
                if (!entityIn.isRidingSameEntity(entity)) {
                    AxisAlignedBB axisalignedbb = entity.getCollisionBoundingBox();

                    if (axisalignedbb != null && axisalignedbb.intersects(aabb)) {
                        list.add(axisalignedbb);
                    }

                    axisalignedbb = entityIn.getCollisionBox(entity);

                    if (axisalignedbb != null && axisalignedbb.intersects(aabb)) {
                        list.add(axisalignedbb);
                    }
                }
            }
        }
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.GetCollisionBoxesEvent((World) (Object) this, entityIn, aabb, list));
        return list;
    }

    @SuppressWarnings("unchecked")
    public void remove(Entity entity) {
        long tmp;
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_72996_f);
            ((List<Entity>) Launch.UNSAFE.getObjectVolatile(this, tmp)).remove(entity);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_73007_j);
            ((List<Entity>) Launch.UNSAFE.getObjectVolatile(this, tmp)).remove(entity);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_73021_x);
            List<IWorldEventListener> list = (List<IWorldEventListener>) Launch.UNSAFE.getObjectVolatile(this, tmp);
            for (IWorldEventListener eventListener : list) {
                eventListener.onEntityRemoved(entity);
            }
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }

        int i = entity.chunkCoordX;
        int j = entity.chunkCoordZ;
        if (this.isChunkLoaded(i, j, true)) {
            ((iChunk) this.getChunk(i, j)).remove(entity);
        }
        if (isRemote) {
            ((iWorldClient) FMLClientHandler.instance().getWorldClient()).REMOVE(entity);
        }
    }


    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean spawnEntity(Entity entityIn) {
        if (entityIn == null) return false;
        if ((Sqlite.IS_MOB_BANNED(entityIn) || EntityUtil.isDEAD(entityIn))) {
            if (Sqlite.DEBUG()) System.out.println("MikuInfo:Ignoring entity:" + entityIn.getClass());
            return false;
        }
        if ((TimeStopUtil.isTimeStop()) && !EntityUtil.isProtected(entityIn)) {
            if (!EntityUtil.isGoodEntity(entityIn)) {
                toSpawn.add(entityIn);
                return true;
            }
        }
        if (EntityUtil.isKilling() && !EntityUtil.isGoodEntity(entityIn)) return false;
        if (Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("entity_info", "LOG_CONFIG")) {
            System.out.println(entityIn.getClass().toString());
            Throwable t = new Throwable();
            t.fillInStackTrace();
            t.printStackTrace();
            System.out.println("\n");
            Stream.of(Thread.currentThread().getStackTrace()).forEach(System.out::println);
        }
        // Do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.isRemote && (entityIn instanceof net.minecraft.entity.item.EntityItem && this.restoringBlockSnapshots))
            return false;

        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer) {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, false)) {
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityIn;
                this.playerEntities.add(entityplayer);
                this.updateAllPlayersSleepingFlag();
            }

            if (MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (World) (Object) this)) && !flag)
                return false;

            this.getChunk(i, j).addEntity(entityIn);
            this.loadedEntityList.add(entityIn);
            this.onEntityAdded(entityIn);
            return true;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public void onEntityAdded(Entity entityIn) {
        if ((Sqlite.IS_MOB_BANNED(entityIn) || EntityUtil.isDEAD(entityIn))) {
            if (Sqlite.DEBUG()) System.out.println("MikuInfo:Ignoring entity:" + entityIn.getClass());
            return;
        }
        if (EntityUtil.isKilling() && !EntityUtil.isGoodEntity(entityIn)) {
            return;
        }
        if ((TimeStopUtil.isTimeStop()) && !EntityUtil.isProtected(entityIn)) {
            if (!EntityUtil.isGoodEntity(entityIn)) {
                toSpawn.add(entityIn);
                return;
            }
        }
        for (IWorldEventListener eventListener : this.eventListeners) {
            eventListener.onEntityAdded(entityIn);
        }
        entityIn.onAddedToWorld();
    }


    @Inject(at = @At("HEAD"), method = "onEntityRemoved", cancellable = true)
    public void onEntityRemoved(Entity entityIn, CallbackInfo ci) {
        if (EntityUtil.isProtected(entityIn)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "removeEntity", cancellable = true)
    public void removeEntity(Entity entityIn, CallbackInfo ci) {
        if (EntityUtil.isProtected(entityIn)) ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "removeEntityDangerously", cancellable = true)
    public void removeEntityDangerously(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at=@At("TAIL"),method = "getEntityByID", cancellable = true)
    public void getEntityByID(int id, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isDEAD(cir.getReturnValue()))cir.setReturnValue(null);
    }



    public List<MikuEffect> GetEntityEffects(EntityLivingBase entity){
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
    public void updateEntities() {
        boolean stop = TimeStopUtil.isTimeStop();
        if (loadedEntityList.getClass() != ArrayList.class) {
            loadedEntityList = new ArrayList<>();
        }
        for (Entity e : protected_entities) {
            if (loadedEntityList.contains(e)) continue;
            loadedEntityList.add(e);
        }
        EntityUtil.REMOVE((World) (Object) this);
        if (EntityUtil.isKilling()) return;

        if (!stop) {
            Iterator<Entity> iterator = toSpawn.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                boolean result = spawnEntity(entity);
                if (result) iterator.remove();
            }
        }

        if (MikuLib.isLAIN()) {
            loadedEntityList.removeIf(e -> e instanceof EntityMob);
        }

        this.profiler.startSection("entities");
        this.profiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i) {
            Entity entity = this.weatherEffects.get(i);

            try {
                if (entity.updateBlocked || ((iEntity) entity).isTimeStop() || ((stop || MikuInsaneMode.isMikuInsaneMode()) && !EntityUtil.isProtected(entity)))
                    continue;
                ++entity.ticksExisted;
                entity.onUpdate();
            }
            catch (Throwable throwable2) {
                System.out.println("MikuWarn:Catch exception when updating entity:" + entity.getName() + "," + entity.getClass());
                removeEntity(entity);
            }

            if (entity.isDead && !EntityUtil.isProtected(entity) && !TimeStopUtil.isTimeStop()) {
                this.weatherEffects.remove(i--);
            }
        }

        this.profiler.endStartSection("remove");
        if (!TimeStopUtil.isTimeStop()) this.loadedEntityList.removeAll(this.unloadedEntityList);

        if (!TimeStopUtil.isTimeStop()) for (Entity entity1 : this.unloadedEntityList) {
            int j = entity1.chunkCoordX;
            int k1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true)) {
                this.getChunk(j, k1).removeEntity(entity1);
            }
        }

        if (!TimeStopUtil.isTimeStop()) for (Entity entity : this.unloadedEntityList) {
            this.onEntityRemoved(entity);
        }

        if (!TimeStopUtil.isTimeStop()) this.unloadedEntityList.clear();
        this.tickPlayers();
        this.profiler.endStartSection("regular");

        for(MikuEffect effect : effects){
            if(effect.shouldRemove())effects.remove(effect);
            if(effect.shouldPerform())effect.perform();
        }

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            Entity entity2 = this.loadedEntityList.get(i1);
            if (EntityUtil.isProtected(entity2)) {
                protected_entities.add(entity2);
            }
            if (stop && !EntityUtil.isProtected(entity2)) continue;
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

        if (!stop && !MikuInsaneMode.isMikuInsaneMode()) {
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
        EntityUtil.REMOVE((World)(Object)this);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void unloadEntities(Collection<Entity> entityCollection) {
        for (Entity en : entityCollection) {
            if (!EntityUtil.isProtected(en)) unloadedEntityList.add(en);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void loadEntities(Collection<Entity> entityCollection) {
        for (Entity entity4 : entityCollection) {
            if (!MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity4, (World) (Object) this))) {
                if (EntityUtil.isDEAD(entity4)) continue;
                loadedEntityList.add(entity4);
                this.onEntityAdded(entity4);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
        if (EntityUtil.isDEAD(entityIn) || ((iEntity) entityIn).isTimeStop() || (TimeStopUtil.isTimeStop() && !EntityUtil.isProtected(entityIn))) {
            if (Sqlite.DEBUG()) System.out.println("MikuInfo:Ignoring entity:" + entityIn.getClass());
            return;
        }
        if (!(entityIn instanceof EntityPlayer)) {
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
                        System.out.println("MikuWarn:Catch exception at entityIn.onUpdate();");
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
