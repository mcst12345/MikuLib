package miku.lib.sqlite;

import java.sql.*;
public class Sqlite {
    public static Connection c;
    public static Statement stmt;
    public static void Init(){
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:miku.db");
            stmt = c.createStatement();
            String sql =  "CREATE TABLE CONFIG " +
                    "(NAME TEXT PRIMARY KEY     NOT NULL," +
                    " TYPE           INT    NOT NULL, " +
                    " VALUE        TEXT)";
            stmt.executeUpdate(sql);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
