package miku.lib.mixins.minecraft;

import miku.lib.command.SQLOperation;
import miku.lib.sqlite.Sqlite;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Mixin(value = CommandHandler.class)
public class MixinCommandHandler {
    @Inject(at=@At("HEAD"),method = "tryExecute", cancellable = true)
    protected void tryExecute(ICommandSender sender, String[] args, ICommand command, String input, CallbackInfoReturnable<Boolean> cir) throws CommandException {
        if(command instanceof SQLOperation || input.matches("/sql (.*)")){
            String statement = input.substring(5);
            System.out.println(statement);
            try(ResultSet rs = Sqlite.ExecuteSQL(statement)) {
                if(rs!=null){
                    ResultSetMetaData md = rs.getMetaData();
                    int columnCount = md.getColumnCount();
                    for (int i = 1; i <= columnCount; i++){
                        if(sender instanceof EntityPlayerMP){
                            sender.sendMessage(new TextComponentString(md.getColumnName(i)+":"+rs.getObject(i)));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new CommandException(e.getMessage());
            }
            cir.setReturnValue(true);
        }
    }
}
