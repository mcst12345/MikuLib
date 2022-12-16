package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.item.SpecialItem;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TimeStop implements IMessage {
    public TimeStop(){}

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<TimeStop, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(TimeStop message, MessageContext ctx) {
            SpecialItem.SetTimeStop();
            return null;
        }
    }
}
