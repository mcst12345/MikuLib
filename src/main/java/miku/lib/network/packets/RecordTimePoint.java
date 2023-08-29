package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.util.TimeStopUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RecordTimePoint implements IMessage {
    public RecordTimePoint() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<RecordTimePoint, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(RecordTimePoint message, MessageContext ctx) {
            TimeStopUtil.Record();
            return null;
        }
    }
}
