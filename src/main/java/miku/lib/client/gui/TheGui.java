package miku.lib.client.gui;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TheGui extends GuiScreen {
    private int enableButtonsTimer;

    public TheGui()
    {
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.enableButtonsTimer = 0;

        if (((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled()) {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.spectate")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen." + (this.mc.isIntegratedServerRunning() ? "deleteWorld" : "leaveServer"))));
        } else {
            this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 72, I18n.format("deathScreen.respawn")));
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (this.mc.getSession() == null)
            {
                (this.buttonList.get(1)).enabled = false;
            }
        }

        for (GuiButton guibutton : this.buttonList)
        {
            guibutton.enabled = false;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id)
        {
            case 0:
                ((iMinecraft) this.mc).MikuPlayer().respawnPlayer();
                this.mc.displayGuiScreen(null);
                break;
            case 1:
                this.mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            if (((iMinecraft) (this.mc)).MikuWorld() != null) {
                ((iMinecraft) (this.mc)).MikuWorld().sendQuittingDisconnectingPacket();
            }

            this.mc.loadWorld(null);
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            ((iMinecraft) this.mc).MikuPlayer().respawnPlayer();
            this.mc.displayGuiScreen(null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        boolean flag = ((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled();
        this.drawGradientRect(0, 0, this.width, this.height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        this.drawCenteredString(this.fontRenderer, I18n.format(flag ? "deathScreen.title.hardcore" : "deathScreen.title"), this.width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();


        this.drawCenteredString(this.fontRenderer, I18n.format("deathScreen.score") + ": " + TextFormatting.YELLOW + ((iMinecraft) this.mc).MikuPlayer().getScore(), this.width / 2, 100, 16777215);


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.enableButtonsTimer;

        if (this.enableButtonsTimer == 20)
        {
            for (GuiButton guibutton : this.buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }
}
