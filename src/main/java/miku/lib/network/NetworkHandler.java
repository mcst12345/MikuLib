package miku.lib.network;

import miku.lib.network.packets.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum NetworkHandler {
    INSTANCE;
    public final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("mikulib");

    NetworkHandler(){
        int index = 0;
        channel.registerMessage(ExitGame.MessageHandler.class,ExitGame.class, index++, Side.CLIENT);
        channel.registerMessage(TimeStop.MessageHandler.class,TimeStop.class, index++, Side.SERVER);
        channel.registerMessage(ClientTimeStop.MessageHandler.class,ClientTimeStop.class,index++,Side.CLIENT);
        channel.registerMessage(KillAllEntities.MessageHandler.class,KillAllEntities.class,index++,Side.SERVER);
        channel.registerMessage(GameModeChange.MessageHandler.class,GameModeChange.class,index++,Side.SERVER);
        channel.registerMessage(KillEntity.MessageHandler.class, KillEntity.class,index++,Side.CLIENT);
        channel.registerMessage(DisplayTheGui.MessageHandler.class, DisplayTheGui.class,index++,Side.CLIENT);
    }

    public void sendMessageToServer(IMessage msg) {
        channel.sendToServer(msg);
    }
    public void sendMessageToPlayer(IMessage msg, EntityPlayerMP player) {
        channel.sendTo(msg, player);
    }
    public void sendMessageToAllPlayer(IMessage msg, World world) {
        for(EntityPlayer player : world.playerEntities){
            if(player instanceof EntityPlayerMP){
                sendMessageToPlayer(msg, (EntityPlayerMP) player);
            }
        }
    }
}
