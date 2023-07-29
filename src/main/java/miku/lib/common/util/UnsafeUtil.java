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
    public static void Fuck(Object o) {
        List<Field> tobeFucked = getAllFieldsList(o.getClass());
        for (Field field : tobeFucked) {
            field.setAccessible(true);
            long tmp = Modifier.isStatic(field.getModifiers()) ? Launch.UNSAFE.staticFieldOffset(field) : Launch.UNSAFE.objectFieldOffset(field);
            Launch.UNSAFE.putObject(o, tmp, null);
        }
    }

    public static void Fuck(Collection collection) {
        for (Object o : collection) {
            Fuck(o);
        }
    }


    //https://victorfengming.gitee.io/cp/java/for-props/
    protected static List<Field> getAllFieldsList(final Class<?> cls) {
        Validate.isTrue(cls != null, "The class must not be null");
        final List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(declaredFields));
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }
}
