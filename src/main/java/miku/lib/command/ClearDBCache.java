package miku.lib.command;

import miku.lib.sqlite.Sqlite;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class ClearDBCache extends CommandBase {
    public int getRequiredPermissionLevel()
    {
        return 3;
    }
    @Override
    @Nonnull
    public String getName() {
        return "clear_db_cache";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.clear_db_cache.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) {
        Sqlite.ClearDBCache();
    }
}
