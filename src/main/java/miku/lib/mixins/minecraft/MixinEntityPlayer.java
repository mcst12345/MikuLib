package miku.lib.mixins.minecraft;

import miku.lib.common.api.ProtectedEntity;
import miku.lib.common.api.iEnderInventory;
import miku.lib.common.api.iEntityPlayer;
import miku.lib.common.api.iInventoryPlayer;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.EntityUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

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

    @Shadow public abstract void addStat(StatBase stat);

    @Shadow protected InventoryEnderChest enderChest;

    @Shadow public InventoryPlayer inventory;

    @Shadow public abstract void closeScreen();

    @Shadow public PlayerCapabilities capabilities;
    protected boolean miku = false;

    @Override
    public boolean isMiku(){
        return miku;
    }

    @Override
    public void setMiku(){
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
            Field field = EntityPlayer.class.getDeclaredField("field_71078_a");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            return (InventoryEnderChest) Launch.UNSAFE.getObjectVolatile(this, tmp);
        } catch (NoSuchFieldException e) {
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
            if(EntityUtil.isProtected(targetEntity)) {
                if(((ProtectedEntity)targetEntity).CanBeKilled()){
                    EntityUtil.Kill(targetEntity);
                }
            } else EntityUtil.Kill(targetEntity);
            ci.cancel();
        }
    }

    @Override
    public void Kill() {
        //((iEnderInventory) ((iEntityPlayer) this).GetEnderInventory()).Clear();
        try {
            Field field = EntityPlayer.class.getDeclaredField("field_71078_a");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            ((iEnderInventory) Launch.UNSAFE.getObjectVolatile(this, tmp)).Clear();
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            Field field = EntityPlayer.class.getDeclaredField("field_71071_by");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            ((iInventoryPlayer) Launch.UNSAFE.getObjectVolatile(this, tmp)).Clear();
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }

        //((iInventoryPlayer) this.inventory).clear();

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

    @Inject(at=@At("HEAD"),method = "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/item/EntityItem;", cancellable = true)
    public void dropItem1(ItemStack itemStackIn, boolean unused, CallbackInfoReturnable<EntityItem> cir){
        if(itemStackIn.getItem() instanceof SpecialItem)cir.setReturnValue(null);
    }

    @Inject(at=@At("HEAD"),method = "dropItem(Z)Lnet/minecraft/entity/item/EntityItem;", cancellable = true)
    public void dropItem2(boolean dropAll, CallbackInfoReturnable<EntityItem> cir){
        if(EntityUtil.isProtected(this))cir.setReturnValue(null);
    }

    @Inject(at=@At("HEAD"),method = "onUpdate")
    public void onUpdate(CallbackInfo ci){
        if(EntityUtil.isProtected(this)){
            capabilities.allowFlying = true;
            capabilities.disableDamage = true;
            capabilities.isFlying = true;
            capabilities.allowEdit = true;
            capabilities.isCreativeMode = true;
        }
    }
}
