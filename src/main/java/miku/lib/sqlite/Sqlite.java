package miku.lib.sqlite;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

public class Sqlite {
    protected static final HashMap<String,Object> Configs = new HashMap<>();
    public static Connection c;
    public static Statement stmt;
    public static void Init(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            System.out.println("Connected to database.");
            try {
                String sql = "CREATE TABLE IF NOT EXISTS CONFIG " +
                        "(NAME TEXT PRIMARY KEY     NOT NULL," +
                        " VALUE        TEXT)";
                stmt.executeUpdate(sql);
                if(GetConfigValue("first_run",0)==null){
                    System.out.println("Init database.");
                    WriteConfigValue("debug_mode","false");
                    System.out.println("debug_mode:"+GetConfigValue("debug_mode",0));
                    WriteConfigValue("auto_range_kill","true");
                    System.out.println("auto_range_kill:"+GetConfigValue("auto_range_kill",0));

                    WriteConfigValue("first_run","false");
                } else {
                    System.out.println("first_run:"+GetConfigValue("first_run",0));
                }
            } catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object GetConfigValue(String NAME,int TYPE){// 0-bool 1-int 2-long 3-str
        if(Configs.get(NAME)!=null)return Configs.get(NAME);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM CONFIG;");
            String result = null;
            while(rs.next()){
                if(Objects.equals(rs.getString("NAME"), NAME)) {
                    result = rs.getString("VALUE");
                    Configs.put(NAME,result);
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
                    return Boolean.parseBoolean(result);
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

    public static void ClearConfigCache(){
        Configs.clear();
    }
}
