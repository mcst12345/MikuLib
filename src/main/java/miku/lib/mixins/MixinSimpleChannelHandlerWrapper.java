package miku.lib.mixins;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import miku.lib.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleChannelHandlerWrapper.class,remap = false)
public abstract class MixinSimpleChannelHandlerWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleChannelInboundHandler<REQ> {
    @Inject(at=@At("TAIL"),method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;)V", cancellable = true)
    protected void channelRead0(ChannelHandlerContext ctx, REQ msg, CallbackInfo ci)  {
        if(NetworkUtil.isBadPacket(msg))ci.cancel();
    }
}
