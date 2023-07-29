package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeUtil {
    public static void FuckMemory(Object o) {
        for (Field field : o.getClass().getFields()) {
            long tmp = Modifier.isStatic(field.getModifiers()) ? Launch.UNSAFE.staticFieldOffset(field) : Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.allocateMemory(tmp);

        }
        for (Field field : o.getClass().getDeclaredFields()) {
            long tmp = Modifier.isStatic(field.getModifiers()) ? Launch.UNSAFE.staticFieldOffset(field) : Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.allocateMemory(tmp);
        }
    }
}
