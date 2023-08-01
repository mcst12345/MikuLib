package miku.lib.common.core;

import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.ClassUtil;
import miku.lib.common.util.transform.ASMUtil;
import miku.lib.common.util.transform.MixinUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static miku.lib.common.sqlite.Sqlite.DEBUG;

public class MikuTransformer implements IClassTransformer {

    public MikuTransformer(){
    }
    public static final List<MethodNode> cached_methods = new ArrayList<>();
    public static final List<FieldNode> BadFields = new ArrayList<>();
    protected static double possibility;
    public static double num;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!ClassUtil.isGoodClass(name) && !ClassUtil.isLibraryClass(name)) {
            if(!name.equals(transformedName)){
                if(ClassUtil.isMinecraftClass(name)){
                    System.out.println("Ignore class:" + transformedName);
                    return basicClass;
                }
            }

            System.out.println("Examine class:" + transformedName);

            cached_methods.clear();
            num = 0.0d;

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();


            cr.accept(cn, 0);

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
        if(Sqlite.DEBUG())System.out.println("Ignore class:" + transformedName);
        return basicClass;
    }


    public static void print(String s){
        if (s.contains(":null")) return;
        System.out.println(s);
    }
}
