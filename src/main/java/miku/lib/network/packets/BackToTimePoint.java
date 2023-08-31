package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.util.TimeStopUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.Charset;

public class BackToTimePoint implements IMessage {
    private String record;

    private BackToTimePoint() {
    }

    public BackToTimePoint(String record) {
        this.record = record;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int tmp = buf.readInt();
        this.record = (String) buf.readCharSequence(tmp, Charset.defaultCharset());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(record.length());
        buf.writeCharSequence(record, Charset.defaultCharset());
    }

    public static class MessageHandler implements IMessageHandler<BackToTimePoint, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(BackToTimePoint message, MessageContext ctx) {
            TimeStopUtil.BackToPoint(message.record);
            return null;
        }
    }
}
