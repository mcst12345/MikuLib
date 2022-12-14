package miku.lib.mixins;

import miku.lib.item.SpecialItem;
import miku.lib.util.EntityUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldServer.class)
public abstract class MixinWorldServer extends World implements IThreadListener {
    protected MixinWorldServer(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    protected void tickPlayers()
    {
        super.tickPlayers();
        this.profiler.endStartSection("players");

        for (Entity entity : this.playerEntities) {
            if(SpecialItem.isTimeStop() && !EntityUtil.isProtected(entity))continue;
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
}
