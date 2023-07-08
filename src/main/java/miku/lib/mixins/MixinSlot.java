package miku.lib.mixins;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Slot.class)
public abstract class MixinSlot {
    @Shadow public abstract ItemStack getStack();

    /**
     * @author mcst12345
     * @reason for hiding items
     */
    @Overwrite
    public boolean getHasStack(){
        if(this.getStack() == null)return false;
        return !this.getStack().isEmpty();
    }
}
