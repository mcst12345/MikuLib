package miku.lib.common.sqlite;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;

public class SqliteCaches {
    protected static final HashMap<String, Object> Configs = new HashMap<>();
    protected static final ArrayList<String> HIDDEN_MODS = new ArrayList<>();
    protected static final ArrayList<Class<? extends Entity>> BANNED_MOBS = new ArrayList<>();
    protected static final ArrayList<Class<? extends Item>> BANNED_ITEMS = new ArrayList<>();
    protected static final ArrayList<String> BANNED_MODS = new ArrayList<>();
    protected static final ArrayList<String> BANNED_CLASS = new ArrayList<>();

    public static void ClearDBCache() {
        Configs.clear();
        BANNED_MOBS.clear();
        Sqlite.GetClassFromTable("BANNED_MOBS", "ID", BANNED_MOBS);
        BANNED_ITEMS.clear();
        Sqlite.GetClassFromTable("BANNED_ITEMS", "ID", BANNED_ITEMS);

    }
}
