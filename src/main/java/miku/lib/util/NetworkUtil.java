package miku.lib.util;

import com.anotherstar.network.LoliKillEntityPacket;
import miku.lib.sqlite.Sqlite;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class NetworkUtil {
    public static boolean isBadPacket(IMessage message){
        if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0))return false;
        if (Loader.isModLoaded("lolipickaxe")) {
            if (message instanceof LoliKillEntityPacket) {
                Entity entity = Minecraft.getMinecraft().player.world.getEntityByID(((LoliKillEntityPacket) message).getEntityID());
                return EntityUtil.isProtected(entity);
            }
        }
        return false;
    }
}
