package miku.lib.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {
    public static boolean find(String clazz){
        try {
            Class.forName(clazz,false, ClassLoader.getSystemClassLoader());
            return true;
        } catch (Exception ignored){
            return false;
        }
    }


    //The following two methods are from https://www.jianshu.com/p/b84cc528fd44
    public static Map<String, Class<?>> loadAllJarFromAbsolute(String directoryPath) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            return loadJarFromAbsolute(directoryPath);
        }
        Map<String, Class<?>> clazzMap = new HashMap<>(16);
        File[] jars = directory.listFiles();
        if (jars != null && jars.length > 0) {
            List<String> jarPath = new LinkedList<>();
            for (File file : jars) {
                String fPath = file.getPath();
                if (fPath.endsWith(".jar")) {
                    jarPath.add(fPath);
                }
            }
            if (jarPath.size() > 0) {
                for (String path : jarPath) {
                    clazzMap.putAll(loadJarFromAbsolute(path));
                }
            }
        }
        return clazzMap;
    }

    public static Map<String, Class<?>> loadJarFromAbsolute(String path) throws IOException {
        JarFile jar = new JarFile(path);
        Enumeration<JarEntry> entryEnumeration = jar.entries();
        Map<String, Class<?>> clazzMap = new HashMap<>(16);
        while (entryEnumeration.hasMoreElements()) {
            JarEntry entry = entryEnumeration.nextElement();
            String clazzName = entry.getName();
            if (clazzName.endsWith(".class")) {
                clazzName = clazzName.substring(0, clazzName.length() - 6);
                clazzName = clazzName.replace("/", ".");
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    clazzMap.put(clazzName, clazz);
                } catch (Throwable ignored) {
                }
            }
        }
        return clazzMap;
    }

}
