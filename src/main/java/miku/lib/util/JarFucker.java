package miku.lib.util;


import net.minecraftforge.fml.common.FMLCommonHandler;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarFucker {
    protected static boolean shouldRestart = false;
    public static void FuckJar(JarFile jar) {
        shouldRestart = true;
        System.out.println("Hi," + jar.getName().replace("mods/", "") + ". Fuck you!");
        System.out.println("如果被干掉的不是一个秒杀mod,请于 https://github.com/mcst12345/MikuLib/issues 汇报");
        try {
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equals("META-INF/MUMFREY.RSA") || entry.getName().equals("META-INF/MUMFREY.SF") || entry.getName().equals("META-INF/SIGNFILE.DSA") || entry.getName().equals("META-INF/SIGNFILE.SF"))
                    continue;
                try (InputStream is = jar.getInputStream(entry)) {
                    if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String str;
                        List<String> tmp = new ArrayList<>();
                        while ((str = br.readLine()) != null) {
                            if (!BadMANIFEST(str)) {
                                tmp.add(str);
                                str = str + "\n";
                                jos.write(str.getBytes());
                            }
                        }
                        String fucked = "Fucked: true";
                        jos.write(fucked.getBytes());
                        br.close();
                        isr.close();
                    } else {
                        jos.putNextEntry(new JarEntry(entry.getName()));
                        jos.write(IOUtils.readNBytes(is, is.available()));
                    }
                }
            }
            jos.closeEntry();
            jos.close();


            if (new File(jar.getName()).renameTo(new File(jar.getName() + ".backup"))) {
                if (new File(jar.getName() + ".fucked").renameTo(new File(jar.getName()))) {
                    return;
                }
            }

            System.out.println("The Fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void OverwriteFile(File source, File target, boolean backup) {

    }

    protected static boolean BadMANIFEST(String s) {
        return s.contains("FMLCorePlugin") || s.contains("FMLCorePluginContainsFMLMod") || s.contains("TweakClass") ||
                s.contains("ForceLoadAsMod") || s.contains("Name:") || s.contains("SHA-256-Digest:") || s.contains("Premain-Class") ||
                s.contains("Agent-Class:") || s.matches("(.*).class") || s.trim().length() <= 5;
    }

    public static boolean shouldRestart() {
        return shouldRestart;
    }
}
