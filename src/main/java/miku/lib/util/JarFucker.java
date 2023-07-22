package miku.lib.util;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFucker {
    public static void FuckJar(JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().equals("META-INF/MANIFEST.MF")) {
                System.out.println("Find MANIFEST.MF of " + jar.getName() + ",fucking it.");
            }
        }
    }
}
