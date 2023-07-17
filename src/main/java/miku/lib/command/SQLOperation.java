package miku.lib.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class SQLOperation extends CommandBase {
    public int getRequiredPermissionLevel()
{
    return 3;
}
    @Override
    @Nonnull
    public String getName() {
        return "sql";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.sql.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) {
        //miku.lib.mixins.minecraft.MixinCommandHandler
    }
}
