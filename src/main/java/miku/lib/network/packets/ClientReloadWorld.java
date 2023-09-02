package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientReloadWorld implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ClientReloadWorld, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientReloadWorld message, MessageContext ctx) {
            System.out.println("MikuInfo:Reloading world.");
            ((iMinecraft) Minecraft.getMinecraft()).reloadWorld();
            return null;
        }
    }
}
