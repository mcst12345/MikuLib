package miku.lib.util;


import miku.lib.core.MikuTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarFucker {
    protected static final IClassTransformer Miku = new MikuTransformer();
    protected static boolean shouldRestart = false;
    public synchronized static void FuckJar(JarFile jar) {
        //shouldRestart = true;
        boolean changed = false;
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
                        while ((str = br.readLine()) != null) {
                            if (!BadMANIFEST(str)) {
                                str = str + "\n";
                                jos.write(str.getBytes());
                            } else {
                                changed = true;
                            }
                        }
                        String fucked = "Fucked: true";
                        jos.write(fucked.getBytes());
                        br.close();
                        isr.close();
                    } else if (entry.getName().matches("(.*).class")) {
                        byte[] original = new byte[is.available()];
                        is.read(original);
                        boolean shouldAdd = true;
                        InputStream is2 = jar.getInputStream(entry);
                        ClassReader cr = new ClassReader(is2);
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, 0);
                        if (cn.interfaces != null) for (String s : cn.interfaces) {
                            if (s.equals("net/minecraftforge/fml/relauncher/IFMLLoadingPlugin") || s.equals("net/minecraft/launchwrapper/ITweaker")) {
                                changed = true;
                                shouldAdd = false;
                                break;
                            }
                        }
                        if (shouldAdd) {
                            byte[] fucked = Miku.transform(null, null, original);
                            if (fucked != original) changed = true;
                            jos.putNextEntry(new JarEntry(entry.getName()));
                            jos.write(fucked);
                        }
                    } else {
                        jos.putNextEntry(new JarEntry(entry.getName()));
                        jos.write(IOUtils.readNBytes(is, is.available()));
                    }
                }
            }
            jos.closeEntry();
            jos.close();

            if (changed) {
                shouldRestart = true;
            }

            OverwriteFile(new File(jar.getName() + ".fucked"), new File(jar.getName()), true);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized static void OverwriteFile(File source, File target, boolean backup) {
        try {
            if (backup) {
                System.gc();
                try (FileOutputStream BACKUP = new FileOutputStream(target.getPath() + ".backup")) {
                    BACKUP.write(Files.readAllBytes(target.toPath()));
                    BACKUP.flush();
                    FileDescriptor fd = BACKUP.getFD();
                    fd.sync();
                }
            }
            System.gc();
            try (FileOutputStream TARGET = new FileOutputStream(target)) {
                TARGET.write(Files.readAllBytes(source.toPath()));
                TARGET.flush();
                FileDescriptor fd = TARGET.getFD();
                fd.sync();
            }
            System.gc();
            if (!source.delete()) {
                System.out.println("Holy Shit? " + source.getName() + " cannot be deleted! Well,ignoring it.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("The Fuck?");
            Runtime.getRuntime().exit(0);
        }
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
