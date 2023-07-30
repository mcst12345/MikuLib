package miku.lib.mixins.minecraft;

import miku.lib.common.api.iEntity;
import miku.lib.common.api.iEntityLivingBase;
import miku.lib.common.api.iWorld;
import miku.lib.common.effect.MikuEffect;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements iEntityLivingBase {
    @Inject(at=@At("TAIL"),method = "readEntityFromNBT")
    public void readEntityFromNBT(NBTTagCompound compound, CallbackInfo ci){
        if(compound.hasKey("MikuEffects",9)){
            NBTTagList MikuEffects = compound.getTagList("MikuEffects", 10);
            for (int i = 0; i < MikuEffects.tagCount(); ++i){
                NBTTagCompound MikuEffect = MikuEffects.getCompoundTagAt(i);
                try {
                    Class<? extends MikuEffect> EffectClass = (Class<? extends miku.lib.common.effect.MikuEffect>) Class.forName(MikuEffect.getString("class"));
                    Constructor<? extends miku.lib.common.effect.MikuEffect> constructor = EffectClass.getConstructor(EntityLivingBase.class, int.class, int.class, int.class);
                    miku.lib.common.effect.MikuEffect effect = constructor.newInstance(this, MikuEffect.getInteger("wait"), MikuEffect.getInteger("duration"), MikuEffect.getInteger("level"));

                    effect.FromNBT(MikuEffect);

                    ((iWorld) world).AddEffect(effect);
                } catch (ClassNotFoundException e) {
                    System.out.println("WARN:Effect class "+MikuEffect.getString("class")+" not found.Skip it.");
                } catch (ClassCastException e){
                    System.out.println("WARN:Class "+MikuEffect.getString("class")+" is not a MikuEffect class.");
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Inject(at=@At("TAIL"),method = "writeEntityToNBT")
    public void writeEntityToNBT(NBTTagCompound compound, CallbackInfo ci){
        NBTTagList MikuEffects = new NBTTagList();
        if(!((iWorld)world).GetEntityEffects((EntityLivingBase)(Object)this).isEmpty())for(MikuEffect effect : ((iWorld)world).GetEntityEffects((EntityLivingBase)(Object)this)){
            MikuEffects.appendTag(effect.toNBT());
        }
        if(!MikuEffects.isEmpty())compound.setTag("MikuEffects",MikuEffects);
    }
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

    @Shadow
    public float moveVertical;

    @Shadow
    public float moveForward;

    @Shadow
    public float prevRotationYawHead;

    @Shadow
    public float prevCameraPitch;

    @Shadow
    public float prevRenderYawOffset;

    @Shadow
    public float limbSwingAmount;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(at = @At("HEAD"), method = "addPotionEffect", cancellable = true)
    public void addPotionEffect(PotionEffect potioneffectIn, CallbackInfo ci) {
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Override
    public void Kill() {
        Field field;
        long tmp;
        try {
            field = EntityLivingBase.class.getDeclaredField("field_70721_aZ");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putFloatVolatile(this, tmp, 1.5F);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70708_bq");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, 0);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_110153_bc");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putFloatVolatile(this, tmp, Float.MAX_VALUE);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70718_bc");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, 60);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70755_b");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObjectVolatile(this, tmp, null);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70756_c");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, 0);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70746_aG");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putFloatVolatile(this, tmp, 0.0f);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70713_bf");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObjectVolatile(this, tmp, new HashMap<>());
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = Entity.class.getDeclaredField("field_70180_af");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            EntityDataManager manager = (EntityDataManager) Launch.UNSAFE.getObjectVolatile(this, tmp);
            manager.set(HEALTH, 0.0f);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_110155_d");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            AbstractAttributeMap AttributeMap = (AbstractAttributeMap) Launch.UNSAFE.getObjectVolatile(this, tmp);
            Field attributes = AbstractAttributeMap.class.getDeclaredField("field_111154_a");
            tmp = Launch.UNSAFE.objectFieldOffset(attributes);
            Map<IAttribute, IAttributeInstance> Attributes = (Map<IAttribute, IAttributeInstance>) Launch.UNSAFE.getObjectVolatile(AttributeMap, tmp);
            IAttributeInstance Attribute = Attributes.get(SharedMonsterAttributes.MAX_HEALTH);
            Attribute.setBaseValue(0.0D);
            Attribute = Attributes.get(SharedMonsterAttributes.MOVEMENT_SPEED);
            Attribute.setBaseValue(0.0D);
            Attribute = Attributes.get(SharedMonsterAttributes.ARMOR);
            Attribute.setBaseValue(0.0D);
            Attribute = Attributes.get(SharedMonsterAttributes.LUCK);
            Attribute.setBaseValue(0.0D);
            Attribute = Attributes.get(SharedMonsterAttributes.ARMOR_TOUGHNESS);
            Attribute.setBaseValue(0.0D);
            Attribute = Attributes.get(SharedMonsterAttributes.ATTACK_DAMAGE);
            Attribute.setBaseValue(0.0D);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //this.limbSwingAmount = 1.5F;
        //this.idleTime = 0;
        //this.lastDamage=Float.MAX_VALUE;
        //this.recentlyHit=60;
        //this.revengeTarget=null;
        //this.revengeTimer=0;
        //this.landMovementFactor = 0.0F;
        //Iterator<PotionEffect> iterator = this.activePotionsMap.values().iterator();
        //while (iterator.hasNext()) {
        //    PotionEffect effect = iterator.next();
        //    this.potionsNeedUpdate = true;
        //    effect.getPotion().applyAttributesModifiersToEntity(((EntityLivingBase) (Object) this), ((EntityLivingBase) (Object) this).getAttributeMap(), effect.getAmplifier());
        //    iterator.remove();
        //}
        //this.dataManager.set(HEALTH, 0.0f);
        //if (this.attributeMap == null) {
        //    this.attributeMap = new AttributeMap();
        //}
        //IAttributeInstance Attribute = this.attributeMap.getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        //Attribute.setBaseValue(0.0D);
        //Attribute = attributeMap.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        //Attribute.setBaseValue(0.0D);

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70729_aU");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putBooleanVolatile(this, tmp, true);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70725_aQ");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, Integer.MAX_VALUE);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_70717_bb");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObjectVolatile(this, tmp, null);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_110150_bn");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObjectVolatile(this, tmp, null);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_142016_bo");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, 0);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //this.dead=true;
        //this.deathTime= Integer.MAX_VALUE;
        //this.attackingPlayer=null;
        //this.lastAttackedEntity=null;
        //this.lastAttackedEntityTime=0;
        //this.velocityChanged=true;
        this.absorptionAmount = 0;
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
