package miku.lib.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class ItemUtil {
    public static boolean BadItem(Item item) {
        if (item == null) return true;
        String s = I18n.translateToLocal(item.getTranslationKey() + ".name").trim().toLowerCase();
        return s.contains("gameover") || s.contains("dead") || s.contains("死");
    }

    public static boolean BadItem(ItemStack item) {
        return item == null || BadItem(item.getItem());
    }
}
