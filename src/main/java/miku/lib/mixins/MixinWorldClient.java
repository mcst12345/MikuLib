package miku.lib.mixins;

import miku.lib.util.EntityUtil;
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

import java.util.Set;

@Mixin(value = WorldClient.class)
public abstract class MixinWorldClient extends World {
    @Shadow @Final private Set<Entity> entityList;

    protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
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
}
