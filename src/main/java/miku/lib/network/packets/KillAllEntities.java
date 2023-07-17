package miku.lib.network.packets;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class KillAllEntities implements IMessage {
    public KillAllEntities(){}

    public KillAllEntities(int id,int sender){
        this.world = id;
        this.sender = sender;
    }

    protected int world;
    protected int sender;

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        world = buf.readInt();
        sender = buf.readInt();
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        buf.writeInt(world);
        buf.writeInt(sender);
    }

    public static class MessageHandler implements IMessageHandler<KillAllEntities, IMessage> {

        @Override
        public IMessage onMessage(KillAllEntities message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.getWorldID());
            EntityPlayer sender = (EntityPlayer) world.getEntityByID(message.sender);
            if(!EntityUtil.isProtected(sender)) EntityUtil.Kill(sender);//Check the sender
            List<Entity> entities = new ArrayList<>(world.loadedEntityList);
            EntityUtil.Kill(entities);
            return null;
        }
    }

    private int getWorldID() {
        return world;
    }
}
