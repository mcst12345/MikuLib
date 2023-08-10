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
    public AccessTransformer() {
        System.out.println("AccessTransformer is running.");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;
        ClassReader cr;
        ClassNode cn;
        try {
            cr = new ClassReader(basicClass);
            cn = new ClassNode();
            cr.accept(cn, 0);
        } catch (Throwable t) {
            System.out.println("Error transforming class:" + transformedName);
            return basicClass;
        }

        for (FieldNode fn : cn.fields) {
            if (fn.name.equals("$VALUES")) continue;
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
        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) continue;
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
        }

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
