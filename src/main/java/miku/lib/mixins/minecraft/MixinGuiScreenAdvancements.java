package miku.lib.mixins.minecraft;

import miku.lib.common.api.iMinecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;

@Mixin(value = GuiScreenAdvancements.class)
public abstract class MixinGuiScreenAdvancements extends GuiScreen{
    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == this.mc.gameSettings.keyBindAdvancements.getKeyCode()) {
            this.mc.displayGuiScreen(null);
            ((iMinecraft) this.mc).SET_INGAME_FOCUS();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

}
