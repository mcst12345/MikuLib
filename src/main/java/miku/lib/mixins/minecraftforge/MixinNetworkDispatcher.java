package miku.lib.mixins.minecraftforge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import miku.lib.common.core.MikuLib;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(value = NetworkDispatcher.class)
public abstract class MixinNetworkDispatcher extends SimpleChannelInboundHandler<Packet<?>> implements ChannelOutboundHandler {
    @Shadow
    private NetworkDispatcher.ConnectionType connectionType;

    @Shadow
    @Final
    public NetworkManager manager;

    @Shadow
    private NetworkDispatcher.ConnectionState state;

    @Shadow
    protected abstract void cleanAttributes(ChannelHandlerContext ctx);

    @Shadow
    @Final
    private Side side;

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    private void completeClientSideConnection(NetworkDispatcher.ConnectionType type) {
        this.connectionType = type;
        FMLLog.log.info("[{}] Client side {} connection established", Thread.currentThread().getName(), this.connectionType.name().toLowerCase(Locale.ENGLISH));
        this.state = NetworkDispatcher.ConnectionState.CONNECTED;
        MikuLib.MikuEventBus().post(new FMLNetworkEvent.ClientConnectedToServerEvent(manager, this.connectionType.name()));
    }

    /**
     * @author mcst12345
     * @reason Shit!!!
     */
    @Overwrite
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
        if (side == Side.CLIENT) {
            MikuLib.MikuEventBus().post(new FMLNetworkEvent.ClientDisconnectionFromServerEvent(manager));
        } else {
            MikuLib.MikuEventBus().post(new FMLNetworkEvent.ServerDisconnectionFromClientEvent(manager));
        }
        cleanAttributes(ctx);
        ctx.disconnect(promise);
    }

    /**
     * @author mcst12345
     * @reason Fuck!!
     */
    @Overwrite
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (side == Side.CLIENT) {
            MikuLib.MikuEventBus().post(new FMLNetworkEvent.ClientDisconnectionFromServerEvent(manager));
        } else {
            MikuLib.MikuEventBus().post(new FMLNetworkEvent.ServerDisconnectionFromClientEvent(manager));
        }
        cleanAttributes(ctx);
        ctx.close(promise);
    }
}
