package miku.lib.mixins.minecraft;

import miku.lib.common.item.SpecialItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Slot.class)
public abstract class MixinSlot {
    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    @Final
    public IInventory inventory;


    @Shadow
    @Final
    private int slotIndex;

    @Shadow
    public abstract void onSlotChanged();

    /**
     * @author mcst12345
     * @reason for hiding items
     */
    @Overwrite
    public boolean getHasStack() {
        if (this.getStack() == null) return false;
        return !this.getStack().isEmpty();
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void putStack(ItemStack stack) {
        if (this.inventory.getStackInSlot(this.slotIndex).getItem() instanceof SpecialItem) {
            return;
        }
        this.inventory.setInventorySlotContents(this.slotIndex, stack);
        this.onSlotChanged();
    }
}
