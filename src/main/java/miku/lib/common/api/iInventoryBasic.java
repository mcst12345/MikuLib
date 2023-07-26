package miku.lib.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface iInventoryBasic {
    NonNullList<ItemStack> GetInventory();
}
