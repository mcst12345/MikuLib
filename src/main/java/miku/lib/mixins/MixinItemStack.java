package miku.lib.mixins;

import miku.lib.item.SpecialItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Inject(at = @At("HEAD"), method = "isItemStackDamageable", cancellable = true)
    public void isItemStackDamageable(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "isItemDamaged", cancellable = true)
    public void isItemDamaged(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "setItemDamage", cancellable = true)
    public void setItemDamage(int meta, CallbackInfo ci) {
        if (this.getItem() instanceof SpecialItem) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getItemDamage", cancellable = true)
    public void getItemDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(0);
        }
    }

    @Inject(at = @At("HEAD"), method = "getMaxDamage", cancellable = true)
    public void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
    }

    @Inject(at = @At("HEAD"), method = "attemptDamageItem", cancellable = true)
    public void attemptDamageItem(int amount, Random rand, EntityPlayerMP damager, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "canDestroy", cancellable = true)
    public void canDestroy(Block blockIn, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "setCount", cancellable = true)
    public void setCount(int size, CallbackInfo ci) {
        if (this.getItem() instanceof SpecialItem) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "getCount", cancellable = true)
    public void getCount(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(1);
        }
    }

}
