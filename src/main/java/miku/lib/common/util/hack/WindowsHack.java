//POWER! UNLIMITED POWER!

package miku.lib.common.util.hack;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

public class WindowsHack extends ClassLoader implements ObjectFactory {
    private WindowsHack(ClassLoader cl) {
        super(cl);
    }

    public static Object Hack() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {

        byte[] hacker;

        try (InputStream is = WindowsHack.class.getResourceAsStream("/WindowsVirtualMachine")) {
            assert is != null;
            hacker = new byte[is.available()];
            is.read(hacker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Class<?> Hacker = new WindowsHack(getSystemClassLoader()).defineClass(hacker, 0, hacker.length);
        if (System.getProperty("os.arch").contains("x86"))
            Hacker.getField("pointerLength").set(null, 4);
        Method get = Hacker.getDeclaredMethod("get");
        return get.invoke(null);
    }


    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        return null;
    }
}
