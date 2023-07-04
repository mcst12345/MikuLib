package miku.lib.sqlite;

import java.sql.*;
public class Sqlite {
    public static Connection c;
    public static Statement stmt;
    public static void Init(){
        try {
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            try {
                String sql = "CREATE TABLE CONFIG " +
                        "(NAME TEXT PRIMARY KEY     NOT NULL," +
                        " TYPE           INT    NOT NULL, " + // 0-bool 1-int 2-long 3-str
                        " VALUE        TEXT)";
                stmt.executeUpdate(sql);
            } catch (Exception ignored){}

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
