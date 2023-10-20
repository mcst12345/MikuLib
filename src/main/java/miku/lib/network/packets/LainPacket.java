package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.core.MikuLib;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LainPacket implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<LainPacket, IMessage> {

        @Override
        public IMessage onMessage(LainPacket message, MessageContext ctx) {
            MikuLib.setLAIN();
            return null;
        }
    }
}
