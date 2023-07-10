package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.api.iEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KillEntity implements IMessage {
    protected int world;
    protected int entity;

    public KillEntity(){}

    public KillEntity(int world ,int entity){
        this.world=world;
        this.entity=entity;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        world=buf.readInt();
        entity=buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(world);
        buf.writeInt(entity);
    }

    public static class MessageHandler implements IMessageHandler<KillEntity,IMessage> {

        @Override
        public IMessage onMessage(KillEntity message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.world);
            Entity entity = world.getEntityByID(message.entity);
            if (entity != null) {
                ((iEntity)entity).kill();
            }
            return null;
        }
    }
}
