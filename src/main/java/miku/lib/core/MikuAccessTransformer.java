package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class MikuAccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!isGoodClass(transformedName)){

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            cn.methods.removeIf(mn -> isBadMethod(mn.name));

            ClassWriter cw = new ClassWriter(0);

            cn.accept(cw);

            return cw.toByteArray();
        }
        return basicClass;
    }

    private static boolean isBadMethod(String method){
        String s = method.toLowerCase();
        boolean result = s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)entity(.*)") || s.matches("(.*)attack(.*)player(.*)");
        if(result){
            System.out.println("Find bad method:"+method+",fucking it.");
            return true;
        }
        return false;
    }

    private static boolean isGoodClass(String clazz){
        return clazz.matches("net.minecraft.(.*)") || clazz.matches("net.minecraftforge.(.*)") || clazz.matches("miku.(.*)") || clazz.matches("paulscode.(.*)");
    }



}
