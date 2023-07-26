package miku.lib.mixins.minecraft;

import miku.lib.common.api.iMinecraft;
import miku.lib.common.api.iWorldClient;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
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

import java.util.Set;

@Mixin(value = WorldClient.class)
public abstract class MixinWorldClient extends World implements iWorldClient {
    @Shadow @Final private Set<Entity> entityList;

    @Shadow @Final private Set<Entity> entitySpawnQueue;

    @Shadow private ChunkProviderClient clientChunkProvider;

    protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    public void REMOVE(Entity entity){
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

        if (entity != null && !EntityUtil.isProtected(entity))
        {
            this.entityList.remove(entity);
            this.removeEntity(entity);
        }

        return entity;
    }

    /**
     * @author mcst12345
     * @reason F K
     */
    @Overwrite
    public void tick()
    {
        super.tick();

        if(SpecialItem.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop())return;

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

    @Inject(at=@At("HEAD"),method = "onEntityAdded", cancellable = true)
    public void onEntityAdded(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isDEAD(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "onEntityRemoved", cancellable = true)
    public void onEntityRemoved(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isProtected(entityIn))ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "addEntityToWorld", cancellable = true)
    public void addEntityToWorld(int entityID, Entity entityToSpawn, CallbackInfo ci){
        if(EntityUtil.isDEAD(entityToSpawn))ci.cancel();
    }

    @Inject(at=@At("TAIL"),method = "getEntityByID", cancellable = true)
    public void getEntityByID(int id, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isDEAD(cir.getReturnValue()))cir.setReturnValue(null);
    }

    @Inject(at=@At("HEAD"),method = "removeEntityFromWorld", cancellable = true)
    public void removeEntityFromWorld(int entityID, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isProtected(entitiesById.lookup(entityID)))cir.setReturnValue(null);
    }
}
