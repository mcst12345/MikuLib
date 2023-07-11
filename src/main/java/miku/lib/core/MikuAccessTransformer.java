package miku.lib.core;

import com.google.common.collect.ImmutableList;
import miku.lib.sqlite.Sqlite;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class MikuAccessTransformer implements IClassTransformer {
    public static final List<FieldNode> BadFields = new ArrayList<>();
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!isGoodClass(transformedName)){

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            cn.methods.removeIf(MikuAccessTransformer::isBadMethod);
            for(FieldNode field : cn.fields){
                if(isBadField(field))BadFields.add(field);
            }

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
        if(method.parameters!=null)for(ParameterNode parameter : method.parameters){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                System.out.println("parameter name:"+parameter.name);
            }
        }
        if(method.visibleTypeAnnotations!=null)for(TypeAnnotationNode typeAnnotation : method.visibleTypeAnnotations){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                System.out.println("typeAnnotation desc:"+typeAnnotation.desc);
                System.out.println("typeAnnotation typePath:"+typeAnnotation.typePath.toString());
            }
        }
        if(method.invisibleTypeAnnotations!=null)for(TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                System.out.println("invisibleTypeAnnotation desc:"+invisibleTypeAnnotation.desc);
                System.out.println("invisibleTypeAnnotation typePath:"+invisibleTypeAnnotation.typePath.toString());
            }
        }

        if(method.attrs != null){
            for(Attribute attr : method.attrs){
                if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                    System.out.println("attr type:"+attr.type);
                }
            }
        }

        if(method.localVariables!=null){
            for(LocalVariableNode localVariable : method.localVariables){
                if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                    System.out.println("localVariable name:"+localVariable.name);
                    if(localVariable.signature!=null)System.out.println("localVariable sign:"+localVariable.signature);
                    System.out.println("localVariable desc:"+localVariable.desc);
                }
            }
        }

        return false;
    }

    private static boolean isGoodClass(String clazz){
        boolean result = clazz.matches("net.minecraft.(.*)") || clazz.matches("net.minecraftforge.(.*)") || clazz.matches("miku.(.*)") || clazz.matches("paulscode.(.*)") || clazz.matches("org.objectweb.(.*)") || clazz.matches("com.google.(.*)")
                || clazz.matches("java.(.*)") || clazz.matches("io.netty.(.*)") || clazz.matches("org.apache.(.*)") || clazz.matches("com.mojang.(.*)") || clazz.matches("com.sun.(.*)") || clazz.matches("org.lwjgl.(.*)") || clazz.matches("org.spongepowered.(.*)") || clazz.matches("scala.(.*)");

        if(result){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0) && (boolean) Sqlite.GetValueFromTable("class_info","LOG_CONFIG",0))System.out.println("Ignore good class:"+clazz);
        }

        return result;
    }

    private static boolean isBadField(FieldNode field){
        boolean result = false;

        if(field.signature!=null){
            String s = field.signature.toLowerCase();
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0) && (boolean) Sqlite.GetValueFromTable("field_info","LOG_CONFIG",0)){
                System.out.println("name:"+field.name);
                System.out.println("sign:"+field.signature);
                System.out.println("desc:"+field.desc);
            }
            result = (s.matches("(.*)/set(.*)entity(.*)") || s.matches("(.*)/list(.*)entity(.*)"))&& !s.matches("(.*)net/minecraft/(.*)");
        }
        if(result){
            System.out.println("Find bad field:"+field.name+",fucking it.");
        }

        return result;
    }


}
