package miku.lib.mixins;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
    @Shadow
    public EntityPlayerMP player;

    @Inject(at = @At("HEAD"), method = "disconnect", cancellable = true)
    public void disconnect(ITextComponent textComponent, CallbackInfo ci) throws NoSuchFieldException, ClassNotFoundException {
        if (EntityUtil.isProtected(player)) ci.cancel();
    }
}
