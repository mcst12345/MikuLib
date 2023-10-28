package miku.lib.common.sqlite;

import miku.lib.common.util.timestop.TimeStopUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class SqliteCaches {
    static final HashMap<String, Object> Configs = new HashMap<>();
    static final ArrayList<String> HIDDEN_MODS = new ArrayList<>();
    static final ArrayList<Class<?>> BANNED_MOBS = new ArrayList<>();
    static final ArrayList<Class<?>> BANNED_ITEMS = new ArrayList<>();
    static final ArrayList<String> BANNED_MODS = new ArrayList<>();
    static final ArrayList<String> BANNED_CLASS = new ArrayList<>();

    public static void ClearDBCache() {
        Configs.clear();
        BANNED_MOBS.clear();
        Sqlite.GetClassFromTable("BANNED_MOBS", "ID", BANNED_MOBS);
        BANNED_ITEMS.clear();
        Sqlite.GetClassFromTable("BANNED_ITEMS", "ID", BANNED_ITEMS);
        TimeStopUtil.time_point_mode = Integer.MIN_VALUE;
    }
}
