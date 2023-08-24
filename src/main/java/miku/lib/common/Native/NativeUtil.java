package miku.lib.common.Native;

public class NativeUtil {
    public native static void Kill(Object o);

    public native static int TEST();

    public native static Class<?> GetClass(String clazz);
}
