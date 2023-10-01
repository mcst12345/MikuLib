package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.client.api.iWorldClient;
import miku.lib.common.api.iMapStorage;
import miku.lib.common.api.iWorld;
import miku.lib.common.core.MikuLib;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.Set;

@Mixin(value = WorldClient.class)
public abstract class MixinWorldClient extends World implements iWorldClient {
    @Override
    public void reload() {
        this.chunkProvider = this.createChunkProvider();
        visibleChunks.clear();
        if (mapStorage != null) {
            ((iMapStorage) mapStorage).clearData();
        }
        if (perWorldStorage != null) {
            ((iMapStorage) perWorldStorage).clearData();
        }
    }

    @Shadow
    @Final
    private Set<Entity> entityList;

    @Shadow
    @Final
    private Set<Entity> entitySpawnQueue;

    @Shadow
    private ChunkProviderClient clientChunkProvider;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected Set<ChunkPos> visibleChunks;

    @NotNull
    @Shadow
    protected abstract IChunkProvider createChunkProvider();

    protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    public void REMOVE(Entity entity) {
        entityList.remove(entity);
        entitySpawnQueue.remove(entity);
    }

    /**
     * @author mcst12345
     * @reason F**k
     */
    @Overwrite
    public Entity removeEntityFromWorld(int entityID)
    {
        Entity entity = this.entitiesById.removeObject(entityID);

        if (entity != null && !EntityUtil.isProtected(entity)) {
            this.entityList.remove(entity);
            this.removeEntity(entity);
        }

        return entity;
    }

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    public void doVoidFogParticles(int posX, int posY, int posZ) {
        if (TimeStopUtil.isTimeStop() || ((iWorld) this).isTimeStop()) return;
        Random random = new Random();
        ItemStack itemstack = this.mc.player.getHeldItemMainhand();
        boolean flag = this.mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j) {
            this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos) {
        if (TimeStopUtil.isTimeStop() || ((iWorld) this).isTimeStop()) return;
        int i = x + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        int j = y + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        int k = z + this.rand.nextInt(offset) - this.rand.nextInt(offset);
        pos.setPos(i, j, k);
        IBlockState iblockstate = this.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, this, pos, random);

        if (holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER) {
            this.spawnParticle(EnumParticleTypes.BARRIER, (float) i + 0.5F, (float) j + 0.5F, (float) k + 0.5F, 0.0D, 0.0D, 0.0D);
        }
    }

    /**
     * @author mcst12345
     * @reason F K
     */
    @Overwrite
    public void tick() {
        super.tick();

        if (TimeStopUtil.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop()) return;

        this.setTotalWorldTime(this.getTotalWorldTime() + 1L);

        if (this.getGameRules().getBoolean("doDaylightCycle"))
        {
            this.setWorldTime(this.getWorldTime() + 1L);
        }

        this.profiler.startSection("reEntryProcessing");

        for (int i = 0; i < 10 && !this.entitySpawnQueue.isEmpty(); ++i)
        {
            Entity entity = this.entitySpawnQueue.iterator().next();
            this.entitySpawnQueue.remove(entity);

            if (!this.loadedEntityList.contains(entity))
            {
                this.spawnEntity(entity);
            }
        }

        this.profiler.endStartSection("chunkCache");
        this.clientChunkProvider.tick();
        this.profiler.endStartSection("blocks");
        this.updateBlocks();
        this.profiler.endSection();
    }

    @Inject(at=@At("HEAD"),method = "spawnEntity", cancellable = true)
    public void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isDEAD(entityIn))cir.setReturnValue(false);
    }

    @Inject(at=@At("HEAD"),method = "removeEntity", cancellable = true)
    public void removeEntity(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onEntityAdded", cancellable = true)
    public void onEntityAdded(Entity entityIn, CallbackInfo ci) {
        if (EntityUtil.isDEAD(entityIn)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onEntityRemoved", cancellable = true)
    public void onEntityRemoved(Entity entityIn, CallbackInfo ci) {
        if (EntityUtil.isProtected(entityIn)) ci.cancel();
    }


    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void addEntityToWorld(int entityID, Entity entityToSpawn) {
        if (EntityUtil.isDEAD(entityToSpawn)) {
            if (Sqlite.DEBUG()) System.out.println("MikuInfo:Ignoring entity " + entityToSpawn.getClass());
            return;
        }

        Entity entity = this.getEntityByID(entityID);

        if (Sqlite.DEBUG()) System.out.println("MikuInfo:Adding entity " + entityToSpawn.getClass() + " to world.");

        if (entity != null) {
            this.removeEntity(entity);
        }

        this.entityList.add(entityToSpawn);
        entityToSpawn.setEntityId(entityID);

        if (!this.spawnEntity(entityToSpawn)) {
            this.entitySpawnQueue.add(entityToSpawn);
        }

        this.entitiesById.addKey(entityID, entityToSpawn);
    }

    @Inject(at = @At("TAIL"), method = "getEntityByID", cancellable = true)
    public void getEntityByID(int id, CallbackInfoReturnable<Entity> cir) {
        if (EntityUtil.isDEAD(cir.getReturnValue())) cir.setReturnValue(null);
    }

    @Inject(at = @At("HEAD"), method = "removeEntityFromWorld", cancellable = true)
    public void removeEntityFromWorld(int entityID, CallbackInfoReturnable<Entity> cir) {
        if (EntityUtil.isProtected(entitiesById.lookup(entityID))) cir.setReturnValue(null);
    }

    static {
        MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
    }
}
