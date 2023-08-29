package miku.lib.mixins.minecraft;

import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
    @Shadow
    public EntityPlayerMP player;

    @Inject(at = @At("HEAD"), method = "disconnect", cancellable = true)
    public void disconnect(ITextComponent textComponent, CallbackInfo ci) {
        if (EntityUtil.isProtected(player)) ci.cancel();
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void processInput(CPacketInput packetIn) {
        if (Sqlite.DEBUG()) {
            System.out.println("MikuInfo:Processing CPacket.");
        }
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayServer) (Object) this, this.player.getServerWorld());
        this.player.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
    }
}
