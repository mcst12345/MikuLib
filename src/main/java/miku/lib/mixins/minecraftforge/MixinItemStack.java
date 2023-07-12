package miku.lib.mixins.minecraftforge;

import miku.lib.item.SpecialItem;
import miku.lib.sqlite.Sqlite;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Random;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Shadow private int stackSize;

    @Shadow private boolean isEmpty;

    @Shadow @Final private Item item;

    @Shadow @Final public static ItemStack EMPTY;

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
        if(Sqlite.IS_ITEM_BANNED(this.getItem()))cir.setReturnValue(0);
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
        }if(Sqlite.IS_ITEM_BANNED(this.getItem()))cir.setReturnValue(false);

    }

    @Inject(at = @At("HEAD"), method = "setCount", cancellable = true)
    public void setCount(int size, CallbackInfo ci) {
        if (this.getItem() instanceof SpecialItem) ci.cancel();
        if(Sqlite.IS_ITEM_BANNED(this.getItem())){
            this.stackSize=0;
            this.isEmpty = true;
        }
    }

    @Inject(at = @At("HEAD"), method = "getCount", cancellable = true)
    public void getCount(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(1);
        }
        if(Sqlite.IS_ITEM_BANNED(this.getItem()))cir.setReturnValue(0);
    }

    /**
     * @author mcst12345
     * @reason F**k
     */
    @Nullable
    @Overwrite(remap = false)
    private Item getItemRaw(){
        return Sqlite.IS_ITEM_BANNED(this.item) ? null : this.item;
    }

    @Inject(at=@At("HEAD"),method = "copy", cancellable = true)
    public void copy(CallbackInfoReturnable<ItemStack> cir){
        if(Sqlite.IS_ITEM_BANNED(this.item))cir.setReturnValue(EMPTY);
    }


}
