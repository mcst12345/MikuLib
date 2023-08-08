package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
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
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            return false;
        }
        return MikuLib.MikuEventBus().post(new LivingEvent.LivingUpdateEvent(entity));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onLivingSetAttackTarget(EntityLivingBase entity, EntityLivingBase target)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        if (EntityUtil.isProtected(target)) {
            target.isDead = false;
            target.deathTime = 0;
            Update(target);
            return;
        }
        MikuLib.MikuEventBus().post(new LivingSetAttackTargetEvent(entity, target));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return entity instanceof EntityPlayer || !MikuLib.MikuEventBus().post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return !MikuLib.MikuEventBus().post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingHurt(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return Float.MAX_VALUE;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return 0.0f;
        }
        LivingHurtEvent event = new LivingHurtEvent(entity, src, amount);
        return (MikuLib.MikuEventBus().post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingDamage(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return Float.MAX_VALUE;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return 0.0f;
        }
        LivingDamageEvent event = new LivingDamageEvent(entity, src, amount);
        return (MikuLib.MikuEventBus().post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingDeath(EntityLivingBase entity, DamageSource src)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return MikuLib.MikuEventBus().post(new LivingDeathEvent(entity, src));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(target)) {
            target.isDead = false;
            if (target instanceof EntityLivingBase) ((EntityLivingBase) target).deathTime = 0;
            Update(target);
            return true;
        }
        if (MikuLib.MikuEventBus().post(new AttackEntityEvent(player, target))) return false;
        ItemStack stack = player.getHeldItemMainhand();
        return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
    }

    private static void Update(Entity e){
        MinecraftServer minecraftserver = e.getServer();
        if (minecraftserver != null) {
            WorldServer worldserver = minecraftserver.getWorld(e.dimension);
            worldserver.resetUpdateEntityTick();
            worldserver.updateEntityWithOptionalForce(e, false);
            e.world.profiler.endStartSection("reloading");
        }

    }
}
