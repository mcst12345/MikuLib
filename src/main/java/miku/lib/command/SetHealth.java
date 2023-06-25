package miku.lib.command;

import miku.lib.api.iEntityLivingBase;
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
        return "commands.miku.set_health";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server,@Nonnull ICommandSender sender,@Nonnull String[] args) throws CommandException {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.miku.set_health");
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
}