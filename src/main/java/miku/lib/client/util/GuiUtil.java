package miku.lib.client.util;

import miku.lib.client.api.iMinecraft;
import miku.lib.client.gui.TheGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


public class GuiUtil {
    @SideOnly(Side.CLIENT)
    public static void DisPlayTheGui() {
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
        TheGui gui = new TheGui();
        if (Minecraft.getMinecraft().currentScreen != null) Minecraft.getMinecraft().currentScreen.onGuiClosed();
        Minecraft.getMinecraft().currentScreen = gui;
        ((iMinecraft) Minecraft.getMinecraft()).SET_INGAME_NOT_FOCUS();
        while (Mouse.next()) {
        }

        while (Keyboard.next()) {
        }

        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        gui.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
        Minecraft.getMinecraft().skipRenderWorld = false;
        Minecraft.getMinecraft().gameSettings.hideGUI = false;
    }
}
