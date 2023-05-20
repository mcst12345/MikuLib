package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.api.iEntity;
import miku.lib.item.SpecialItem;
import miku.lib.network.NetworkHandler;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
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

    public TimeStop() {
    }

    public TimeStop(int id){
        this.world = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        world = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(world);
    }

    public static class MessageHandler implements IMessageHandler<TimeStop, IMessage> {

        @Override
        public IMessage onMessage(TimeStop message, MessageContext ctx) {
            SpecialItem.SetTimeStop();
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.getWorldID());
            for(Entity entity : world.loadedEntityList){
                if(EntityUtil.isProtected(entity))continue;
                if(entity instanceof EntityPlayer) {
                    NetworkHandler.INSTANCE.sendMessageToPlayer(new ClientTimeStop(), (EntityPlayerMP) entity);
                    continue;
                }
                ((iEntity)entity).SetTimeStop();
            }
            return null;
        }
    }

    private int getWorldID() {
        return world;
    }
}
