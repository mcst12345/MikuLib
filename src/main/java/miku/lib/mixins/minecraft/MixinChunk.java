package miku.lib.mixins.minecraft;

import miku.lib.api.iChunk;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Chunk.class)
public class MixinChunk implements iChunk {
    @Final
    @Shadow
    private final ClassInheritanceMultiMap[] entityLists = new ClassInheritanceMultiMap[16];

    @Inject(at = @At("HEAD"), method = "removeEntity", cancellable = true)
    public void removeEntity(Entity entityIn, CallbackInfo ci) {
        if (((Chunk) (Object) this).isLoaded()) {
            if(EntityUtil.isProtected(entityIn))ci.cancel();
        }
    }

    @Override
    public void remove(Entity entityIn) {
        if ( entityIn.chunkCoordY < 0) {
            entityIn.chunkCoordY = 0;
        }

        if ( entityIn.chunkCoordY >= entityLists.length) {
            entityIn.chunkCoordY = entityLists.length - 1;
        }

        entityLists[ entityIn.chunkCoordY].remove(entityIn);
        ((Chunk) (Object) this).markDirty(); // Forge - ensure chunks are marked to save after entity removals
    }

    @Inject(at=@At("HEAD"),method = "addEntity", cancellable = true)
    public void addEntity(Entity entityIn, CallbackInfo ci){
        if(EntityUtil.isDEAD(entityIn))ci.cancel();
    }
}
