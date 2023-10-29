package miku.lib.mixins.minecraft;

import miku.lib.client.api.iGuiContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen implements iGuiContainer {
    public int guiLeft() {
        return guiLeft;

    }

    public int guiTop() {
        return guiTop;
    }

    @Shadow
    protected int guiLeft;

    @Shadow
    protected int guiTop;
}
