package miku.lib.mixins;

import miku.lib.util.EntityUtil;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketEntityStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Shadow private WorldClient world;

    @Inject(at=@At("HEAD"),method = "handleEntityStatus", cancellable = true)
    public void handleEntityStatus(SPacketEntityStatus packetIn, CallbackInfo ci){
        if(EntityUtil.isProtected(packetIn.getEntity(this.world))){
            if(packetIn.getOpCode() == 3 || packetIn.getOpCode() == 30 || packetIn.getOpCode() == 29 || packetIn.getOpCode() == 37 || packetIn.getOpCode() == 33 || packetIn.getOpCode() == 36 || packetIn.getOpCode() == 20 || packetIn.getOpCode() == 2 || packetIn.getOpCode() == 35)ci.cancel();
        }
    }
}
