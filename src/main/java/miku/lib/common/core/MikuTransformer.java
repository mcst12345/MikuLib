package miku.lib.common.core;

import miku.lib.common.exception.NoYouCannotBeLoaded;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.Misc;
import miku.lib.common.util.transform.ASMUtil;
import miku.lib.common.util.transform.MixinUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MikuTransformer implements IClassTransformer {

    public MikuTransformer() {
    }

    public static final List<MethodNode> cached_methods = new ArrayList<>();
    public static final List<FieldNode> BadFields = new ArrayList<>();
    protected static double possibility;
    public static double num;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!ClassUtil.Loaded() || basicClass == null) return basicClass;
        if (name.equals("javax.annotation.Resource") || name.equals("javax.annotation.Nullable") || name.equals("org.apache.log4j.Logger") || name.equals("optifine.OptiFineForgeTweaker"))
            return basicClass;
        if (!name.equals(transformedName)) {
            if (ClassUtil.isMinecraftClass(name)) {
                return basicClass;
            }
        }
        final boolean goodClass = ClassUtil.isGoodClass(name);
        final boolean libraryClass = ClassUtil.isLibraryClass(name);
        if (!goodClass && !libraryClass) {
            System.out.println("Examine class:" + transformedName);

            cached_methods.clear();
            num = 0.0d;

            ClassReader cr;
            ClassNode cn;
            try {
                cr = new ClassReader(basicClass);
                cn = new ClassNode();
                cr.accept(cn, 0);
            } catch (Throwable t) {
                return basicClass;
            }

            if (ASMUtil.isBadClass(transformedName) || ASMUtil.isBadSuperClass(cr.getSuperName())) {
                ASMUtil.FuckClass(cn);
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                return cw.toByteArray();
            }


            for (String s : cn.interfaces) {
                if (ASMUtil.isBadInterface(s)) {
                    ASMUtil.FuckClass(cn);
                    throw new NoYouCannotBeLoaded();
                }
            }

            if (cn.visibleAnnotations != null) {
                System.out.println("visibleAnnotations:");
                for (AnnotationNode an : cn.visibleAnnotations) {
                    if (Sqlite.DEBUG()) {
                        Misc.print(an.desc);
                        if (an.values != null) Misc.print(an.values.toString());
                    }
                }
            }

            if (cn.visibleTypeAnnotations != null) for (TypeAnnotationNode an : cn.visibleTypeAnnotations) {
                {
                    System.out.println("visibleTypeAnnotations:");
                    if (Sqlite.DEBUG()) {
                        Misc.print(an.desc);
                        if (an.values != null) Misc.print(an.values.toString());
                    }
                }
            }

            if (cn.invisibleAnnotations != null) {
                System.out.println("invisibleAnnotations:");
                for (AnnotationNode an : cn.invisibleAnnotations) {
                    if (Sqlite.DEBUG()) {
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
                    if (Sqlite.DEBUG()) {
                        Misc.print(an.desc);
                        Misc.print(an.values.toString());
                    }
                }
            }

            double tmp = cn.methods.size();


            cn.methods.removeIf(mn -> ASMUtil.isBadMethod(mn, cn.name));


            possibility = num / tmp;
            System.out.println("The danger-value of class " + cn.name + ":" + possibility);

            if (possibility > 0.6d) {
                System.out.println(cn.name + "is too dangerous. Destroy it.");
                ASMUtil.FuckClass(cn);
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                return cw.toByteArray();
            }

            else if(possibility > 0.4d){
                System.out.println(cn.name+"contains too many dangerous methods.Fucking those methods.");
                for(MethodNode m : cached_methods) {
                    cn.methods.remove(m);
                }
            }

            for(FieldNode field : cn.fields){
                if (ASMUtil.isBadField(field)) BadFields.add(field);
            }


            ClassWriter cw = new ClassWriter(0);

            cn.accept(cw);

            return cw.toByteArray();
        }
        if (Sqlite.DEBUG()) System.out.println("Ignoring class:" + transformedName);
        return basicClass;
    }


}
