package miku.lib.mixins;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Inject(at=@At("HEAD"),method = "drawSlot", cancellable = true)
    private void drawSlot(Slot slotIn, CallbackInfo ci){
        if(slotIn.getStack() == null)ci.cancel();
    }
}
