package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiGameOver.class)
public abstract class MixinGuiGameOver extends GuiScreen {
    @Shadow
    private int enableButtonsTimer;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void initGui() {
        this.buttonList.clear();
        this.enableButtonsTimer = 0;

        if (((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled()) {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.spectate")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen." + (this.mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
        } else {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (this.mc.getSession() == null) {
                (this.buttonList.get(1)).enabled = false;
            }
        }

        for (GuiButton guibutton : this.buttonList) {
            guibutton.enabled = false;
        }
    }
}
