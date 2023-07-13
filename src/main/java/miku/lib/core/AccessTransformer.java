package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static java.lang.reflect.Modifier.*;

//Hi,forge. Fxxk you!

//R.I.P. Do not enable this.
//Or you'll get: java.lang.NullPointerException: Initializing game
//        at net.minecraftforge.registries.ObjectHolderRef.<init>(ObjectHolderRef.java:61)
//        at net.minecraftforge.registries.ObjectHolderRegistry.scanClassForFields(ObjectHolderRegistry.java:153)
//        at net.minecraftforge.registries.ObjectHolderRegistry.scanTarget(ObjectHolderRegistry.java:115)
//        at net.minecraftforge.registries.ObjectHolderRegistry.findObjectHolders(ObjectHolderRegistry.java:83)
//        at net.minecraftforge.fml.common.Loader.preinitializeMods(Loader.java:626)
//        at net.minecraftforge.fml.client.FMLClientHandler.beginMinecraftLoading(FMLClientHandler.java:252)
//        at net.minecraft.client.Minecraft.func_71384_a(Minecraft.java:467)
//        at net.minecraft.client.Minecraft.func_99999_d(Minecraft.java:378)
//        at net.minecraft.client.main.Main.main(SourceFile:123)
//        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
//        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
//        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//        at java.lang.reflect.Method.invoke(Method.java:498)
//        at net.minecraft.launchwrapper.Launch.launch(Launch.java:135)
//        at net.minecraft.launchwrapper.Launch.main(Launch.java:28)
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
