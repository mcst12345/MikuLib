package miku.lib.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class SpecialItem extends Item {
    protected static boolean TimeStop = false;
    public static boolean isTimeStop() {
        return TimeStop;
    }
    protected EntityPlayer owner = null;

    public SpecialItem(){

    }
}
