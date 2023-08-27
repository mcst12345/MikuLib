package miku.lib.common.command;

import miku.lib.common.api.iEntityLivingBase;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class SetHealth extends CommandBase {
    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }
    @Override
    @Nonnull
    public String getName() {
        return "set_health";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.miku.set_health.usage";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) throws CommandException {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.set_health.usage");
        }
        else {
            if (args.length > 1){
                Entity entity = getEntity(server, sender, args[0]);
                if (entity instanceof EntityLivingBase) {
                    double value;
                    EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                    value = parseDouble(entityLivingBase.getHealth(), args[1], false);
                    ((iEntityLivingBase) entityLivingBase).SetHealth((float) value);
                }
            }
            else {
                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                double value;
                value = parseDouble(player.getHealth(), args[0], false);
                ((iEntityLivingBase) player).SetHealth((float) value);
            }
        }
    }

    public boolean isUsernameIndex(@Nonnull String[] args, int index)
    {
        return index == 0;
    }

    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
    }
}
