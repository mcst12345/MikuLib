package miku.lib.network;

import miku.lib.network.packets.ExitGame;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum NetworkHandler {
    INSTANCE;
    public final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("miku");

    NetworkHandler(){
        int index = 0;
        channel.registerMessage(ExitGame.MessageHandler.class,ExitGame.class, index++, Side.CLIENT);
    }

    public void sendMessageToServer(IMessage msg) {
        channel.sendToServer(msg);
    }

    public void sendMessageToPlayer(IMessage msg, EntityPlayerMP player) {
        channel.sendTo(msg, player);
    }
}
