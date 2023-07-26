package miku.lib.common.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public interface iEntityLiving {
    void Kill();

    void ClearAI();

    void ClearInventory();

    NonNullList<ItemStack> inventoryHands();

    NonNullList<ItemStack> inventoryArmor();
}
