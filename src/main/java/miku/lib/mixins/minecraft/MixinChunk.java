package miku.lib.mixins.minecraft;

import miku.lib.api.iChunk;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Chunk.class)
public abstract class MixinChunk implements iChunk {
    @Final
    @Shadow
    private final ClassInheritanceMultiMap[] entityLists = new ClassInheritanceMultiMap[16];

    @Shadow public abstract void markDirty();


    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void removeEntity(Entity entityIn)
    {
        if(EntityUtil.isProtected(entityIn))return;
        this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void removeEntityAtIndex(Entity entityIn, int index)
    {
        if(EntityUtil.isProtected(entityIn))return;
        if (index < 0)
        {
            index = 0;
        }

        if (index >= this.entityLists.length)
        {
            index = this.entityLists.length - 1;
        }

        this.entityLists[index].remove(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after entity removals
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
