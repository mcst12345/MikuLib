package miku.lib.common.command;

import miku.lib.common.util.EntityUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MikuKill extends CommandBase {
    public int getRequiredPermissionLevel()
    {
        return 3;
    }
    @Override
    @Nonnull
    public String getName() {
        return "miku_kill";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.miku.miku_kill.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) throws CommandException {
        if(args.length == 0){
            EntityUtil.Kill(getCommandSenderAsPlayer(sender));
            notifyCommandListener(sender, this, "commands.kill.successful", getCommandSenderAsPlayer(sender).getDisplayName());
        }
        else {
            List<Entity> toKill = new ArrayList<>();
            if (Objects.equals(args[0], "@enp")) {
                List<Entity> list = server.getEntityWorld().loadedEntityList;
                toKill.addAll(list);
                toKill.remove(getCommandSenderAsPlayer(sender));
                toKill.removeIf(e -> e instanceof EntityPlayer);
                //System.out.println(toKill);
                EntityUtil.Kill(toKill);
            } else if (Objects.equals(args[0], "@item")) {
                List<Entity> list = server.getEntityWorld().loadedEntityList;
                toKill.addAll(list);
                toKill.remove(getCommandSenderAsPlayer(sender));
                toKill.removeIf(e -> !(e instanceof EntityItem));
                //System.out.println(toKill);
                EntityUtil.Kill(toKill);
            } else {
                Entity entity = getEntity(server, sender, args[0]);
                EntityUtil.Kill(entity);
            }
        }
    }

    public boolean isUsernameIndex(@Nonnull String[] args, int index)
    {
        return index == 0;
    }

    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
    }
}
