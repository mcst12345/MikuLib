package miku.lib.core;

import miku.lib.util.transform.MixinUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.Objects;

//The original one is just a piece of shit.So I'm trying to write a new one.

public class NewTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {


        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        if (cn.invisibleAnnotations != null) {
            System.out.println("invisibleAnnotations:");
            for (AnnotationNode an : cn.invisibleAnnotations) {
                if (Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/Mixin;")) {
                    System.out.println("Found mixin class:" + cn.name + ",fucking it.");
                    MixinUtil.FuckMixinClass(cn);
                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    return cw.toByteArray();
                }
            }
        }

        for (MethodNode mn : cn.methods) {
            ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode instruction = iterator.next();

            }
        }

        return basicClass;
    }
}
