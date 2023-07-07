package miku.lib.sqlite;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class Sqlite {
    protected static final HashMap<String,Object> Configs = new HashMap<>();

    protected static final ArrayList<String> HIDDEN_MODS = new ArrayList<>();

    protected static final ArrayList<String> BANNED_MOBS = new ArrayList<>();

    public static Connection c;
    public static Statement stmt;
    public static void Init(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            System.out.println("Connected to database.");
            try {
                System.out.println("Creating tables.");
                CreateTable("CONFIG","NAME TEXT PRIMARY KEY     NOT NULL,VALUE TEXT");
                CreateTable("HIDDEN_MODS","ID TEXT PRIMARY KEY    NOT NULL");
                CreateTable("BANNED_MOBS","ID TEXT PRIMARY KEY    NOT NULL");
                if(GetValueFromTable("first_run","CONFIG",0)==null){
                    System.out.println("Init database.");
                    WriteConfigValue("debug_mode","false");
                    System.out.println("debug_mode:"+ GetValueFromTable("debug_mode","CONFIG",0));
                    WriteConfigValue("auto_range_kill","true");
                    System.out.println("auto_range_kill:"+ GetValueFromTable("auto_range_kill","CONFIG",0));

                    WriteConfigValue("first_run","false");
                }
                System.out.println("Reading lists.");
                GetValuesFromTable("HIDDEN_MODS","ID",HIDDEN_MODS);
                GetValuesFromTable("BANNED_MOBS","ID",BANNED_MOBS);

                System.out.println("Hidden mods:");
                for(String s : HIDDEN_MODS){
                    System.out.println(s);
                }

                System.out.println("Banned mobs");
                for(String s : BANNED_MOBS){
                    System.out.println(s);
                }

            } catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void GetValuesFromTable(String TABLE,String VALUE,ArrayList list){
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM "+TABLE+";");
            while(rs.next()){
                list.add(rs.getString(VALUE));
                System.out.println("Add value:"+rs.getString(VALUE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Nullable
    public static Object GetValueFromTable(String NAME,String TABLE, int TYPE){// 0-bool 1-int 2-long 3-str
        if(Configs.get(NAME)!=null)return Configs.get(NAME);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM "+TABLE+";");
            String result = null;
            while(rs.next()){
                if(Objects.equals(rs.getString("NAME"), NAME)) {
                    result = rs.getString("VALUE");
                    switch (TYPE){
                        case 0: {
                            Configs.put(NAME,result.equals("true"));
                            break;
                        }
                        case 1: {
                            Configs.put(NAME,Integer.parseInt(result));
                            break;
                        }
                        case 2: {
                            Configs.put(NAME,Long.parseLong(result));
                            break;
                        }
                        case 3: {
                            Configs.put(NAME,result);
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
            if(result == null) {
                rs.close();
                return null;
            }
            switch (TYPE){
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
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void WriteConfigValue(String NAME,String VALUE){
        String sql = "INSERT INTO CONFIG (NAME,VALUE)" +
                    "VALUES ('"+NAME+"','"+VALUE+"')";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void ClearDBCache(){
        Configs.clear();
        BANNED_MOBS.clear();
        GetValuesFromTable("BANNED_MOBS","ID",BANNED_MOBS);
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

    public static boolean IS_MOB_BANNED(Entity entity){
        return BANNED_MOBS.contains(entity.getClass().toString());
    }

    public static void InitModList(){
        Class<Loader> loader = (Class<Loader>) Loader.instance().getClass();
        try {
            System.out.println("Init mod list.");
            Field field = loader.getField("namedMods");
            field.setAccessible(true);
            System.out.println("Successfully set field 'namedMods' to accessible.");
            Map<String, ModContainer> mods;// = (Map<String, ModContainer>)field.get(Loader.instance());
            mods = (Map<String, ModContainer>) field.get(Loader.instance());
            System.out.println("Successfully get the object of namedMods.");
            Map<String, ModContainer> result = new HashMap<>();
            mods.forEach((key,value) -> {
                if(!HIDDEN_MODS.contains(key)){
                    result.put(key,value);
                }
            });

            field.set(Loader.instance(),result);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
