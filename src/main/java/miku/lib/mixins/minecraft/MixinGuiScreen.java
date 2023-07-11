package miku.lib.mixins.minecraft;

import miku.lib.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(value = GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {
    @Shadow public Minecraft mc;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null)
            {
                ((iMinecraft)this.mc).SET_INGAME_FOCUS();
            }
        }
    }
}
