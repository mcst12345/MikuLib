package miku.lib.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

public class MikuInsaneMode extends CommandBase {
    protected static boolean MikuInsaneMode = false;

    @Override
    @Nonnull
    public String getName() {
        return "miku_insane_mode";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.miku.insane";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args) {
        if (args.length == 0) {
            MikuInsaneMode = !MikuInsaneMode;
        } else {
            String s = args[0];
            MikuInsaneMode = Boolean.parseBoolean(s);
        }
        sender.sendMessage(new TextComponentString("MikuInsaneMode:" + MikuInsaneMode));
    }

    public static boolean isMikuInsaneMode() {
        return MikuInsaneMode;
    }
}
