package miku.lib.mixins.minecraft;

import miku.lib.api.iEntity;
import miku.lib.api.iEntityLivingBase;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;

@Mixin(value = EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements iEntityLivingBase {
    @Override
    public int idleTime(){
        return idleTime;
    }

    @Override
    public void SetIdleTime(int value){
        idleTime = value;
    }

    @Override
    public int recentlyHit(){
        return recentlyHit;
    }

    @Override
    public void SetRecentlyHit(int value){
        recentlyHit = value;
    }

    @Override
    public int revengeTimer(){
        return revengeTimer;
    }

    @Override
    public void SetRevengeTimer(int value){
        revengeTimer = value;
    }

    @Override
    public float landMovementFactor(){
        return landMovementFactor;
    }

    @Override
    public void SetLandMovementFactor(int value){
        landMovementFactor = value;
    }

    @Shadow protected int recentlyHit;

    @Shadow @Final private static DataParameter<Float> HEALTH;

    @Shadow protected boolean dead;

    @Shadow public int deathTime;

    @Shadow @Final private CombatTracker combatTracker;

    @Shadow protected abstract void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source);

    @Shadow protected int idleTime;

    @Shadow protected abstract void damageShield(float damage);

    @Shadow protected float lastDamage;

    @Shadow private EntityLivingBase revengeTarget;

    @Shadow private int revengeTimer;

    @Shadow protected EntityPlayer attackingPlayer;

    @Override
    public EntityPlayer attackingPlayer(){
        return attackingPlayer;
    }

    @Override
    public void SetAttackingPlayer(EntityPlayer player){
        attackingPlayer = player;
    }

    @Shadow private float absorptionAmount;

    @Override
    public float absorptionAmount(){
        return absorptionAmount;
    }

    @Override
    public void SetAbsorptionAmount(float value){
        absorptionAmount = value;
    }

    @Shadow private AbstractAttributeMap attributeMap;

    @Shadow @Final private Map<Potion, PotionEffect> activePotionsMap;

    @Override
    public Map<Potion, PotionEffect> GetPotion(){
        return activePotionsMap;
    }

    @Shadow private boolean potionsNeedUpdate;

    @Shadow private float landMovementFactor;

    @Shadow private EntityLivingBase lastAttackedEntity;

    @Shadow private int lastAttackedEntityTime;

    @Shadow public int swingProgressInt;

    @Shadow public float swingProgress;

    @Shadow public float rotationYawHead;

    @Shadow public float cameraPitch;

    @Shadow public float renderYawOffset;

    @Shadow public float moveStrafing;

    @Shadow public float moveVertical;

    @Shadow public float moveForward;

    @Shadow public float prevRotationYawHead;

    @Shadow public float prevCameraPitch;

    @Shadow public float prevRenderYawOffset;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(at=@At("HEAD"),method = "addPotionEffect", cancellable = true)
    public void addPotionEffect(PotionEffect potioneffectIn, CallbackInfo ci){
        if(EntityUtil.isProtected(this))ci.cancel();
    }

    @Override
    public void Kill(){
        ((EntityLivingBase) (Object) this).limbSwingAmount = 1.5F;
        this.idleTime = 0;
        this.damageShield(Float.MAX_VALUE);
        this.lastDamage=Float.MAX_VALUE;
        this.recentlyHit=60;
        this.revengeTarget=null;
        this.revengeTimer=0;
        this.landMovementFactor = 0.0F;
        Iterator<PotionEffect> iterator = this.activePotionsMap.values().iterator();
        while (iterator.hasNext()) {
            PotionEffect effect = iterator.next();
            this.potionsNeedUpdate = true;
            effect.getPotion().applyAttributesModifiersToEntity(((EntityLivingBase) (Object) this), ((EntityLivingBase) (Object) this).getAttributeMap(), effect.getAmplifier());
            iterator.remove();
        }
        this.dataManager.set(HEALTH, 0.0f);
        if (this.attributeMap == null) {
            this.attributeMap = new AttributeMap();
        }
        IAttributeInstance Attribute = this.attributeMap.getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        Attribute.setBaseValue(0.0D);
        Attribute = attributeMap.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        Attribute.setBaseValue(0.0D);
        this.dead=true;
        this.deathTime= Integer.MAX_VALUE;
        this.attackingPlayer=null;
        this.lastAttackedEntity=null;
        this.lastAttackedEntityTime=0;
        this.velocityChanged=true;
        this.absorptionAmount=0;
        combatTracker.reset();
        if (!this.world.isRemote) {
            int i = Integer.MAX_VALUE;
            captureDrops = true;
            capturedDrops.clear();
            this.dropLoot(true, i, DamageSource.OUT_OF_WORLD);
            captureDrops = false;
            for (EntityItem item : capturedDrops) {
                world.spawnEntity(item);
            }
        }
        world.setEntityState(this, (byte) 3);
    }

    @Inject(at = @At("HEAD"), method = "setHealth", cancellable = true)
    public void setHealth(float health, CallbackInfo ci){
        if(EntityUtil.isProtected(this)){
            this.dataManager.set(HEALTH, 20.0f);
            ci.cancel();
        }
        if(EntityUtil.isDEAD(this)){
            this.dataManager.set(HEALTH, 0.0f);
            ci.cancel();
        }
    }

    @Override
    public void SetHealth(float value){
        this.dataManager.set(HEALTH,value);
    }

    @Inject(at = @At("HEAD"), method = "damageEntity", cancellable = true)
    protected void damageEntity(DamageSource damageSrc, float damageAmount, CallbackInfo ci){
        if(EntityUtil.isProtected(this))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "isMovementBlocked", cancellable = true)
    protected void isMovementBlocked(CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isProtected(this))cir.setReturnValue(false);
        if(EntityUtil.isDEAD(this))cir.setReturnValue(true);
    }

    @Override
    public void AddPotion(PotionEffect potioneffect) {
        PotionEffect potionEffect = this.activePotionsMap.get(potioneffect.getPotion());
        if (potionEffect == null) {
            this.activePotionsMap.put(potioneffect.getPotion(), potioneffect);
            this.potionsNeedUpdate = true;
            if (!this.world.isRemote) {
                potioneffect.getPotion().applyAttributesModifiersToEntity(((EntityLivingBase) (Object) this), ((EntityLivingBase) (Object) this).getAttributeMap(), potioneffect.getAmplifier());
            }
        } else {
            potionEffect.combine(potioneffect);
            this.potionsNeedUpdate = true;
            if (!this.world.isRemote) {
                Potion potion = potionEffect.getPotion();
                potion.removeAttributesModifiersFromEntity(((EntityLivingBase) (Object) this), ((EntityLivingBase) (Object) this).getAttributeMap(), potionEffect.getAmplifier());
                potion.applyAttributesModifiersToEntity(((EntityLivingBase) (Object) this), ((EntityLivingBase) (Object) this).getAttributeMap(), potionEffect.getAmplifier());
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getHealth", cancellable = true)
    public final void getHealth(CallbackInfoReturnable<Float> cir) {
        if(EntityUtil.isProtected(this))cir.setReturnValue(20.0f);
        if(EntityUtil.isDEAD(this))cir.setReturnValue(0.0f);
    }

    @Inject(at = @At("HEAD"), method = "getMaxHealth", cancellable = true)
    public final void GetMaxHealth(CallbackInfoReturnable<Float> cir) {
        if(EntityUtil.isProtected(this))cir.setReturnValue(20.0f);
        if(EntityUtil.isDEAD(this))cir.setReturnValue(0.0f);
    }

    @Inject(at = @At("HEAD"),method = "onUpdate",cancellable = true)
    public void onUpdate(CallbackInfo ci){
        if(((iEntity)this).isTimeStop()){
            ((iEntity)this).TimeStop();
            this.swingProgressInt=0;
            this.swingProgress=0.0f;
            ci.cancel();
        }
    }

    @Override
    public void TimeStop(){
        rotationYawHead=prevRotationYawHead;
        cameraPitch=prevCameraPitch;
        renderYawOffset=prevRenderYawOffset;
        moveStrafing=0.0f;
        moveVertical=0.0f;
        moveForward=0.0f;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void onItemPickup(Entity entityIn, int quantity)
    {
        if ((!entityIn.isDead && !EntityUtil.isProtected(entityIn)) && !this.world.isRemote)
        {
            EntityTracker entitytracker = ((WorldServer)this.world).getEntityTracker();

            if (entityIn instanceof EntityItem || entityIn instanceof EntityArrow || entityIn instanceof EntityXPOrb)
            {
                entitytracker.sendToTracking(entityIn, new SPacketCollectItem(entityIn.getEntityId(), this.getEntityId(), quantity));
            }
        }
    }
}
