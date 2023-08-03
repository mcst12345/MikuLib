package miku.lib.common.util;

public class Misc {
    public static void print(String s) {
        if (s.contains(":null")) return;
        System.out.println(s);
    }
}
