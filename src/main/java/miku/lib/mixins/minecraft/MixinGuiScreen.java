package miku.lib.mixins.minecraft;

import miku.lib.client.api.iGuiScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiScreen.class)
public abstract class MixinGuiScreen extends Gui implements iGuiScreen {
    public List<GuiButton> buttonList() {
        return buttonList;
    }
    @Shadow
    protected List<GuiButton> buttonList;
}
