package miku.lib.common.api;

import miku.lib.common.effect.MikuEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;

public interface iWorld {
    void remove(Entity entity);
    void AddEffect(MikuEffect effect);
    List<MikuEffect> GetEntityEffects(EntityLivingBase entity);
    boolean HasEffect(EntityLivingBase entity);

    void summonEntity(Entity entity);

    List<Entity> getProtectedEntities();
}
