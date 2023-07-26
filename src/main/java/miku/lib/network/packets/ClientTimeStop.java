package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.api.iMinecraft;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientTimeStop implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ClientTimeStop, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientTimeStop message, MessageContext ctx) {
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player))return null;
            ((iMinecraft) Minecraft.getMinecraft()).SetTimeStop();
            return null;
        }
    }
}
