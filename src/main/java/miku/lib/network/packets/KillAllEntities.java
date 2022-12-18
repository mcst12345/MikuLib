package miku.lib.network.packets;

import miku.lib.util.EntityUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class KillAllEntities implements IMessage {
    public KillAllEntities(){}

    public KillAllEntities(int id){
        this.world = id;
    }

    protected int world;

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        world = buf.readInt();
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        buf.writeInt(world);
    }

    public static class MessageHandler implements IMessageHandler<KillAllEntities, IMessage> {

        @Override
        public IMessage onMessage(KillAllEntities message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.getWorldID());
            EntityUtil.Kill(world.loadedEntityList);
            return null;
        }
    }

    private int getWorldID() {
        return world;
    }
}
