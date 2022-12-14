package miku.lib.mixins;

import miku.lib.util.NetworkUtil;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleNetworkWrapper.class, remap = false)
public class MixinSimpleNetworkWrapper {
    @Inject(at = @At("HEAD"), method = "sendToAll", cancellable = true)
    public void sendToAll(IMessage message, CallbackInfo ci) {
        if(NetworkUtil.isBadPacket(message))ci.cancel();
    }


}
