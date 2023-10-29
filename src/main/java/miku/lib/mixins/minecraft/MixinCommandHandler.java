package miku.lib.mixins.minecraft;

import miku.lib.common.command.SQLOperation;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

@Mixin(value = CommandHandler.class)
public abstract class MixinCommandHandler {


    @Shadow
    @Final
    private Map<String, ICommand> commandMap;


    @Shadow
    protected abstract MinecraftServer getServer();

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Set<ICommand> commandSet;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public boolean tryExecute(ICommandSender sender, String[] args, ICommand command, String input) throws CommandException {
        if (command instanceof SQLOperation || input.startsWith("/sql ")) {
            String statement = input.substring(4).trim();
            System.out.println(statement);
            try (ResultSet rs = Sqlite.ExecuteSQL(statement)) {
                if (rs != null && sender instanceof EntityPlayerMP) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnCount = md.getColumnCount();
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            sender.sendMessage(new TextComponentString(md.getColumnName(i) + ":" + rs.getObject(i)));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new CommandException(e.getMessage());
            }
            return true;
        }
        try {
            command.execute(this.getServer(), sender, args);
            return true;
        } catch (WrongUsageException wrongusageexception) {
            TextComponentTranslation textcomponenttranslation2 = new TextComponentTranslation("commands.generic.usage", new TextComponentTranslation(wrongusageexception.getMessage(), wrongusageexception.getErrorObjects()));
            textcomponenttranslation2.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(textcomponenttranslation2);
        } catch (CommandException commandexception) {
            TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(textcomponenttranslation1);
        } catch (Throwable throwable) {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.generic.exception");
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(textcomponenttranslation);
            LOGGER.warn("Couldn't process command: " + input, throwable);
        }

        return false;
    }

    /**
     * @author mcst12345
     * @reason THE FUCK?
     */
    @Overwrite
    public ICommand registerCommand(ICommand command) {
        if (command == null) return null;
        this.commandMap.put(command.getName(), command);
        this.commandSet.add(command);

        for (String s : command.getAliases()) {
            ICommand icommand = this.commandMap.get(s);

            if (icommand == null || !icommand.getName().equals(s)) {
                this.commandMap.put(s, command);
            }
        }

        return command;
    }
}
