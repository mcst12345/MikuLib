package miku.lib.common.util;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Misc {
    public static void print(String s) {
        if (s.contains(":null")) return;
        System.out.println(s);
    }

    public static boolean Client() {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.Minecraft", false, Launch.classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void returnMixin() {
        try {

            Class<?> klass = Proxy.class;

            Field field = klass.getDeclaredField("transformer");


            field.setAccessible(true);

            Object transformer = field.get(null);


            klass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

            field = klass.getDeclaredField("processor");


            field.setAccessible(true);

            Object processor = field.get(transformer);


            klass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");

            Method method = klass.getDeclaredMethod("select", MixinEnvironment.class);


            method.setAccessible(true);

            method.invoke(processor, MixinEnvironment.getCurrentEnvironment());

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException |
                 IllegalAccessException e) {

            // no-op

        }
    }

    public static Class<?> deduceMainApplicationClass() {
        Class<?> result = null;
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    result = Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return result;
    }

    public static List<Class<?>> deduceMainApplicationClasses() {
        List<Class<?>> result = new ArrayList<>();
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    result.add(Class.forName(stackTraceElement.getClassName()));
                }
            }
        } catch (ClassNotFoundException ex) {
            // Swallow and continue
        }
        return result;
    }
}
