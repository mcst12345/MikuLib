package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.Native.NativeUtil;
import miku.lib.common.api.iEntity;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KillEntity implements IMessage {
    protected int entity;
    protected int sender;

    public KillEntity(){}

    public KillEntity(int entity,int sender){
        this.entity = entity;
        this.sender = sender;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        entity = buf.readInt();
        sender = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entity);
        buf.writeInt(sender);
    }

    public static class MessageHandler implements IMessageHandler<KillEntity,IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(KillEntity message, MessageContext ctx) {
            WorldClient world = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
            EntityPlayer sender = null;
            try {
                sender = (EntityPlayer) world.getEntityByID(message.sender);
            } catch (Throwable ignored) {
            }
            if (!EntityUtil.isProtected(sender)) EntityUtil.Kill(sender);//Check the package sender
            Entity entity = world.getEntityByID(message.entity);
            if (entity != null) {
                try {
                    NativeUtil.Kill(entity);
                } catch (Throwable t) {
                    t.printStackTrace();
                    ((iEntity) entity).kill();
                }
            }
            return null;
        }
    }
}
