package miku.lib.mixins.minecraftforge;

import miku.lib.common.core.MikuLib;
import net.minecraft.command.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static net.minecraft.util.text.TextFormatting.RED;

@Mixin(value = ClientCommandHandler.class)
public abstract class MixinClientCommandHandler extends CommandHandler {
    @Shadow(remap = false)
    protected abstract TextComponentTranslation format(TextFormatting color, String str, Object... args);

    /**
     * @author mcst12345
     * @reason Shit!
     */
    @Overwrite
    public int executeCommand(ICommandSender sender, String message) {
        message = message.trim();

        boolean usedSlash = message.startsWith("/");
        if (usedSlash) {
            message = message.substring(1);
        }

        String[] temp = message.split(" ");
        String[] args = new String[temp.length - 1];
        String commandName = temp[0];
        System.arraycopy(temp, 1, args, 0, args.length);
        ICommand icommand = getCommands().get(commandName);

        try {
            if (icommand == null || (!usedSlash && icommand instanceof IClientCommand && !((IClientCommand) icommand).allowUsageWithoutPrefix(sender, message))) {
                return 0;
            }

            if (icommand.checkPermission(this.getServer(), sender)) {
                CommandEvent event = new CommandEvent(icommand, sender, args);
                if (MikuLib.MikuEventBus().post(event)) {
                    if (event.getException() != null) {
                        throw event.getException();
                    }
                    return 0;
                }

                this.tryExecute(sender, args, icommand, message);
                return 1;
            } else {
                sender.sendMessage(format(RED, "commands.generic.permission"));
            }
        } catch (WrongUsageException wue) {
            sender.sendMessage(format(RED, "commands.generic.usage", format(RED, wue.getMessage(), wue.getErrorObjects())));
        } catch (CommandException ce) {
            sender.sendMessage(format(RED, ce.getMessage(), ce.getErrorObjects()));
        } catch (Throwable t) {
            sender.sendMessage(format(RED, "commands.generic.exception"));
            FMLLog.log.error("Command '{}' threw an exception:", message, t);
        }

        return -1;
    }
}
