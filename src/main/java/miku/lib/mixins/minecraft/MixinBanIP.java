package miku.lib.mixins.minecraft;

import miku.lib.util.EntityUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBanIp;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandBanIp.class)
public class MixinBanIP {
    @Inject(at = @At("HEAD"), method = "execute", cancellable = true)
    public void execute(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci){
        EntityPlayerMP entityplayermp = server.getPlayerList().getPlayerByUsername(args[0]);
        if (entityplayermp != null) {
            if (EntityUtil.isProtected(entityplayermp)) ci.cancel();
        }
    }
}
