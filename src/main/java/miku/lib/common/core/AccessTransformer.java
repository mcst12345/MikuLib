package miku.lib.common.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.reflect.Modifier.*;

public class AccessTransformer implements IClassTransformer {
    private static final boolean debug = System.getProperty("Miku_AT_debug") != null;
    public AccessTransformer() {
        System.out.println("AccessTransformer is running.");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        ClassReader cr;
        ClassNode cn;
        try {
            cr = new ClassReader(basicClass);
            cn = new ClassNode();
            cr.accept(cn, 0);
        } catch (Throwable t) {
            System.out.println("MikuWarn:AT Error transforming class:" + transformedName + ",ignoring it.");
            t.printStackTrace();
            return basicClass;
        }

        if (isInterface(cn.access)) {
            if (debug) System.out.println("MikuInfo:AT ignoring interface:" + transformedName);
            return basicClass;
        }


        if (debug) System.out.println("Processing class:" + transformedName);

        for (FieldNode fn : cn.fields) {
            if (fn.name.equals("$VALUES")) continue;
            int original = fn.access;

            if (isPrivate(fn.access)) {
                fn.access &= ~Opcodes.ACC_PRIVATE;
                fn.access |= Opcodes.ACC_PUBLIC;
            }
            if (isProtected(fn.access)) {
                fn.access &= ~Opcodes.ACC_PROTECTED;
                fn.access |= Opcodes.ACC_PUBLIC;
            }
            if (isFinal(fn.access)) {
                fn.access &= ~Opcodes.ACC_FINAL;
            }

            if (debug && fn.access != original) {
                System.out.println("Field:" + fn.name);
                System.out.println("Original access:" + original);
                System.out.println("Transformed access:" + fn.access);
            }
        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) continue;
            int original = mn.access;

            if (isPrivate(mn.access)) {
                mn.access &= ~Opcodes.ACC_PRIVATE;
                mn.access |= Opcodes.ACC_PUBLIC;
            }
            if (isProtected(mn.access)) {
                mn.access &= ~Opcodes.ACC_PROTECTED;
                mn.access |= Opcodes.ACC_PUBLIC;
            }
            if (isFinal(mn.access)) {
                mn.access &= ~Opcodes.ACC_FINAL;
            }

            if (debug && mn.access != original) {
                System.out.println("Method:" + mn.name);
                System.out.println("Original access:" + original);
                System.out.println("Transformed access:" + mn.access);
            }
        }

        if (!cn.interfaces.contains("java/io/Serializable")) {
            cn.interfaces.add("java/io/Serializable");
        }

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
