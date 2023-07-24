package miku.lib.util;


import miku.lib.MikuLib;
import miku.lib.core.MikuTransformer;
import miku.lib.util.transform.ASMUtil;
import miku.lib.util.transform.MixinUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static miku.lib.core.MikuTransformer.*;
import static miku.lib.sqlite.Sqlite.DEBUG;

public class JarFucker {
    protected static final IClassTransformer Miku = new MikuTransformer();
    protected static boolean shouldRestart = false;
    public synchronized static void FuckModJar(JarFile jar) {
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
                    } else if (entry.getName().matches("(.*).class") && !entry.getName().contains("$")) {
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
                            byte[] fucked = transform(cn.name.replace("/", "."), cn);
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
        return s.contains("FMLCorePlugin") || s.contains("FMLCorePluginContainsFMLMod") || s.contains("TweakClass") ||
                s.contains("ForceLoadAsMod") || s.contains("Name:") || s.contains("SHA-256-Digest:") || s.contains("Premain-Class") ||
                s.contains("Agent-Class:") || s.matches("(.*).class") || s.trim().length() <= 5;
    }

    public static boolean shouldRestart() {
        return shouldRestart;
    }

    protected static byte[] transform(String transformedName, ClassNode cn) {
        System.out.println("Examine class:" + transformedName);

        cached_methods.clear();
        double num = 0.0d;

        if (DEBUG()) {
            print("Class name:" + cn.name);
            print("Class sign:" + cn.signature);
            print("outer class:" + cn.outerClass);
            print("outer method:" + cn.outerMethod);
            print("outer method desc:" + cn.outerMethodDesc);
            System.out.println("Interfaces:");
            for (String s : cn.interfaces) {
                print(s);
            }
        }
        if (cn.visibleAnnotations != null) {
            System.out.println("visibleAnnotations:");
            for (AnnotationNode an : cn.visibleAnnotations) {
                if (DEBUG()) {
                    print(an.desc);
                    if (an.values != null) print(an.values.toString());
                }
            }
        }

        if (cn.visibleTypeAnnotations != null) for (TypeAnnotationNode an : cn.visibleTypeAnnotations) {
            {
                System.out.println("visibleTypeAnnotations:");
                if (DEBUG()) {
                    print(an.desc);
                    if (an.values != null) print(an.values.toString());
                }
            }
        }

        if (cn.invisibleAnnotations != null) {
            System.out.println("invisibleAnnotations:");
            for (AnnotationNode an : cn.invisibleAnnotations) {
                if (DEBUG()) {
                    print(an.desc);
                    if (an.values != null) print(an.values.toString());
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
                if (DEBUG()) {
                    print(an.desc);
                    print(an.values.toString());
                }
            }
        }

        double tmp = cn.methods.size();


        if (ASMUtil.isBadClass(transformedName)) {
            System.out.println("Find dangerous class " + cn.name + ",fucking it.");
            ASMUtil.FuckClass(cn);
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            return cw.toByteArray();
        }


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

    public synchronized static void FuckLaunchWrapper(File launch) {
        try {
            System.out.println(HashUtil.getHash(launch, "SHA-256"));
            InputStream MikuLaunch = MikuLib.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
            assert MikuLaunch != null;
            byte[] file = new byte[MikuLaunch.available()];
            MikuLaunch.read(file);
            MikuLaunch.close();
            FileOutputStream fuckedFile = new FileOutputStream(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.fucked");
            fuckedFile.write(file);
            fuckedFile.close();
            OverwriteFile(new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.fucked"), launch, true);
        } catch (IOException | NoSuchAlgorithmException ignored) {
        }
    }
}
