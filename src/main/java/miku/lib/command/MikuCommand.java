package miku.lib.command;

import miku.lib.util.Register;

public class MikuCommand {
    public static void Init(){
        Register.RegisterCommands(new MikuKill());
        Register.RegisterCommands(new SetHealth());
        Register.RegisterCommands(new ClearDBCache());
    }
}
