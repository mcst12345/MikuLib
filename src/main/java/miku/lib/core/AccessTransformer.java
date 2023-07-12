package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.matches("miku.(.*)"))return basicClass;
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for(MethodNode mn : cn.methods){
            if(mn.access != Opcodes.ACC_PUBLIC) {
                if(mn.access == Opcodes.ACC_PRIVATE)mn.access &=- Opcodes.ACC_PRIVATE;
                if(mn.access == Opcodes.ACC_PROTECTED)mn.access &=- Opcodes.ACC_PROTECTED;
                mn.access |= Opcodes.ACC_PUBLIC;
            }
        }

        for(FieldNode fn : cn.fields){
            if(fn.access != Opcodes.ACC_PUBLIC) {
                if(fn.access == Opcodes.ACC_PRIVATE)fn.access &=- Opcodes.ACC_PRIVATE;
                if(fn.access == Opcodes.ACC_PROTECTED)fn.access &=- Opcodes.ACC_PROTECTED;
                fn.access |= Opcodes.ACC_PUBLIC;
            }
        }

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
