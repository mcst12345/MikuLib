package miku.lib.mixins;

import miku.lib.api.iInventoryBasic;
import miku.lib.sqlite.Sqlite;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InventoryBasic.class)
public class MixinInventoryBasic implements iInventoryBasic {
    @Shadow
    @Final
    private NonNullList<ItemStack> inventoryContents;

    @Override
    public NonNullList<ItemStack> GetInventory() {
        return inventoryContents;
    }

    @Inject(at=@At("TAIL"),method = "getStackInSlot", cancellable = true)
    public void getStackInSlot(int index, CallbackInfoReturnable<ItemStack> cir){
        if(Sqlite.IS_ITEM_BANNED(cir.getReturnValue().getItem()))cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(at=@At("HEAD"),method = "addItem", cancellable = true)
    public void addItem(ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
        if(Sqlite.IS_ITEM_BANNED(stack.getItem()))cir.setReturnValue(ItemStack.EMPTY);
    }
}
