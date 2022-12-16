package miku.lib.mixins;

import miku.lib.api.iEnderInventory;
import miku.lib.api.iEntityPlayer;
import miku.lib.api.iInventoryPlayer;
import miku.lib.util.EntityUtil;
import miku.lib.util.MikuUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements iEntityPlayer {

    @Shadow public abstract void addStat(StatBase stat);

    @Shadow protected InventoryEnderChest enderChest;

    @Shadow public InventoryPlayer inventory;

    @Shadow public abstract void closeScreen();

    protected boolean Miku;

    @Override
    public void MikuMode(){
        Miku=true;
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
    public InventoryEnderChest GetEnderInventory() {
        return this.enderChest;
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
        if (EntityUtil.isProtected(this) || EntityUtil.isProtected(targetEntity)) {
            if(!EntityUtil.isProtected(targetEntity))EntityUtil.Kill(targetEntity);
            ci.cancel();
        }
    }

    @Override
    public void Kill(){
        ((iEnderInventory) ((iEntityPlayer) this).GetEnderInventory()).Clear();
        ((iInventoryPlayer) this.inventory).clear();
        ((EntityPlayer) (Object) this).inventoryContainer.onContainerClosed((EntityPlayer) (Object) this);

        if (((EntityPlayer) (Object) this).openContainer != null) {
            ((EntityPlayer) (Object) this).openContainer.onContainerClosed((EntityPlayer) (Object) this);
        }
        this.addStat(StatList.DEATHS);
        this.closeScreen();
    }

    @Inject(at=@At("HEAD"),method = "onUpdate")
    public void onUpdateStart(CallbackInfo ci){
        if(EntityUtil.isProtected(this))MikuUtil.ADD((EntityPlayer) (Object)this,Miku);
    }

    @Inject(at=@At("TAIL"),method = "onUpdate")
    public void onUpdateEnd(CallbackInfo ci){

    }
}
