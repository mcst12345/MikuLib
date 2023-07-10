package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.api.iEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KillEntity implements IMessage {
    protected int entity;

    public KillEntity(){}

    public KillEntity(int entity){
        this.entity=entity;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        entity=buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entity);
    }

    public static class MessageHandler implements IMessageHandler<KillEntity,IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KillEntity message, MessageContext ctx) {
            System.out.println("packet arrived on "+Minecraft.getMinecraft().player.getName()+"'s client");
            WorldClient world = Minecraft.getMinecraft().world;
            Entity entity = world.getEntityByID(message.entity);
            if (entity != null) {
                System.out.println("Get entity. Killing it.");
                ((iEntity)entity).kill();
            }
            return null;
        }
    }
}
