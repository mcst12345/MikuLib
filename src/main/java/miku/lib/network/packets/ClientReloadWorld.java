package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.api.iMapStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientReloadWorld implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ClientReloadWorld, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ClientReloadWorld message, MessageContext ctx) {
            WorldClient worldClient = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
            Minecraft.getMinecraft().loadWorld(null);
            MapStorage mapStorage = worldClient.getMapStorage();
            if (mapStorage != null) {
                ((iMapStorage) mapStorage).clearData();
            }
            Minecraft.getMinecraft().loadWorld(worldClient);
            return null;
        }
    }
}
