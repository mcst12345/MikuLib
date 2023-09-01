package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BackToTimePoint implements IMessage {
    private int world, sender;

    private BackToTimePoint() {
    }

    public BackToTimePoint(int world, int sender) {
        this.world = world;
        this.sender = sender;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        world = buf.readInt();
        sender = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(world);
        buf.writeInt(sender);
    }

    public static class MessageHandler implements IMessageHandler<BackToTimePoint, IMessage> {

        @Override
        @SideOnly(Side.SERVER)
        public IMessage onMessage(BackToTimePoint message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.world);
            EntityPlayer sender = (EntityPlayer) world.getEntityByID(message.sender);
            if (!EntityUtil.isProtected(sender)) EntityUtil.Kill(sender);//Check the sender
            TimeStopUtil.BackToPoint();
            return null;
        }
    }
}
