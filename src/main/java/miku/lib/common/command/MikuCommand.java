package miku.lib.common.command;

import miku.lib.common.util.Register;

public class MikuCommand {
    public static void Init(){
        Register.RegisterCommands(new MikuKill());
        Register.RegisterCommands(new SetHealth());
        Register.RegisterCommands(new ClearDBCache());
        Register.RegisterCommands(new SQLOperation());
    }
}
