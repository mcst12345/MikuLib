package miku.lib.common.api;

import net.minecraft.item.ItemStack;

public interface iInventoryPlayer {
    void clear();

    void ADD(ItemStack stack,int i);
}
