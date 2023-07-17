package miku.lib.api;

import miku.lib.effect.MikuEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;
import java.util.UUID;

public interface iWorld {
    void remove(Entity entity);
    void AddEffect(MikuEffect effect);
    List<MikuEffect> GetEntityEffects(EntityLivingBase entity);

    boolean HasEffect(EntityLivingBase entity);
}
