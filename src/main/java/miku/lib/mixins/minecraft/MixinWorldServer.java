package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.util.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Mixin(value = WorldServer.class)
public abstract class MixinWorldServer extends World implements IThreadListener {
    @Shadow
    protected abstract boolean canAddEntity(Entity entityIn);

    @Shadow
    private int updateEntityTick;

    @Shadow
    public abstract void resetUpdateEntityTick();

    @Shadow
    @Final
    private Set<NextTickListEntry> pendingTickListEntriesHashSet;

    @Shadow
    @Final
    private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;

    @Shadow
    @Final
    private List<NextTickListEntry> pendingTickListEntriesThisTick;

    @Shadow
    protected abstract void playerCheckLight();

    @Shadow
    @Final
    private PlayerChunkMap playerChunkMap;

    @Shadow
    protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);

    @Shadow
    protected abstract void saveLevel() throws MinecraftException;

    @Shadow
    public abstract ChunkProviderServer getChunkProvider();

    @Shadow
    public abstract boolean areAllPlayersAsleep();

    @Shadow
    protected abstract void wakeAllPlayers();

    @Shadow
    @Final
    private WorldEntitySpawner entitySpawner;

    @Shadow
    @Final
    protected VillageSiege villageSiege;

    @Shadow
    @Final
    private Teleporter worldTeleporter;

    @Shadow(remap = false)
    public List<Teleporter> customTeleporters;

    @Shadow
    protected abstract void sendQueuedBlockEvents();

    protected MixinWorldServer(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void tickPlayers() {
        super.tickPlayers();
        this.profiler.endStartSection("players");

        for (Entity entity : this.playerEntities) {
            if (SpecialItem.isTimeStop() && !EntityUtil.isProtected(entity)) continue;
            Entity entity1 = entity.getRidingEntity();

            if (entity1 != null) {
                if (!entity1.isDead && entity1.isPassenger(entity)) {
                    continue;
                }

                entity.dismountRidingEntity();
            }

            this.profiler.startSection("tick");

            if (!entity.isDead || EntityUtil.isProtected(entity)) {
                try {
                    this.updateEntity(entity);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
                    entity.addEntityCrashInfo(crashreportcategory);
                    throw new ReportedException(crashreport);
                }
            }

            this.profiler.endSection();
            this.profiler.startSection("remove");

            if (entity.isDead && !EntityUtil.isProtected(entity)) {
                int j = entity.chunkCoordX;
                int k = entity.chunkCoordZ;

                if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
                    this.getChunk(j, k).removeEntity(entity);
                }

                this.loadedEntityList.remove(entity);
                this.onEntityRemoved(entity);
            }

            this.profiler.endSection();
        }
    }

    @Inject(at=@At("HEAD"),method = "spawnEntity", cancellable = true)
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isDEAD(entityIn))cir.setReturnValue(false);
    }

    @Inject(at=@At("HEAD"),method = "canAddEntity", cancellable = true)
    private void canAddEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isDEAD(entityIn))cir.setReturnValue(false);
        if(EntityUtil.isProtected(entityIn))cir.setReturnValue(true);
    }

    @Inject(at=@At("HEAD"),method = "onEntityAdded", cancellable = true)
    public void onEntityAdded(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isDEAD(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "onEntityRemoved", cancellable = true)
    public void onEntityRemoved(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "setEntityState", cancellable = true)
    public void setEntityState(Entity entityIn, byte state, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn)){
            if(state == (byte) 3 || state == (byte) 30 || state == (byte) 29 || state == (byte) 37 || state == (byte) 33 || state == (byte) 36 || state == (byte) 20 || state == (byte) 2 || state == (byte) 35)ci.cancel();
        }
    }
    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void loadEntities(@Nonnull Collection<Entity> entityCollection)
    {
        List<Entity> fucked = new ArrayList<>();
        for(Entity e : entityCollection){
            if(!EntityUtil.isDEAD(e))fucked.add(e);
        }
        for (Entity entity : fucked) {
            if (MikuInsaneMode.isMikuInsaneMode()) {
                if (this.canAddEntity(entity)) {
                    this.loadedEntityList.add(entity);
                    this.onEntityAdded(entity);
                }
            } else {
                if (this.canAddEntity(entity) && !MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, this))) {
                    this.loadedEntityList.add(entity);
                    this.onEntityAdded(entity);
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void updateEntities()
    {
        if (this.playerEntities.isEmpty() && getPersistentChunks().isEmpty())
        {
            if (this.updateEntityTick++ >= 300)
            {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }

        this.provider.onWorldUpdateEntities();
        super.updateEntities();
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void updateBlockTick(@Nonnull BlockPos pos, @Nonnull Block blockIn, int delay, int priority) {
        if (MikuInsaneMode.isMikuInsaneMode() || SpecialItem.isTimeStop()) return;
        Material material = blockIn.getDefaultState().getMaterial();

        if (this.scheduledUpdatesAreImmediate && material != Material.AIR) {
            if (blockIn.requiresUpdates()) {
                //Keeping here as a note for future when it may be restored.
                boolean isForced = getPersistentChunks().containsKey(new ChunkPos(pos));
                int range = isForced ? 0 : 8;
                if (this.isAreaLoaded(pos.add(-range, -range, -range), pos.add(range, range, range))) {
                    IBlockState iblockstate = this.getBlockState(pos);

                    if (iblockstate.getMaterial() != Material.AIR && iblockstate.getBlock() == blockIn) {
                        iblockstate.getBlock().updateTick(this, pos, iblockstate, this.rand);
                    }
                }

                return;
            }

            delay = 1;
        }

        NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);

        if (this.isBlockLoaded(pos)) {
            if (material != Material.AIR) {
                nextticklistentry.setScheduledTime((long) delay + this.worldInfo.getWorldTotalTime());
                nextticklistentry.setPriority(priority);
            }

            if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
                this.pendingTickListEntriesHashSet.add(nextticklistentry);
                this.pendingTickListEntriesTreeSet.add(nextticklistentry);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean tickUpdates(boolean runAllPending) {
        if (MikuInsaneMode.isMikuInsaneMode() || SpecialItem.isTimeStop()) return true;
        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            int i = this.pendingTickListEntriesTreeSet.size();

            if (i != this.pendingTickListEntriesHashSet.size()) {
                throw new IllegalStateException("TickNextTick list out of synch");
            } else {
                if (i > 65536) {
                    i = 65536;
                }

                this.profiler.startSection("cleaning");

                for (int j = 0; j < i; ++j) {
                    NextTickListEntry nextticklistentry = this.pendingTickListEntriesTreeSet.first();

                    if (!runAllPending && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                        break;
                    }

                    this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
                    this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                    this.pendingTickListEntriesThisTick.add(nextticklistentry);
                }

                this.profiler.endSection();
                this.profiler.startSection("ticking");
                Iterator<NextTickListEntry> iterator = this.pendingTickListEntriesThisTick.iterator();

                while (iterator.hasNext()) {
                    NextTickListEntry nextticklistentry1 = iterator.next();
                    iterator.remove();
                    //Keeping here as a note for future when it may be restored.
                    //boolean isForced = getPersistentChunks().containsKey(new ChunkPos(nextticklistentry.xCoord >> 4, nextticklistentry.zCoord >> 4));
                    //byte b0 = isForced ? 0 : 8;

                    if (this.isAreaLoaded(nextticklistentry1.position.add(0, 0, 0), nextticklistentry1.position.add(0, 0, 0))) {
                        IBlockState iblockstate = this.getBlockState(nextticklistentry1.position);

                        if (iblockstate.getMaterial() != Material.AIR && Block.isEqualTo(iblockstate.getBlock(), nextticklistentry1.getBlock())) {
                            try {
                                iblockstate.getBlock().updateTick(this, nextticklistentry1.position, iblockstate, this.rand);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while ticking a block");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                                CrashReportCategory.addBlockInfo(crashreportcategory, nextticklistentry1.position, iblockstate);
                                throw new ReportedException(crashreport);
                            }
                        }
                    } else {
                        this.scheduleUpdate(nextticklistentry1.position, nextticklistentry1.getBlock(), 0);
                    }
                }

                this.profiler.endSection();
                this.pendingTickListEntriesThisTick.clear();
                return !this.pendingTickListEntriesTreeSet.isEmpty();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void updateBlocks() {
        if (MikuInsaneMode.isMikuInsaneMode() || SpecialItem.isTimeStop()) return;
        this.playerCheckLight();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            Iterator<Chunk> iterator1 = this.playerChunkMap.getChunkIterator();

            while (iterator1.hasNext()) {
                iterator1.next().onTick(false);
            }
        } else {
            int i = this.getGameRules().getInt("randomTickSpeed");
            boolean flag = this.isRaining();
            boolean flag1 = this.isThundering();
            this.profiler.startSection("pollingChunks");

            for (Iterator<Chunk> iterator = getPersistentChunkIterable(this.playerChunkMap.getChunkIterator()); iterator.hasNext(); this.profiler.endSection()) {
                this.profiler.startSection("getChunk");
                Chunk chunk = iterator.next();
                int j = chunk.x * 16;
                int k = chunk.z * 16;
                this.profiler.endStartSection("checkNextLight");
                chunk.enqueueRelightChecks();
                this.profiler.endStartSection("tickChunk");
                chunk.onTick(false);
                this.profiler.endStartSection("thunder");

                if (this.provider.canDoLightning(chunk) && flag && flag1 && this.rand.nextInt(100000) == 0) {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int l = this.updateLCG >> 2;
                    BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));

                    if (this.isRainingAt(blockpos)) {
                        DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);

                        if (this.getGameRules().getBoolean("doMobSpawning") && this.rand.nextDouble() < (double) difficultyinstance.getAdditionalDifficulty() * 0.01D) {
                            EntitySkeletonHorse entityskeletonhorse = new EntitySkeletonHorse(this);
                            entityskeletonhorse.setTrap(true);
                            entityskeletonhorse.setGrowingAge(0);
                            entityskeletonhorse.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                            this.spawnEntity(entityskeletonhorse);
                            this.addWeatherEffect(new EntityLightningBolt(this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), true));
                        } else {
                            this.addWeatherEffect(new EntityLightningBolt(this, blockpos.getX(), blockpos.getY(), blockpos.getZ(), false));
                        }
                    }
                }

                this.profiler.endStartSection("iceandsnow");

                if (this.provider.canDoRainSnowIce(chunk) && this.rand.nextInt(16) == 0) {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int j2 = this.updateLCG >> 2;
                    BlockPos blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
                    BlockPos blockpos2 = blockpos1.down();

                    if (this.isAreaLoaded(blockpos2, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
                        if (this.canBlockFreezeNoWater(blockpos2)) {
                            this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
                        }

                    if (flag && this.canSnowAt(blockpos1, true)) {
                        this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
                    }

                    if (flag && this.getBiome(blockpos2).canRain()) {
                        this.getBlockState(blockpos2).getBlock().fillWithRain(this, blockpos2);
                    }
                }

                this.profiler.endStartSection("tickBlocks");

                if (i > 0) {
                    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()) {
                        if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.needsRandomTick()) {
                            for (int i1 = 0; i1 < i; ++i1) {
                                this.updateLCG = this.updateLCG * 3 + 1013904223;
                                int j1 = this.updateLCG >> 2;
                                int k1 = j1 & 15;
                                int l1 = j1 >> 8 & 15;
                                int i2 = j1 >> 16 & 15;
                                IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                                Block block = iblockstate.getBlock();
                                this.profiler.startSection("randomTick");

                                if (block.getTickRandomly()) {
                                    block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);
                                }

                                this.profiler.endSection();
                            }
                        }
                    }
                }
            }

            this.profiler.endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!
     */
    @Overwrite
    public void saveAllChunks(boolean all, @Nullable IProgressUpdate progressCallback) throws MinecraftException {
        ChunkProviderServer chunkproviderserver = this.getChunkProvider();

        if (chunkproviderserver.canSave()) {
            if (progressCallback != null) {
                progressCallback.displaySavingString("Saving level");
            }

            this.saveLevel();

            if (progressCallback != null) {
                progressCallback.displayLoadingString("Saving chunks");
            }

            chunkproviderserver.saveChunks(all);
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.world.WorldEvent.Save(this));

            for (Chunk chunk : Lists.newArrayList(chunkproviderserver.getLoadedChunks())) {
                if (chunk != null && !this.playerChunkMap.contains(chunk.x, chunk.z)) {
                    chunkproviderserver.queueUnload(chunk);
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void tick() {
        super.tick();

        boolean stop = SpecialItem.isTimeStop();

        if (!stop) {
            try {
                if (this.getWorldInfo().isHardcoreModeEnabled() && this.getDifficulty() != EnumDifficulty.HARD) {
                    this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        this.provider.getBiomeProvider().cleanupCache();

        if (!stop) {
            try {
                if (this.areAllPlayersAsleep()) {
                    if (this.getGameRules().getBoolean("doDaylightCycle")) {
                        long i = this.getWorldTime() + 24000L;
                        this.setWorldTime(i - i % 24000L);
                    }

                    this.wakeAllPlayers();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        this.profiler.startSection("mobSpawner");

        try {
            if (!stop)
                if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    this.entitySpawner.findChunksForSpawning((WorldServer) (Object) this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
                }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        this.profiler.endStartSection("chunkSource");
        this.chunkProvider.tick();
        int j = this.calculateSkylightSubtracted(1.0F);

        if (!stop) {
            if (j != this.getSkylightSubtracted()) {
                this.setSkylightSubtracted(j);
            }

            this.worldInfo.setWorldTotalTime(this.worldInfo.getWorldTotalTime() + 1L);

            if (this.getGameRules().getBoolean("doDaylightCycle")) {
                this.setWorldTime(this.getWorldTime() + 1L);
            }
        }

        this.profiler.endStartSection("tickPending");
        this.tickUpdates(false);
        this.profiler.endStartSection("tickBlocks");
        this.updateBlocks();
        this.profiler.endStartSection("chunkMap");
        this.playerChunkMap.tick();
        if (!stop) {
            this.profiler.endStartSection("village");
            this.villageCollection.tick();
            this.villageSiege.tick();
        }
        if (!stop) {
            this.profiler.endStartSection("portalForcer");
            this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());
            for (Teleporter tele : customTeleporters) {
                tele.removeStalePortalLocations(getTotalWorldTime());
            }
        }
        this.profiler.endSection();
        this.sendQueuedBlockEvents();
    }
}
