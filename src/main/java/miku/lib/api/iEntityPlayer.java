package miku.lib.api;

import net.minecraft.inventory.InventoryEnderChest;

public interface iEntityPlayer {
    void Kill();

    InventoryEnderChest GetEnderInventory();

    boolean isMiku();

    void setMiku();
}
