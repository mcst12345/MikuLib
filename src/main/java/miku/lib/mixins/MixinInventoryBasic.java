package miku.lib.mixins;

import miku.lib.api.iInventoryBasic;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = InventoryBasic.class)
public class MixinInventoryBasic implements iInventoryBasic {
    @Shadow
    @Final
    private NonNullList<ItemStack> inventoryContents;

    @Override
    public NonNullList<ItemStack> GetInventory() {
        return inventoryContents;
    }
}
