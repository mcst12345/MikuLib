package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ShowEntityList implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ShowEntityList, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ShowEntityList message, MessageContext ctx) {
            WorldClient world = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
            for (Entity e : world.loadedEntityList) {
                System.out.println(e.getClass() + "," + e.getName() + "," + e.getUniqueID());
            }
            return null;
        }
    }
}
