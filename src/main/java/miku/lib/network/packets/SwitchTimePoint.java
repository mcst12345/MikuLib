package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;

public class SwitchTimePoint implements IMessage {
    private int sender, world;

    private SwitchTimePoint() {
    }

    public SwitchTimePoint(int sender, int world) {
        this.sender = sender;
        this.world = world;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sender = buf.readInt();
        world = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(sender);
        buf.writeInt(world);
    }

    public static class MessageHandler implements IMessageHandler<SwitchTimePoint, IMessage> {

        @Override
        public IMessage onMessage(SwitchTimePoint message, MessageContext ctx) {
            File file = TimeStopUtil.SwitchTimePoint();
            if (file != null) {
                try {
                    MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                    World world = server.getWorld(message.world);
                    EntityPlayerMP sender = (EntityPlayerMP) world.getEntityByID(message.sender);
                    if (sender != null) {
                        sender.sendMessage(new TextComponentString("Switching time point:" + file.getName()));
                    }
                } catch (Throwable throwable) {
                    System.out.println("MikuWarn:Can't get message sender of package:SwitchTimePoint.");
                }
            }
            return null;
        }
    }
}
