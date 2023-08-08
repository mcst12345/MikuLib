package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;

public class Misc {
    public static void print(String s) {
        if (s.contains(":null")) return;
        System.out.println(s);
    }

    public static boolean Client() {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.Minecraft", false, Launch.classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
