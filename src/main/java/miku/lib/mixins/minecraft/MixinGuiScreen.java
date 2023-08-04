package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {
    @Shadow
    public Minecraft mc;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    public abstract void drawBackground(int tint);

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                ((iMinecraft) this.mc).SET_INGAME_FOCUS();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void drawWorldBackground(int tint) {
        if (((iMinecraft) (this.mc)).MikuWorld() != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.drawBackground(tint);
        }
    }
}
