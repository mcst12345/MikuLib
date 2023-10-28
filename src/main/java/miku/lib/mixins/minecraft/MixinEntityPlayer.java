package miku.lib.mixins.minecraft;

import miku.lib.common.api.ProtectedEntity;
import miku.lib.common.api.iEnderInventory;
import miku.lib.common.api.iEntityPlayer;
import miku.lib.common.api.iInventoryPlayer;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.FieldUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Mixin(value = EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements iEntityPlayer {
    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite(remap = false)
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        if (EntityUtil.isProtected(this)) {
            String clazz = mod.getClass().toString().substring(5).trim();
            if (!ClassUtil.isGoodClass(clazz) && !ClassUtil.isLibraryClass(clazz)) {
                System.out.println(clazz);
                return;
            }
        }
        net.minecraftforge.fml.common.network.internal.FMLNetworkHandler.openGui((EntityPlayer) (Object) this, mod, modGuiId, world, x, y, z);
    }

    protected int mode = -1;

    @Override
    public void SetGameMode(int mode) {
        this.mode = mode;
    }

    @Override
    public int GetGameMode() {
        return mode;
    }

    @Shadow
    public abstract void addStat(StatBase stat);

    @Shadow
    protected InventoryEnderChest enderChest;

    @Shadow
    public InventoryPlayer inventory;

    @Shadow
    public abstract void closeScreen();

    @Shadow
    public PlayerCapabilities capabilities;
    @Shadow
    protected int flyToggleTimer;
    @Shadow
    protected FoodStats foodStats;
    @Shadow
    public float prevCameraYaw;
    @Shadow
    public float cameraYaw;
    @Shadow
    protected float speedInAir;

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    protected abstract void collideWithPlayer(Entity entityIn);

    @Shadow
    protected abstract void playShoulderEntityAmbientSound(@Nullable NBTTagCompound p_192028_1_);

    @Shadow
    public abstract NBTTagCompound getLeftShoulderEntity();

    @Shadow
    public abstract NBTTagCompound getRightShoulderEntity();

    @Shadow
    protected abstract void spawnShoulderEntities();

    protected boolean miku = false;

    @Override
    public boolean isMiku() {
        return miku;
    }

    @Override
    public void setMiku() {
        miku=true;
    }

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(at=@At("HEAD"),method = "getExperiencePoints", cancellable = true)
    public void getExperiencePoints(EntityPlayer player, CallbackInfoReturnable<Integer> cir){
        if(EntityUtil.isProtected(this))cir.setReturnValue(Integer.MAX_VALUE);
    }

    @Inject(at=@At("HEAD"),method = "canPlayerEdit", cancellable = true)
    public void canPlayerEdit(BlockPos pos, EnumFacing facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isProtected(this))cir.setReturnValue(true);
    }

    @Inject(at=@At("HEAD"),method = "isAllowEdit", cancellable = true)
    public void isAllowEdit(CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isProtected(this))cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "addExperience", cancellable = true)
    public void addExperience(int amount, CallbackInfo ci){
        if(amount<=0&&EntityUtil.isProtected(this))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "setInWeb", cancellable = true)
    public void setInWeb(CallbackInfo ci){
        if(EntityUtil.isProtected(this))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "doWaterSplashEffect", cancellable = true)
    protected void doWaterSplashEffect(CallbackInfo ci){
        if(EntityUtil.isProtected(this))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "setDead", cancellable = true)
    public void setDead(CallbackInfo ci){
        if(EntityUtil.isProtected(this))ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "damageEntity", cancellable = true)
    protected void damageEntity(DamageSource damageSrc, float damageAmount, CallbackInfo ci)  {
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "getArmorVisibility", cancellable = true)
    public void getArmorVisibility(CallbackInfoReturnable<Float> cir) {
        if (EntityUtil.isProtected(this)) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(at = @At("HEAD"), method = "damageShield", cancellable = true)
    protected void damageShield(float damage, CallbackInfo ci) {
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "damageArmor", cancellable = true)
    protected void damageArmor(float damage, CallbackInfo ci) {
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Inject(at=@At("HEAD"),method = "canAttackPlayer", cancellable = true)
    public void canAttackPlayer(EntityPlayer other, CallbackInfoReturnable<Boolean> cir){
        if (EntityUtil.isProtected(this))cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "attackEntityFrom", cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)  {
        if (EntityUtil.isProtected(this)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public InventoryEnderChest GetEnderInventory() {//net.minecraft.entity.player.EntityPlayer field_71078_a # enderChest
        try {
            long tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_71078_a);
            return (InventoryEnderChest) Launch.UNSAFE.getObjectVolatile(this, tmp);
        } catch (Throwable e) {
            return this.enderChest;
        }
    }

    @Inject(at = @At("HEAD"), method = "replaceItemInInventory", cancellable = true)
    public void replaceItemInInventory(int inventorySlot, ItemStack itemStackIn, CallbackInfoReturnable<Boolean> cir){
        if (EntityUtil.isProtected(this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void onDeath(DamageSource cause, CallbackInfo ci){
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "canHarvestBlock", cancellable = true)
    public void canHarvestBlock(IBlockState state, CallbackInfoReturnable<Boolean> cir){
        if (EntityUtil.isProtected(this)) cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "attackTargetEntityWithCurrentItem", cancellable = true)
    public void attackTargetEntityWithCurrentItem(Entity targetEntity, CallbackInfo ci){
        if (EntityUtil.isProtected(this)) {
            ItemStack itemstack = this.getHeldItem(EnumHand.MAIN_HAND);
            if (itemstack == ItemStack.EMPTY) {
                if (EntityUtil.isProtected(targetEntity)) {
                    if (((ProtectedEntity) targetEntity).CanBeKilled()) {
                        EntityUtil.Kill(targetEntity);
                    }
                } else EntityUtil.Kill(targetEntity);
                ci.cancel();
            }
        }
    }

    @Override
    public void Kill() {
        try {
            long tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_71078_a);
            Object o = Launch.UNSAFE.getObjectVolatile(this, tmp);
            if (o != null) ((iEnderInventory) o).Clear();
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            long tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_71071_by);
            Object o = Launch.UNSAFE.getObjectVolatile(this, tmp);
            if (o != null) ((iInventoryPlayer) o).Clear();
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        ((EntityPlayer) (Object) this).inventoryContainer.onContainerClosed((EntityPlayer) (Object) this);

        if (((EntityPlayer) (Object) this).openContainer != null) {
            ((EntityPlayer) (Object) this).openContainer.onContainerClosed((EntityPlayer) (Object) this);
        }
        this.addStat(StatList.DEATHS);
        this.closeScreen();
    }


    @Inject(at=@At("HEAD"),method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", cancellable = true)
    public void dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem, CallbackInfoReturnable<EntityItem> cir){
        if(droppedItem.getItem() instanceof SpecialItem)cir.setReturnValue(null);
    }

    @Inject(at = @At("HEAD"), method = "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", cancellable = true)
    public void dropItem1(ItemStack itemStackIn, boolean unused, CallbackInfoReturnable<EntityItem> cir) {
        if (itemStackIn.getItem() instanceof SpecialItem) cir.setReturnValue(null);
    }

    @Inject(at = @At("HEAD"), method = "dropItem(Z)Lnet/minecraft/entity/item/EntityItem;", cancellable = true)
    public void dropItem2(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        if (EntityUtil.isProtected(this)) cir.setReturnValue(null);
    }

    /**
     * @author mcst12345
     * @reason fuck
     */
    @Overwrite
    public void onLivingUpdate() {
        boolean protect = EntityUtil.isProtected(this);
        if (protect) {
            capabilities.allowFlying = true;
            capabilities.disableDamage = true;
            capabilities.isFlying = true;
            capabilities.allowEdit = true;
            capabilities.isCreativeMode = true;
        }
        if (!protect) {
            if (this.flyToggleTimer > 0) {
                --this.flyToggleTimer;
            }

            if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.world.getGameRules().getBoolean("naturalRegeneration")) {
                if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
                    this.heal(1.0F);
                }

                if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
                    this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
                }
            }
        }

        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        if (!protect) {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

            if (!this.world.isRemote) {
                iattributeinstance.setBaseValue(this.capabilities.getWalkSpeed());
            }

            this.jumpMovementFactor = this.speedInAir;

            if (this.isSprinting()) {
                this.jumpMovementFactor = (float) (this.jumpMovementFactor + this.speedInAir * 0.3D);
            }

            this.setAIMoveSpeed((float) iattributeinstance.getAttributeValue());
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            float f1 = (float) (Math.atan(-this.motionY * 0.20000000298023224D) * 15.0D);

            if (f > 0.1F) {
                f = 0.1F;
            }

            if (!this.onGround || this.getHealth() <= 0.0F) {
                f = 0.0F;
            }

            if (this.onGround || this.getHealth() <= 0.0F) {
                f1 = 0.0F;
            }

            this.cameraYaw += (f - this.cameraYaw) * 0.4F;
            this.cameraPitch += (f1 - this.cameraPitch) * 0.8F;

            if (this.getHealth() > 0.0F && !this.isSpectator()) {
                AxisAlignedBB axisalignedbb;

                if (this.isRiding() && !Objects.requireNonNull(this.getRidingEntity()).isDead) {
                    axisalignedbb = this.getEntityBoundingBox().union(this.getRidingEntity().getEntityBoundingBox()).grow(1.0D, 0.0D, 1.0D);
                } else {
                    axisalignedbb = this.getEntityBoundingBox().grow(1.0D, 0.5D, 1.0D);
                }

                List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);

                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = list.get(i);

                    if (!entity.isDead) {
                        this.collideWithPlayer(entity);
                    }
                }
            }

            this.playShoulderEntityAmbientSound(this.getLeftShoulderEntity());
            this.playShoulderEntityAmbientSound(this.getRightShoulderEntity());
        }

        if (!this.world.isRemote && (this.fallDistance > 0.5F || this.isInWater() || this.isRiding()) || this.capabilities.isFlying) {
            this.spawnShoulderEntities();
        }
    }
}
