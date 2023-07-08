package miku.lib.mixins;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class)
public class MixinGuiContainer {

    @Inject(at=@At("HEAD"),method = "drawSlot", cancellable = true)
    private void drawSlot(Slot slotIn, CallbackInfo ci){
        if(slotIn.getStack().getItem() == null)ci.cancel();
    }
}
