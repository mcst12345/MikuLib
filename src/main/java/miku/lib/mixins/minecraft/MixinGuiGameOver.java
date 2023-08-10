package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(value = GuiGameOver.class)
public abstract class MixinGuiGameOver extends GuiScreen {
    @Shadow
    private int enableButtonsTimer;

    @Shadow
    @Final
    private ITextComponent causeOfDeath;

    @Shadow
    @Nullable
    public abstract ITextComponent getClickedComponentAt(int p_184870_1_);

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

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                this.mc.player.respawnPlayer();
                this.mc.displayGuiScreen(null);
                break;
            case 1:

                if (((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled()) {
                    this.mc.displayGuiScreen(new GuiMainMenu());
                } else {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    this.mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void confirmClicked(boolean result, int id) {
        if (result) {
            if (((iMinecraft) (this.mc)).MikuWorld() != null) {
                ((iMinecraft) (this.mc)).MikuWorld().sendQuittingDisconnectingPacket();
            }

            this.mc.loadWorld(null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        } else {
            this.mc.player.respawnPlayer();
            this.mc.displayGuiScreen(null);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean flag = ((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled();
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(this.fontRenderer, I18n.format(flag ? "deathScreen.title.hardcore" : "deathScreen.title"), this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (this.causeOfDeath != null) {
            this.drawCenteredString(this.fontRenderer, this.causeOfDeath.getFormattedText(), this.width / 2, 85, 16777215);
        }

        this.drawCenteredString(this.fontRenderer, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + this.mc.player.getScore(), this.width / 2, 100, 16777215);

        if (this.causeOfDeath != null && mouseY > 85 && mouseY < 85 + this.fontRenderer.FONT_HEIGHT) {
            ITextComponent itextcomponent = this.getClickedComponentAt(mouseX);

            if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
                this.handleComponentHover(itextcomponent, mouseX, mouseY);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
