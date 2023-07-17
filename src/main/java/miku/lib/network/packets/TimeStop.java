package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.item.SpecialItem;
import miku.lib.network.NetworkHandler;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TimeStop implements IMessage {
    protected int world;
    protected int sender;

    public TimeStop() {
    }

    public TimeStop(int id,int sender){
        this.world = id;
        this.sender = sender;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        world = buf.readInt();
        sender =buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(world);
        buf.writeInt(sender);
    }

    public static class MessageHandler implements IMessageHandler<TimeStop, IMessage> {

        @Override
        public IMessage onMessage(TimeStop message, MessageContext ctx) {
            SpecialItem.SetTimeStop();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.world);
            EntityPlayer sender = (EntityPlayer) world.getEntityByID(message.sender);
            if(!EntityUtil.isProtected(sender))EntityUtil.Kill(sender);
            for(EntityPlayer player : world.playerEntities){
                NetworkHandler.INSTANCE.sendMessageToPlayer(new ClientTimeStop(), (EntityPlayerMP) player);
            }
            return null;
        }
    }
}
