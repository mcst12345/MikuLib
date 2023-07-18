package miku.lib.api;

import miku.lib.effect.MikuEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;

public interface iWorld {
    void remove(Entity entity);
    void AddEffect(MikuEffect effect);
    List<MikuEffect> GetEntityEffects(EntityLivingBase entity);
    boolean HasEffect(EntityLivingBase entity);
}
