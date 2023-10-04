package miku.lib.common.Native;

public class NativeUtil {
    public native static void Kill(Object o);

    public native static Class<?> GetClass(String clazz);

    public native static void MikuMapPut(String table, Object key, Object value);

    public native static Object MikuMapGet(String table, Object key);

    public native static boolean MikuMapContains(String table, Object key);

    public native static void MikuListAdd(String list, Object value);

    public native static void MikuListRemove(String list, Object value);

    public native static boolean MikuListContains(String list, Object value);

    public native static Object[] GetObjectsFromList(String list);
}
