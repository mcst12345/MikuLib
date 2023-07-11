package miku.lib.mixins.minecraft;

import miku.lib.command.MikuCommand;
import miku.lib.util.Register;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerCommandManager.class)
public abstract class MixinServerCommandManager extends CommandHandler {

    @Inject(at=@At("TAIL"),method = "<init>")
    public void ServerCommandManager(MinecraftServer serverIn, CallbackInfo ci){
        MikuCommand.Init();
        for(CommandBase command : Register.commands){
            this.registerCommand(command);
        }
        if(serverIn.isDedicatedServer()){
            for(CommandBase command : Register.server_commands){
                this.registerCommand(command);
            }
        }
    }
}
