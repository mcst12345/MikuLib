package miku.lib.util;

public class ClassUtil {
    public static boolean find(String clazz){
        try {
            Class.forName(clazz,false, ClassLoader.getSystemClassLoader());
            return true;
        } catch (Exception ignored){
            return false;
        }
    }
}
