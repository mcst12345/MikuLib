package miku.lib.core;

import miku.lib.sqlite.Sqlite;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MikuAccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!isGoodClass(transformedName)){

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            cn.methods.removeIf(MikuAccessTransformer::isBadMethod);
            cn.fields.removeIf(MikuAccessTransformer::isBadField);

            ClassWriter cw = new ClassWriter(0);

            cn.accept(cw);

            return cw.toByteArray();
        }
        return basicClass;
    }

    private static boolean isBadMethod(MethodNode method){
        String s = method.name.toLowerCase();
        boolean result = s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)entity(.*)") || s.matches("(.*)attack(.*)player(.*)") || s.matches("(.*)drop(.*)item(.*)") || s.matches("(.*)clear(.*)inventory(.*)")
                || s.matches("(.*)remove(.*)entity(.*)") || s.matches("(.*)entity(.*)remove(.*)");
        if(result){
            System.out.println("Find bad method:"+method.name+",fucking it.");
            return true;
        }
        return false;
    }

    private static boolean isGoodClass(String clazz){
        boolean result = clazz.matches("net.minecraft.(.*)") || clazz.matches("net.minecraftforge.(.*)") || clazz.matches("miku.(.*)") || clazz.matches("paulscode.(.*)") || clazz.matches("org.objectweb.(.*)") || clazz.matches("com.google.(.*)")
                || clazz.matches("java.(.*)") || clazz.matches("io.netty.(.*)") || clazz.matches("org.apache.(.*)") || clazz.matches("com.mojang.(.*)") || clazz.matches("com.sun.(.*)") || clazz.matches("org.lwjgl.(.*)") || clazz.matches("org.spongepowered.(.*)") || clazz.matches("scala.(.*)");

        if(result){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0))System.out.println("Ignore good class:"+clazz);
        }

        return result;
    }

    private static boolean isBadField(FieldNode field){
        boolean result = false;

        if(field.signature!=null){
            String s = field.signature.toLowerCase();
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                System.out.println("name:"+field.name);
                System.out.println("sign:"+field.signature);
                System.out.println("desc:"+field.desc);
            }
            result = s.matches("(.*)/set(.*)entity") && !s.matches("(.*)net/minecraft/(.*)");
        }
        if(result){
            System.out.println("Find bad field:"+field.name+",fucking it.");
        }

        return result;
    }


}
