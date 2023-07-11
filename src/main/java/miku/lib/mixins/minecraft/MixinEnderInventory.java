package miku.lib.mixins.minecraft;

import miku.lib.api.iEnderInventory;
import miku.lib.api.iInventoryBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = InventoryEnderChest.class)
public abstract class MixinEnderInventory extends InventoryBasic implements iEnderInventory {

    public MixinEnderInventory(String title, boolean customName, int slotCount) {
        super(title, customName, slotCount);
    }

    @Override
    public void Clear() {
        for (int i = 0; i < getSizeInventory(); i++) {
            ((iInventoryBasic) this).GetInventory().set(i, ItemStack.EMPTY);
        }
    }
}
