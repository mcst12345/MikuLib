package miku.lib.common.sqlite;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//shitmountain.
//TODO:Rewrite this shit.

@SuppressWarnings("unchecked")
public class Sqlite {

    protected static boolean loaded = false;

    protected static Connection c;
    protected static Statement stmt;

    public static synchronized boolean isLoaded() {
        return loaded;
    }

    public static synchronized void CoreInit() {
        if (loaded) return;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            System.out.println("Connected to database.");
            try {
                System.out.println("Creating tables.");
                CreateTable("CONFIG", "NAME TEXT PRIMARY KEY     NOT NULL,VALUE TEXT");
                CreateTable("RENDER_CONFIG", "NAME TEXT PRIMARY KEY     NOT NULL,VALUE TEXT");
                CreateTable("HIDDEN_MODS", "ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("BANNED_MODS", "ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("BANNED_MOBS","ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("BANNED_ITEMS","ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("BANNED_CLASS","ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("LOG_CONFIG", "NAME TEXT PRIMARY KEY     NOT NULL,VALUE TEXT");
                CreateTable("PERFORMANCE", "NAME TEXT PRIMARY KEY     NOT NULL,VALUE TEXT");
                System.out.println("Init database.");
                CreateConfigValue("auto_range_kill", "CONFIG", "true");
                CreateConfigValue("debug", "CONFIG", "false");
                CreateConfigValue("void_keep_loaded", "CONFIG", "true");
                CreateConfigValue("miku_kill_exit_attack", "CONFIG", "true");
                CreateConfigValue("miku_kill_kick_attack", "CONFIG", "true");
                CreateConfigValue("class_info", "LOG_CONFIG", "true");
                CreateConfigValue("method_info", "LOG_CONFIG", "true");
                CreateConfigValue("field_info", "LOG_CONFIG", "true");
                CreateConfigValue("ignore_info", "LOG_CONFIG", "false");
                CreateConfigValue("render_info", "LOG_CONFIG", "false");
                CreateConfigValue("key_info", "LOG_CONFIG", "false");
                CreateConfigValue("entity_info", "LOG_CONFIG", "false");
                CreateConfigValue("time_point_mode", "CONFIG", "0");
                CreateConfigValue("rain_splash", "RENDER_CONFIG", "true");
                CreateConfigValue("rain", "RENDER_CONFIG", "true");
                CreateConfigValue("fast", "RENDER_CONFIG", "false");
                CreateConfigValue("particle", "RENDER_CONFIG", "true");
                CreateConfigValue("PERFORMANCE", "PERFORMANCE", "false");
                System.out.println("Reading lists.");
                GetStringsFromTable("HIDDEN_MODS", "ID", SqliteCaches.HIDDEN_MODS);
                GetStringsFromTable("BANNED_MODS", "ID", SqliteCaches.BANNED_MODS);
                GetStringsFromTable("BANNED_CLASS", "ID", SqliteCaches.BANNED_CLASS);

            } catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        loaded = true;
        Launch.sqliteLoaded = true;
    }

    public static void GetStringsFromTable(String TABLE, String KEY, ArrayList list){
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM "+TABLE+";");
            while(rs.next()){
                list.add(rs.getString(KEY));
                System.out.println("Add value:"+rs.getString(KEY));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void GetClassFromTable(String TABLE,String KEY, ArrayList list) {
        ResultSet rs;
        String s = null;
        try {
            rs = stmt.executeQuery("SELECT * FROM " + TABLE + ";");
            while (rs.next()) {
                s = rs.getString(KEY);
                list.add(Class.forName(s));
                System.out.println("Add class:" + rs.getString(KEY));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("MikuInfo:Class not found:" + s);
        }
    }

    /*
     * Get a value from MikuDatabase.
     * Type 0 refers to bool, 1 refers to int, 2 refers to long, 3 refers to string.
     */
    @Nullable
    public static Object GetValueFromTable(String NAME, String TABLE, int TYPE) {
        if (SqliteCaches.Configs.get(NAME) != null) return SqliteCaches.Configs.get(NAME);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + TABLE + ";");
            String result = null;
            while (rs.next()) {
                if (Objects.equals(rs.getString("NAME"), NAME)) {
                    result = rs.getString("VALUE");
                    switch (TYPE) {
                        case 0: {
                            SqliteCaches.Configs.put(NAME, result.equals("true"));
                            break;
                        }
                        case 1: {
                            SqliteCaches.Configs.put(NAME, Integer.parseInt(result));
                            break;
                        }
                        case 2: {
                            SqliteCaches.Configs.put(NAME, Long.parseLong(result));
                            break;
                        }
                        case 3: {
                            SqliteCaches.Configs.put(NAME, result);
                            break;
                        }
                        default: {
                            rs.close();
                            return null;
                        }
                    }
                    break;
                }
            }
            if (result == null) {
                rs.close();
                return null;
            } else {
                switch (TYPE) {
                    case 0: {
                        rs.close();
                        return result.equals("true");
                    }
                    case 1: {
                        rs.close();
                        return Integer.parseInt(result);
                    }
                    case 2: {
                        rs.close();
                        return Long.parseLong(result);
                    }
                    case 3: {
                        rs.close();
                        return result;
                    }
                    default: {
                        rs.close();

                    }
                }
            }
            return null;
        } catch (SQLException | NullPointerException e) {
            if (e instanceof SQLException && loaded) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return true;
        }
    }

    public static void CreateConfigValue(String NAME, String TABLE, String VALUE){
        String sql = "INSERT OR IGNORE INTO "+TABLE+" (NAME,VALUE)" +
                "VALUES ('"+NAME+"','"+VALUE+"')";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void CreateTable(String NAME,String VALUES){
        try {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS "+NAME+
                    " ("+VALUES+")");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static boolean IS_MOB_BANNED(@Nullable Entity entity) {
        return entity != null && SqliteCaches.BANNED_MOBS.contains(entity.getClass());
    }

    public static boolean IS_ITEM_BANNED(@Nullable Item item) {
        return item != null && SqliteCaches.BANNED_ITEMS.contains(item.getClass());
    }

    public static synchronized void Init() {
        GetClassFromTable("BANNED_MOBS", "ID", SqliteCaches.BANNED_MOBS);
        GetClassFromTable("BANNED_ITEMS", "ID", SqliteCaches.BANNED_ITEMS);

        if (DEBUG()) {
            for (Object o : SqliteCaches.BANNED_MOBS) {
                System.out.println("Mob " + o.toString() + " is banned.");
            }
            for (Object o : SqliteCaches.BANNED_ITEMS) {
                System.out.println("Item " + o.toString() + " is banned.");
            }
        }

        for (String s : SqliteCaches.BANNED_CLASS) {
            if (isClassLoaded(s)) {
                System.out.println("Class " + s + " is banned. Exiting.");
                Runtime.getRuntime().halt(39);
                System.exit(0);
            }
        }

        Class<Loader> loader = (Class<Loader>) Loader.instance().getClass();
        try {
            System.out.println("Init mod list.");
            Field field = loader.getDeclaredField("namedMods");
            field.setAccessible(true);
            System.out.println("Successfully set field 'namedMods' to accessible.");
            Map<String, ModContainer> mods;// = (Map<String, ModContainer>)field.get(Loader.instance());
            mods = (Map<String, ModContainer>) field.get(Loader.instance());
            System.out.println("Successfully get the object of namedMods.");
            Map<String, ModContainer> result = new HashMap<>();
            mods.forEach((key,value) -> {
                if (!SqliteCaches.HIDDEN_MODS.contains(key)) {
                    result.put(key, value);
                }
                if (SqliteCaches.BANNED_MODS.contains(key)) {
                    System.out.println("Mod " + key + " is banned. Exiting.");
                    Runtime.getRuntime().halt(39);
                    System.exit(0);
                }
            });

            field.set(Loader.instance(),result);
            System.out.println("Successfully overwrite field 'nameMods'.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected static boolean isClassLoaded(String clazz){
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e){
            return false;
        }
    }

    @Nullable
    public static synchronized ResultSet ExecuteSQL(String s) throws SQLException {
        if (hasReturn(s)) {
            return stmt.executeQuery(s);
        } else {
            stmt.executeUpdate(s);
            return null;
        }
    }

    public static boolean DEBUG() {
        return GetBooleanFromTable("debug", "CONFIG");
    }

    private static boolean hasReturn(String s) {
        return s.startsWith("SELECT ");
    }

    public static boolean GetBooleanFromTable(String name, String table) {
        try {
            return (boolean) GetValueFromTable(name, table, 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    public static int GetIntFromTable(String name, String table) {
        try {
            return (int) GetValueFromTable(name, table, 1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
    }
}
