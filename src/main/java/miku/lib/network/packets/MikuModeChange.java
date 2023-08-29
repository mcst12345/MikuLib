package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.item.SpecialItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MikuModeChange implements IMessage {
    private int sender, world;

    private MikuModeChange() {
    }

    public MikuModeChange(int sender, int world) {
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

    public static class MessageHandler implements IMessageHandler<MikuModeChange, IMessage> {

        @Override
        public IMessage onMessage(MikuModeChange message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.world);
            EntityPlayer sender = (EntityPlayer) world.getEntityByID(message.sender);
            SpecialItem item = SpecialItem.Get(sender);
            if (item != null) {
                item.ModeChange();
            }
            return null;
        }
    }
}
