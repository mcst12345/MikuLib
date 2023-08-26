//POWER! UNLIMITED POWER!

package miku.lib.common.util.hack;

import net.minecraft.launchwrapper.Launch;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

public class WindowsHack extends ClassLoader implements ObjectFactory {
    private WindowsHack(ClassLoader cl) {
        super(cl);
    }

    public static int pointerLength = 8;

    public static Object Hack() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if (System.getProperty("os.arch").contains("x86")) pointerLength = 4;

        long JPLISAgent = Launch.UNSAFE.allocateMemory(4096L);
        byte[] buf = new byte[]{72, -125, -20, 40, 72, -125, -28, -16, 72, 49, -55, 101, 72, -117, 65, 96, 72, -117, 64, 24, 72, -117, 112, 32, 72, -83, 72, -106, 72, -83, 72, -117, 88, 32, 77, 49, -64, 68, -117, 67, 60, 76, -119, -62, 72, 1, -38, 68, -117, -126, -120, 0, 0, 0, 73, 1, -40, 72, 49, -10, 65, -117, 112, 32, 72, 1, -34, 72, 49, -55, 73, -71, 71, 101, 116, 80, 114, 111, 99, 65, 72, -1, -63, 72, 49, -64, -117, 4, -114, 72, 1, -40, 76, 57, 8, 117, -17, 72, 49, -10, 65, -117, 112, 36, 72, 1, -34, 102, -117, 12, 78, 72, 49, -10, 65, -117, 112, 28, 72, 1, -34, 72, 49, -46, -117, 20, -114, 72, 1, -38, 72, -119, -41, -71, 97, 114, 121, 65, 81, 72, -71, 76, 111, 97, 100, 76, 105, 98, 114, 81, 72, -119, -30, 72, -119, -39, 72, -125, -20, 48, -1, -41, 72, -125, -60, 48, 72, -125, -60, 16, 72, -119, -58, -71, 108, 108, 0, 0, 81, -71, 106, 118, 109, 0, 81, 72, -119, -31, 72, -125, -20, 48, -1, -42, 72, -125, -60, 48, 72, -125, -60, 16, 73, -119, -57, 72, 49, -55, 72, -71, 118, 97, 86, 77, 115, 0, 0, 0, 81, 72, -71, 114, 101, 97, 116, 101, 100, 74, 97, 81, 72, -71, 74, 78, 73, 95, 71, 101, 116, 67, 81, 72, -119, -30, 76, -119, -7, 72, -125, -20, 40, -1, -41, 72, -125, -60, 40, 72, -125, -60, 24, 73, -119, -57, 72, -125, -20, 40, 72, -119, -31, -70, 1, 0, 0, 0, 73, -119, -56, 73, -125, -64, 8, 72, -125, -20, 40, 65, -1, -41, 72, -125, -60, 40, 72, -117, 9, 72, -125, -20, 32, 84, 72, -119, -30, 77, 49, -64, 76, -117, 57, 77, -117, 127, 32, 73, -119, -50, 65, -1, -41, 76, -119, -15, 72, -70, 72, 71, 70, 69, 68, 67, 66, 65, 65, -72, 0, 2, 1, 48, 77, -117, 62, 77, -117, 127, 48, 72, -125, -20, 32, 65, -1, -41, 72, -125, -60, 32, 76, -119, -15, 77, -117, 62, 77, -117, 127, 40, 65, -1, -41, 72, -125, -60, 120, -61};
        byte[] stub = new byte[]{72, 71, 70, 69, 68, 67, 66, 65};
        if (pointerLength == 4) {
            buf = new byte[]{-112, -112, -112, 51, -55, 100, -95, 48, 0, 0, 0, -117, 64, 12, -117, 112, 20, -83, -106, -83, -117, 88, 16, -117, 83, 60, 3, -45, -117, 82, 120, 3, -45, 51, -55, -117, 114, 32, 3, -13, 65, -83, 3, -61, -127, 56, 71, 101, 116, 80, 117, -12, -127, 120, 4, 114, 111, 99, 65, 117, -21, -127, 120, 8, 100, 100, 114, 101, 117, -30, -117, 114, 36, 3, -13, 102, -117, 12, 78, 73, -117, 114, 28, 3, -13, -117, 20, -114, 3, -45, 82, 51, -55, 81, 104, 97, 114, 121, 65, 104, 76, 105, 98, 114, 104, 76, 111, 97, 100, 84, 83, -1, -46, -125, -60, 12, 89, 80, 102, -71, 51, 50, 81, 104, 106, 118, 109, 0, 84, -1, -48, -117, -40, -125, -60, 12, 90, 51, -55, 81, 106, 115, 104, 118, 97, 86, 77, 104, 101, 100, 74, 97, 104, 114, 101, 97, 116, 104, 71, 101, 116, 67, 104, 74, 78, 73, 95, 84, 83, -1, -46, -119, 69, -16, 84, 106, 1, 84, 89, -125, -63, 16, 81, 84, 89, 106, 1, 81, -1, -48, -117, -63, -125, -20, 48, 106, 0, 84, 89, -125, -63, 16, 81, -117, 0, 80, -117, 24, -117, 67, 16, -1, -48, -117, 67, 24, 104, 0, 2, 1, 48, 104, 68, 67, 66, 65, -125, -20, 4, -1, -48, -125, -20, 12, -117, 67, 20, -1, -48, -125, -60, 92, -61};
            stub = new byte[]{68, 67, 66, 65};
        }

        buf = replaceBytes(buf, stub, long2ByteArray_Little_Endian(JPLISAgent + (long) pointerLength, pointerLength));

        try {
            System.loadLibrary("attach");
            Class<?> vm = Class.forName("sun.tools.attach.WindowsVirtualMachine");

            Method enqueue = vm.getDeclaredMethod("enqueue", long.class, byte[].class, String.class, String.class, Object[].class);
            enqueue.invoke(null, -1, buf, "enqueue", "enqueue", null);
        } catch (Exception var13) {
            var13.printStackTrace();
            return null;
        }

        long native_jvmtienv = Launch.UNSAFE.getLong(JPLISAgent + (long) pointerLength);
        if (pointerLength == 4) {
            Launch.UNSAFE.putByte(native_jvmtienv + 201L, (byte) 2);
        } else {
            Launch.UNSAFE.putByte(native_jvmtienv + 361L, (byte) 2);
        }

        try {
            Class<?> instrument_clazz = Class.forName("sun.instrument.InstrumentationImpl");
            Constructor<?> constructor = instrument_clazz.getDeclaredConstructor(Long.TYPE, Boolean.TYPE, Boolean.TYPE);
            constructor.setAccessible(true);
            return constructor.newInstance(JPLISAgent, true, false);
        } catch (Throwable var12) {
            var12.printStackTrace();
            throw var12;
        }
    }

    public static byte[] long2ByteArray_Little_Endian(long l, int length) {

        byte[] array = new byte[length];

        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (l >> (i * 8));
        }
        return array;
    }


    private static byte[] replaceBytes(byte[] bytes, byte[] byteSource, byte[] byteTarget) {
        for (int i = 0; i < bytes.length; i++) {
            boolean bl = true;//从当前下标开始的字节是否与欲替换字节相等;
            for (int j = 0; j < byteSource.length; j++) {
                if (i + j < bytes.length && bytes[i + j] == byteSource[j]) {
                } else {
                    bl = false;
                }
            }
            if (bl) {
                System.arraycopy(byteTarget, 0, bytes, i, byteTarget.length);
            }
        }
        return bytes;
    }

    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        return null;
    }
}
