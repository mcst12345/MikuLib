package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.api.iMinecraft;
import miku.lib.gui.TheGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class DisplayTheGui implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<DisplayTheGui, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(DisplayTheGui message, MessageContext ctx) {
            Minecraft.getMinecraft().gameSettings.hideGUI=false;
            TheGui gui = new TheGui();
            if(Minecraft.getMinecraft().currentScreen!=null)Minecraft.getMinecraft().currentScreen.onGuiClosed();
            Minecraft.getMinecraft().currentScreen = gui;
            ((iMinecraft)Minecraft.getMinecraft()).SET_INGAME_NOT_FOCUS();
            while (Mouse.next())
            {
            }

            while (Keyboard.next())
            {
            }

            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            gui.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            Minecraft.getMinecraft().skipRenderWorld = false;
            Minecraft.getMinecraft().gameSettings.hideGUI=false;
            return null;
        }
    }
}
