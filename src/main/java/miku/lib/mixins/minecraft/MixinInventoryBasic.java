package miku.lib.mixins.minecraft;

import miku.lib.common.api.iInventoryBasic;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = InventoryBasic.class)
public class MixinInventoryBasic implements iInventoryBasic {
    @Shadow
    @Final
    private NonNullList<ItemStack> inventoryContents;

    @Override
    public NonNullList<ItemStack> GetInventory() {
        try {
            Field field = InventoryBasic.class.getDeclaredField("field_70482_c");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            return (NonNullList<ItemStack>) Launch.UNSAFE.getObjectVolatile(this, tmp);
        } catch (NoSuchFieldException e) {
            return inventoryContents;
        }
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
