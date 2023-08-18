package miku.lib.common.util;


import miku.lib.common.util.transform.ASMUtil;
import miku.lib.common.util.transform.MixinUtil;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static miku.lib.common.core.MikuTransformer.BadFields;
import static miku.lib.common.core.MikuTransformer.cached_methods;
import static miku.lib.common.sqlite.Sqlite.DEBUG;

public class JarFucker {
    public static double num;
    protected static boolean ShouldIgnore(String s) {
        return (s.startsWith("META-INF/") && (s.endsWith(".RSA") || s.endsWith(".SF") || s.endsWith(".DSA"))) ||
                s.endsWith(".exe") || s.endsWith(".dll") || s.endsWith(".so") || !s.contains(".") || s.endsWith("at.cfg") || s.endsWith(".bin") || s.endsWith(".zip") ||
                s.endsWith(".7z") || s.endsWith(".rar") || s.endsWith(".xz") || s.endsWith(".tar") || s.endsWith(".gz") || s.equals(".jar");
    }

    public synchronized static void FuckModJar(JarFile jar) {
        if (ClassUtil.DisablejarFucker) {
            System.out.println("JarFucker is disabled. Continue.");
            return;
        }
        System.out.println("Hi," + jar.getName().replace("mods/", "") + ". Fuck you!");
        System.out.println("如果被干掉的不是一个秒杀mod,请于 https://github.com/mcst12345/MikuLib/issues 汇报");
        try {
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                if (ShouldIgnore(entry.getName()))
                    continue;
                try (InputStream is = jar.getInputStream(entry)) {
                    if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String str;
                        boolean flag = false;
                        while ((str = br.readLine()) != null) {
                            if (!BadMANIFEST(str)) {
                                if (flag) {
                                    if (str.startsWith(" ")) {
                                        flag = false;
                                        continue;
                                    }
                                }
                                str = str + "\n";
                                jos.write(str.getBytes());
                            } else {
                                flag = true;
                            }
                        }
                        String fucked = "Fucked: true";
                        jos.write(fucked.getBytes());
                        br.close();
                        isr.close();
                    } else if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                        byte[] original = new byte[is.available()];
                        is.read(original);
                        boolean shouldAdd = true;
                        InputStream is2 = jar.getInputStream(entry);
                        ClassReader cr = new ClassReader(is2);
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, 0);
                        if (cn.interfaces != null) for (String s : cn.interfaces) {
                            if (s.equals("net/minecraftforge/fml/relauncher/IFMLLoadingPlugin") || s.equals("net/minecraft/launchwrapper/ITweaker")) {
                                shouldAdd = false;
                                break;
                            }
                        }
                        if (shouldAdd) {
                            byte[] fucked = transform(cn.name.replace("/", "."), cn);
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

            OverwriteFile(new File(jar.getName() + ".fucked"), new File(jar.getName()), true);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static void RemoveSignature(JarFile jar) {
        try {
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                if (isSignFile(entry.getName()))
                    continue;
                try (InputStream is = jar.getInputStream(entry)) {
                    if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String str;
                        boolean flag = false;
                        while ((str = br.readLine()) != null) {

                            if (!Remove(str)) {
                                if (flag) {
                                    if (str.startsWith(" ")) {
                                        flag = false;
                                        continue;
                                    }
                                }
                                str = str + "\n";
                                jos.write(str.getBytes());
                            } else {
                                flag = true;
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
        } catch (IOException ignored) {
        }

        OverwriteFile(new File(jar.getName() + ".fucked"), new File(jar.getName()), true);
    }

    public synchronized static void OverwriteFile(File source, File target, boolean backup) {
        source.setReadable(true);
        source.setWritable(true);
        target.setReadable(true);
        target.setWritable(true);
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
        return s.startsWith("FMLCorePlugin") || s.startsWith("FMLCorePluginContainsFMLMod") || s.startsWith("TweakClass") ||
                s.startsWith("ForceLoadAsMod") || s.startsWith("SHA-256-Digest:") || s.startsWith("Premain-Class") ||
                s.startsWith("Agent-Class:") || s.startsWith("Name:");
    }

    protected static boolean Remove(String s) {
        return s.startsWith("SHA") || s.startsWith("Name:") || s.contains("org.spongepowered.asm.launch.MixinTweaker");
    }

    protected static boolean isSignFile(String s) {
        return (s.startsWith("META-INF/") && s.endsWith(".RSA")) || (s.startsWith("META-INF/") && s.endsWith(".SF")) || (s.startsWith("META-INF/") && s.endsWith(".DSA"));
    }

    protected static byte[] transform(String transformedName, ClassNode cn) {
        System.out.println("Examine class:" + transformedName);

        cached_methods.clear();
        num = 0.0d;


        if (ASMUtil.isBadClass(transformedName)) {
            ASMUtil.FuckClass(cn);
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        }

        if (Launch.sqliteLoaded) if (DEBUG()) {
            Misc.print("Class name:" + cn.name);
            Misc.print("Class sign:" + cn.signature);
            Misc.print("outer class:" + cn.outerClass);
            Misc.print("outer method:" + cn.outerMethod);
            Misc.print("outer method desc:" + cn.outerMethodDesc);
            System.out.println("Interfaces:");
            for (String s : cn.interfaces) {
                Misc.print(s);
            }
        }
        if (cn.visibleAnnotations != null) {
            System.out.println("visibleAnnotations:");
            for (AnnotationNode an : cn.visibleAnnotations) {
                if (Launch.sqliteLoaded) if (DEBUG()) {
                    Misc.print(an.desc);
                    if (an.values != null) Misc.print(an.values.toString());
                }
            }
        }

        if (cn.visibleTypeAnnotations != null) for (TypeAnnotationNode an : cn.visibleTypeAnnotations) {
            {
                System.out.println("visibleTypeAnnotations:");
                if (Launch.sqliteLoaded) if (DEBUG()) {
                    Misc.print(an.desc);
                    if (an.values != null) Misc.print(an.values.toString());
                }
            }
        }

        if (cn.invisibleAnnotations != null) {
            System.out.println("invisibleAnnotations:");
            for (AnnotationNode an : cn.invisibleAnnotations) {
                if (Launch.sqliteLoaded) if (DEBUG()) {
                    Misc.print(an.desc);
                    if (an.values != null) Misc.print(an.values.toString());
                }
                if (Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/Mixin;")) {
                    System.out.println("Found mixin class:" + cn.name + ",fucking it.");
                    MixinUtil.FuckMixinClass(cn);
                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    return cw.toByteArray();
                }
            }
        }

        if (cn.invisibleTypeAnnotations != null) {
            System.out.println("invisibleTypeAnnotations:");
            for (TypeAnnotationNode an : cn.invisibleTypeAnnotations) {
                if (Launch.sqliteLoaded) if (DEBUG()) {
                    Misc.print(an.desc);
                    Misc.print(an.values.toString());
                }
            }
        }

        double tmp = cn.methods.size();


        cn.methods.removeIf(mn -> ASMUtil.isBadMethod(mn, cn.name));


        double possibility = num / tmp;
        System.out.println("The danger-value of class " + cn.name + ":" + possibility);

        if (possibility > 0.6d) {
            System.out.println(cn.name + "is too dangerous. Destroy it.");
            ASMUtil.FuckClass(cn);
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        } else if (possibility > 0.4d) {
            System.out.println(cn.name + "contains too many dangerous methods.Fucking those methods.");
            for (MethodNode m : cached_methods) {
                cn.methods.remove(m);
            }
        }

        for (FieldNode field : cn.fields) {
            if (ASMUtil.isBadField(field)) BadFields.add(field);
        }


        ClassWriter cw = new ClassWriter(0);

        cn.accept(cw);

        return cw.toByteArray();
    }
}
