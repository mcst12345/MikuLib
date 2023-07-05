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
                        " TYPE           INT    NOT NULL, " + // 0-bool 1-int 2-long 3-str
                        " VALUE        TEXT)";
                stmt.executeUpdate(sql);
            } catch (Exception ignored){}



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static Object GetConfigValue(String NAME,int TYPE){
        try {
            ResultSet rs = stmt.executeQuery("SELETE * FROM CONFIG;");
            switch (TYPE){
                case 0:
                    return Boolean.parseBoolean(rs.getString("value"));
                case 1:
                    return Integer.parseInt(rs.getString("value"));
                case 2:
                    return Long.parseLong(rs.getString("value"));
                case 3:
                    return rs.getString("value");
                default:
                    return null;
            }
        } catch (SQLException ignored) {
            return null;
        }
    }
}
