package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.reflect.Modifier.*;

public class AccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(transformedName.matches("miku.(.*)"))return basicClass;
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for(MethodNode mn : cn.methods){
            if(!isPublic(mn.access)) {
                if(isPrivate(mn.access))mn.access &=~ PRIVATE;
                if(isProtected(mn.access))mn.access &=~ PROTECTED;
                mn.access |= PUBLIC;
            }
        }

        for(FieldNode fn : cn.fields){
            if(!isPublic(fn.access)) {
                if(isPrivate(fn.access))fn.access &=~ PRIVATE;
                if(isProtected(fn.access))fn.access &=~ PROTECTED;
                fn.access |= PUBLIC;
            }
        }

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
