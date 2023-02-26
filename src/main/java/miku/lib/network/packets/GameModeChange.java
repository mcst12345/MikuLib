package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.api.iEntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GameModeChange implements IMessage {
    protected int mode;
    protected int world;
    protected int player;

    public GameModeChange() {
    }

    public GameModeChange(int mode,int world,int player){
        this.mode = mode;
        this.world = world;
        this.player = player;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readInt();
        world = buf.readInt();
        player = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(mode);
        buf.writeInt(world);
        buf.writeInt(player);
    }

    public static class MessageHandler implements IMessageHandler<GameModeChange, IMessage> {

        @Override
        public IMessage onMessage(GameModeChange message, MessageContext ctx) {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            World world = server.getWorld(message.getWorldID());
            EntityPlayer player = (EntityPlayer) world.getEntityByID(message.player);
            if(player != null)((iEntityPlayer)player).SetGameMode(message.mode);
            return null;
        }
    }

    private int getWorldID() {
        return world;
    }
}
