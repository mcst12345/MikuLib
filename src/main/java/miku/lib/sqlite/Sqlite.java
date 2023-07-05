package miku.lib.sqlite;

import javax.annotation.Nullable;
import java.sql.*;
public class Sqlite {
    public static Connection c;
    public static Statement stmt;
    public static void Init(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            try {
                String sql = "CREATE TABLE IF NOT EXISTS CONFIG " +
                        "(NAME TEXT PRIMARY KEY     NOT NULL," +
                        " VALUE        TEXT)";
                stmt.executeUpdate(sql);
                c.commit();
                if(GetConfigValue("first_run",0)==null){
                    WriteConfigValue("debug_mode","false");
                    System.out.println("debug_mode:"+GetConfigValue("debug_mode",0));
                    WriteConfigValue("auto_range_kill","true");
                    System.out.println("auto_range_kill:"+GetConfigValue("auto_range_kill",0));

                }
            } catch (Exception ignored){}



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object GetConfigValue(String NAME,int TYPE){// 0-bool 1-int 2-long 3-str
        try {
            ResultSet rs = stmt.executeQuery("SELETE * FROM CONFIG;");
            switch (TYPE){
                case 0:
                    return Boolean.parseBoolean(rs.getString(NAME));
                case 1:
                    return Integer.parseInt(rs.getString(NAME));
                case 2:
                    return Long.parseLong(rs.getString(NAME));
                case 3:
                    return rs.getString(NAME);
                default:
                    return null;
            }
        } catch (SQLException ignored) {
            return null;
        }
    }

    public static void WriteConfigValue(String NAME,String VALUE){
        String sql = "INSERT INTO CONFIG (NAME,VALUE)" +
                    "VALUES ("+NAME+","+VALUE+")";
        try {
            stmt.executeUpdate(sql);
            c.commit();
        } catch (SQLException ignored) {
        }
    }
}
