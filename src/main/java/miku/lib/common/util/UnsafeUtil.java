package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UnsafeUtil {
    public static void FuckMemory(Object o) {
        Launch.UNSAFE.freeMemory(o.hashCode());
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
