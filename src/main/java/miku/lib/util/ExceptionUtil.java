package miku.lib.util;

public class ExceptionUtil {
    public static boolean isIgnored(Throwable e){
        return e instanceof NoSuchFieldException || e instanceof NoSuchMethodException || e instanceof NoSuchFieldError || e instanceof NoSuchMethodError || e instanceof NoClassDefFoundError || e instanceof ClassNotFoundException;
    }
}
