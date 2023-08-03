package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenDemo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.URI;

@Mixin(value = GuiScreenDemo.class)
public abstract class MixinGuiScreenDemo extends GuiScreen{
    @Shadow @Final private static Logger LOGGER;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                button.enabled = false;

                try {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop").invoke(null);
                    oclass.getMethod("browse", URI.class).invoke(object, new URI("http://www.minecraft.net/store?source=demo"));
                }
                catch (Throwable throwable)
                {
                    LOGGER.error("Couldn't open link", throwable);
                }

                break;
            case 2:
                this.mc.displayGuiScreen(null);
                ((iMinecraft)this.mc).SET_INGAME_FOCUS();
        }
    }
}
