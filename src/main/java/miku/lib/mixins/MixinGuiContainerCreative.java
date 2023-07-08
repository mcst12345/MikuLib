package miku.lib.mixins;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends InventoryEffectRenderer {
    public MixinGuiContainerCreative(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(at=@At("HEAD"),method = "handleMouseClick", cancellable = true)
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type, CallbackInfo ci){
        if(this.mc.player.inventory.getItemStack() == null)ci.cancel();
    }
}
