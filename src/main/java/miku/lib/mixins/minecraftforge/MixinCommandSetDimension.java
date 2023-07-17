package miku.lib.mixins.minecraftforge;

import miku.lib.util.EntityUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandSetDimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommandSetDimension.class)
public abstract class MixinCommandSetDimension extends CommandBase {
    @Inject(at=@At("HEAD"),method = "execute", cancellable = true)
    public void execute(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci) throws CommandException {
        if(EntityUtil.isDEAD(getEntity(server,sender,args[0])))ci.cancel();
    }
}
