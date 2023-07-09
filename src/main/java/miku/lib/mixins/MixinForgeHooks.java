package miku.lib.mixins;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ForgeHooks.class,remap = false)
public class MixinForgeHooks {
    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingUpdate(EntityLivingBase entity)
    {
        if(EntityUtil.isProtected(entity))entity.isDead=false;
        return MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingUpdateEvent(entity));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onLivingSetAttackTarget(EntityLivingBase entity, EntityLivingBase target)
    {
        if(EntityUtil.isProtected(target)){
            EntityUtil.Kill(entity);
            target.isDead=false;
            return;
        }
        if(EntityUtil.isProtected(entity)) {
            EntityUtil.Kill(target);
            return;
        }
        MinecraftForge.EVENT_BUS.post(new LivingSetAttackTargetEvent(entity, target));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if(EntityUtil.isProtected(entity)){
            entity.isDead=false;
            EntityUtil.Kill(src.getTrueSource());
            return true;
        }
        if(EntityUtil.isProtected(src.getTrueSource())) {
            EntityUtil.Kill(entity);
            return true;
        }
        return entity instanceof EntityPlayer || !MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if(EntityUtil.isProtected(entity)){
            entity.isDead=false;
            EntityUtil.Kill(src.getTrueSource());
            return true;
        }
        if(EntityUtil.isProtected(src.getTrueSource())) {
            EntityUtil.Kill(entity);
            return true;
        }
        return !MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingHurt(EntityLivingBase entity, DamageSource src, float amount)
    {
        if(EntityUtil.isProtected(entity)){
            entity.isDead=false;
            EntityUtil.Kill(src.getTrueSource());
            return 0.0f;
        }
        if(EntityUtil.isProtected(src.getTrueSource())) {
            EntityUtil.Kill(entity);
            return 0.0f;
        }
        LivingHurtEvent event = new LivingHurtEvent(entity, src, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingDamage(EntityLivingBase entity, DamageSource src, float amount)
    {
        if(EntityUtil.isProtected(entity)){
            entity.isDead=false;
            EntityUtil.Kill(src.getTrueSource());
            return 0.0f;
        }
        if(EntityUtil.isProtected(src.getTrueSource())) {
            EntityUtil.Kill(entity);
            return 0.0f;
        }
        LivingDamageEvent event = new LivingDamageEvent(entity, src, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingDeath(EntityLivingBase entity, DamageSource src)
    {
        if(EntityUtil.isProtected(entity)){
            entity.isDead=false;
            EntityUtil.Kill(src.getTrueSource());
            return true;
        }
        if(EntityUtil.isProtected(src.getTrueSource())) {
            EntityUtil.Kill(entity);
            return true;
        }
        return MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target)
    {
        if(EntityUtil.isProtected(target)){
            EntityUtil.Kill(player);
            target.isDead=false;
            return true;
        }
        if(EntityUtil.isProtected(player)) {
            EntityUtil.Kill(target);
            return true;
        }
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, target))) return false;
        ItemStack stack = player.getHeldItemMainhand();
        return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
    }
}
