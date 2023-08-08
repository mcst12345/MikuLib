package miku.lib.mixins.minecraft;

import miku.lib.common.api.iEntity;
import miku.lib.common.api.iEntityLivingBase;
import miku.lib.common.api.iWorld;
import miku.lib.common.core.MikuLib;
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
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
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

    @Shadow
    protected abstract void updateActiveHand();

    @Shadow
    public abstract int getArrowCountInEntity();

    @Shadow
    public int arrowHitTimer;

    @Shadow
    public abstract void setArrowCountInEntity(int count);

    @Shadow
    @Final
    private NonNullList<ItemStack> handInventory;

    @Shadow
    @Final
    private NonNullList<ItemStack> armorArray;

    @Shadow
    public abstract ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn);

    @Shadow
    public abstract AbstractAttributeMap getAttributeMap();

    @Shadow
    public abstract CombatTracker getCombatTracker();

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow
    public abstract void onLivingUpdate();

    @Shadow
    protected float prevOnGroundSpeedFactor;

    @Shadow
    protected float onGroundSpeedFactor;

    @Shadow
    protected abstract float updateDistance(float p_110146_1_, float p_110146_2_);

    @Shadow
    protected float movedDistance;

    @Shadow
    public abstract boolean isElytraFlying();

    @Shadow
    protected int ticksElytraFlying;

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

        try {
            field = EntityLivingBase.class.getDeclaredField("field_110151_bq");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putIntVolatile(this, tmp, 0);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            field = EntityLivingBase.class.getDeclaredField("field_94063_bt");
            tmp = Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObjectVolatile(this, tmp, new CombatTracker((EntityLivingBase) (Object) this));
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //this.dead=true;
        //this.deathTime= Integer.MAX_VALUE;
        //this.attackingPlayer=null;
        //this.lastAttackedEntity=null;
        //this.lastAttackedEntityTime=0;
        //this.velocityChanged=true;
        //this.absorptionAmount = 0;
        //combatTracker.reset();
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

    @Inject(at = @At("HEAD"), method = "onUpdate", cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if (((iEntity) this).isTimeStop()) {
            ((iEntity) this).TimeStop();
            this.swingProgressInt = 0;
            this.swingProgress = 0.0f;
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void onUpdate() {
        if (((iEntity) this).isTimeStop()) {
            ((iEntity) this).TimeStop();
            this.swingProgressInt = 0;
            this.swingProgress = 0.0f;
            return;
        }
        boolean Flag;
        try {
            Flag = net.minecraftforge.common.ForgeHooks.onLivingUpdate((EntityLivingBase) (Object) this) && !EntityUtil.isProtected(this);
        } catch (Throwable t) {
            Flag = false;
        }
        if (Flag) return;
        super.onUpdate();
        this.updateActiveHand();

        if (!this.world.isRemote) {
            int i = this.getArrowCountInEntity();

            if (i > 0) {
                if (this.arrowHitTimer <= 0) {
                    this.arrowHitTimer = 20 * (30 - i);
                }

                --this.arrowHitTimer;

                if (this.arrowHitTimer <= 0) {
                    this.setArrowCountInEntity(i - 1);
                }
            }

            for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
                ItemStack itemstack;

                switch (entityequipmentslot.getSlotType()) {
                    case HAND:
                        itemstack = this.handInventory.get(entityequipmentslot.getIndex());
                        break;
                    case ARMOR:
                        itemstack = this.armorArray.get(entityequipmentslot.getIndex());
                        break;
                    default:
                        continue;
                }

                ItemStack itemstack1 = this.getItemStackFromSlot(entityequipmentslot);

                if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                    if (!ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack))
                        ((WorldServer) this.world).getEntityTracker().sendToTracking(this, new SPacketEntityEquipment(this.getEntityId(), entityequipmentslot, itemstack1));
                    MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent((EntityLivingBase) (Object) this, entityequipmentslot, itemstack, itemstack1));

                    if (!itemstack.isEmpty()) {
                        this.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(entityequipmentslot));
                    }

                    if (!itemstack1.isEmpty()) {
                        this.getAttributeMap().applyAttributeModifiers(itemstack1.getAttributeModifiers(entityequipmentslot));
                    }

                    switch (entityequipmentslot.getSlotType()) {
                        case HAND:
                            this.handInventory.set(entityequipmentslot.getIndex(), itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy());
                            break;
                        case ARMOR:
                            this.armorArray.set(entityequipmentslot.getIndex(), itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1.copy());
                    }
                }
            }

            if (this.ticksExisted % 20 == 0) {
                this.getCombatTracker().reset();
            }

            if (!this.glowing) {
                boolean flag = this.isPotionActive(MobEffects.GLOWING);

                if (this.getFlag(6) != flag) {
                    this.setFlag(6, flag);
                }
            }
        }

        this.onLivingUpdate();
        double d0 = this.posX - this.prevPosX;
        double d1 = this.posZ - this.prevPosZ;
        float f3 = (float) (d0 * d0 + d1 * d1);
        float f4 = this.renderYawOffset;
        float f5 = 0.0F;
        this.prevOnGroundSpeedFactor = this.onGroundSpeedFactor;
        float f = 0.0F;

        if (f3 > 0.0025000002F) {
            f = 1.0F;
            f5 = (float) Math.sqrt(f3) * 3.0F;
            float f1 = (float) MathHelper.atan2(d1, d0) * (180F / (float) Math.PI) - 90.0F;
            float f2 = MathHelper.abs(MathHelper.wrapDegrees(this.rotationYaw) - f1);

            if (95.0F < f2 && f2 < 265.0F) {
                f4 = f1 - 180.0F;
            } else {
                f4 = f1;
            }
        }

        if (this.swingProgress > 0.0F) {
            f4 = this.rotationYaw;
        }

        if (!this.onGround) {
            f = 0.0F;
        }

        this.onGroundSpeedFactor += (f - this.onGroundSpeedFactor) * 0.3F;
        this.world.profiler.startSection("headTurn");
        f5 = this.updateDistance(f4, f5);
        this.world.profiler.endSection();
        this.world.profiler.startSection("rangeChecks");

        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        while (this.renderYawOffset - this.prevRenderYawOffset < -180.0F) {
            this.prevRenderYawOffset -= 360.0F;
        }

        while (this.renderYawOffset - this.prevRenderYawOffset >= 180.0F) {
            this.prevRenderYawOffset += 360.0F;
        }

        while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
            this.prevRotationPitch -= 360.0F;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYawHead - this.prevRotationYawHead < -180.0F) {
            this.prevRotationYawHead -= 360.0F;
        }

        while (this.rotationYawHead - this.prevRotationYawHead >= 180.0F) {
            this.prevRotationYawHead += 360.0F;
        }

        this.world.profiler.endSection();
        this.movedDistance += f5;

        if (this.isElytraFlying()) {
            ++this.ticksElytraFlying;
        } else {
            this.ticksElytraFlying = 0;
        }
    }

    @Override
    public void TimeStop() {
        rotationYawHead = prevRotationYawHead;
        cameraPitch = prevCameraPitch;
        renderYawOffset = prevRenderYawOffset;
        moveStrafing = 0.0f;
        moveVertical = 0.0f;
        moveForward = 0.0f;
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
