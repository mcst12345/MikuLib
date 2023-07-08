package miku.lib.mixins;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainerCreative.class)
public class MixinGuiContainerCreative {
    @Inject(at=@At("HEAD"),method = "handleMouseClick", cancellable = true)
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type, CallbackInfo ci){
        if(slotIn.getStack() == null)ci.cancel();
    }
}
