package miku.lib.util;

import com.anotherstar.network.LoliKillEntityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class NetworkUtil {
    public static boolean isBadPacket(IMessage message){
        if (Loader.isModLoaded("lolipickaxe")) {
            if (message instanceof LoliKillEntityPacket) {
                Entity entity = Minecraft.getMinecraft().player.world.getEntityByID(((LoliKillEntityPacket) message).getEntityID());
                return EntityUtil.isProtected(entity);
            }
        }
        return false;
    }
}
