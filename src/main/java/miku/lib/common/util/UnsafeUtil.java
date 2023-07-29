package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UnsafeUtil {
    public static void FuckMemory(Object o) {
        List<Field> tobeFucked = getAllFieldsList(o.getClass());

        for (Field field : tobeFucked) {
            System.out.println(field.getName());
            field.setAccessible(true);
            long tmp = Modifier.isStatic(field.getModifiers()) ? Launch.UNSAFE.staticFieldOffset(field) : Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.allocateMemory(tmp);
        }
    }

    public static void FuckMemory(Collection collection) {
        for (Object o : collection) {
            FuckMemory(o);
        }
    }


    //https://victorfengming.gitee.io/cp/java/for-props/
    protected static List<Field> getAllFieldsList(final Class<?> cls) {
        Validate.isTrue(cls != null, "The class must not be null");
        final List<Field> allFields = new ArrayList<Field>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(declaredFields));
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }
}
