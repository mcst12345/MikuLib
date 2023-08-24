package miku.lib.mixins.minecraft;

import miku.lib.common.command.SQLOperation;
import miku.lib.common.core.MikuLib;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = CommandHandler.class)
public abstract class MixinCommandHandler {
    @Shadow
    private static String[] dropFirstString(String[] input) {
        return new String[0];
    }

    @Shadow
    @Final
    private Map<String, ICommand> commandMap;

    @Shadow
    protected abstract int getUsernameIndex(ICommand command, String[] args) throws CommandException;

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
     * @reason FUCK!!!
     */
    @Overwrite
    public int executeCommand(ICommandSender sender, String rawCommand) {
        rawCommand = rawCommand.trim();

        if (rawCommand.startsWith("/")) {
            rawCommand = rawCommand.substring(1);
        }

        String[] astring = rawCommand.split(" ");
        String s = astring[0];
        astring = dropFirstString(astring);
        ICommand icommand = this.commandMap.get(s);
        int i = 0;

        try {
            int j = this.getUsernameIndex(icommand, astring);

            if (icommand == null) {
                TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.notFound");
                textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(textcomponenttranslation1);
            } else if (icommand.checkPermission(this.getServer(), sender)) {
                net.minecraftforge.event.CommandEvent event = new net.minecraftforge.event.CommandEvent(icommand, sender, astring);
                if (MikuLib.MikuEventBus().post(event)) {
                    if (event.getException() != null) {
                        com.google.common.base.Throwables.throwIfUnchecked(event.getException());
                    }
                    return 1;
                }
                if (event.getParameters() != null) astring = event.getParameters();

                if (j > -1) {
                    List<Entity> list = EntitySelector.matchEntities(sender, astring[j], Entity.class);
                    String s1 = astring[j];
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

                    if (list.isEmpty()) {
                        throw new PlayerNotFoundException("commands.generic.selector.notFound", astring[j]);
                    }

                    for (Entity entity : list) {
                        astring[j] = entity.getCachedUniqueIdString();

                        if (this.tryExecute(sender, astring, icommand, rawCommand)) {
                            ++i;
                        }
                    }

                    astring[j] = s1;
                } else {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);

                    if (this.tryExecute(sender, astring, icommand, rawCommand)) {
                        ++i;
                    }
                }
            } else {
                TextComponentTranslation textcomponenttranslation2 = new TextComponentTranslation("commands.generic.permission");
                textcomponenttranslation2.getStyle().setColor(TextFormatting.RED);
                sender.sendMessage(textcomponenttranslation2);
            }
        } catch (CommandException commandexception) {
            TextComponentTranslation textcomponenttranslation = new TextComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
            textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(textcomponenttranslation);
        }

        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, i);
        return i;
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
