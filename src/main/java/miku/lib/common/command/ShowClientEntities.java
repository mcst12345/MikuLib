package miku.lib.common.command;

import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.ShowEntityList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class ShowClientEntities extends CommandBase {
    @NotNull
    @Override
    public String getName() {
        return "show_client_entities";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "commands.miku.show_entities";
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull String[] args) throws CommandException {
        EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
        NetworkHandler.INSTANCE.sendMessageToPlayer(new ShowEntityList(), playerMP);
    }
}
