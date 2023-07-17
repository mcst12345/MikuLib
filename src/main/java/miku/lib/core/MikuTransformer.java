package miku.lib.core;

import miku.lib.sqlite.Sqlite;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class MikuTransformer implements IClassTransformer {
    private static boolean decompiler = false;

    public MikuTransformer(){
        try {
            InputStream stream = MikuTweaker.class.getResourceAsStream("/decompiler");
            byte[] dat = new byte[stream.available()];
            stream.read(dat);
            stream.close();
            char[] text = new char[dat.length];

            for (int i = 0; i < dat.length; ++i) {
                text[i] = (char) dat[i];
            }

            String s = String.copyValueOf(text);

            decompiler = !s.contains("false") && s.contains("true");

            if(decompiler)print("Decompiler is enabled.");
        } catch (IOException ignored) {
        }
    }


    //ShitMountain #1
    protected static final String[] white_list = new String[]{"zone.rong.(.*)","pl.asie.(.*)","micdoodle8.(.*)",
            "noppes.(.*)","mezz.(.*)","com.brandon3055.(.*)","codechicken.(.*)","twilightforest.(.*)",
            "moze_intel.(.*),","cofh.(.*)","alexiy.(.*)","vazkii.(.*)","sweetmagic.(.*)","stevekung.(.*)",
            "com.dhanantry.(.*)","com.therandomlabs.(.*)","thebetweenlands.(.*)","java.com.pg85.otg.forge.(.*)",
            "com.gildedgames.(.*)","com.lulan.shincolle.(.*)","net.blay09.mods.waystones.(.*)","com.xwm.(.*)",
            "snownee.(.*)","mcp.mobius.waila.(.*)","matteroverdrive.(.*)","com.mrcrayfish.(.*)","gregtech.(.*)",
            "xaero.(.*)","com.sci.(.*)","com.lilacmods.(.*)","cn.academy.(.*)","tragicneko.(.*)","com.github.alexthe666.(.*)",
            "ic2.(.*)","com.vicmatskiv.(.*)","com.shinoow.(.*)","morph.(.*)","appeng.(.*)","com.lucunji.(.*)","software.bernie.(.*)",
            "com.enderio.(.*)","forestry.(.*)","com.pam.harvestcraft.(.*)","slimeknights.(.*)","com.rwtema.(.*)","cn.mcmod.(.*)",
            "cn.mcmod_mmf.(.*)","galaxyspace.(.*)","com.gildedgames.(.*)","blusunrize.(.*)","com.mega.(.*)","koala.(.*)","net.tslat.(.*)",
            "com.ferreusveritas.(.*)","harmonised.(.*)","mod.acgaming.(.*)","openeye.(.*)"};
    public static final List<FieldNode> BadFields = new ArrayList<>();
    protected static final List<MethodNode> cached_methods = new ArrayList<>();
    protected static double possibility;
    protected static double num;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!isGoodClass(transformedName)){
            cached_methods.clear();
            num = 0.0d;

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();


            cr.accept(cn, 0);

            if(transformedName.toLowerCase().matches("(.*)transformer(.*)")){
                System.out.println("Find coremod that is not in whitelist. Fucking it.");
                System.out.println("If this breaks innocent mods,report this on https://github.com/mcst12345/MikuLib/issues");
                for(MethodNode mn : cn.methods){
                    if(Objects.equals(mn.name, "transform")){
                        mn.visitCode();
                        Label label0 = new Label();
                        mn.visitLabel(label0);
                        mn.visitLineNumber(8, label0);
                        mn.visitVarInsn(ALOAD, 3);
                        mn.visitInsn(ARETURN);
                        mn.visitMaxs(1,4);
                        mn.visitEnd();
                    }
                }
            }

            if(isBadClass(transformedName)){
                System.out.println("Find dangerous class "+cn.name+",fucking it.");
                FuckClass(cn);
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                return cw.toByteArray();
            }

            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0)){
                print("Class name:"+cn.name);
                print("Class sign:"+cn.signature);
                print("outer class:"+cn.outerClass);
                print("outer method:"+cn.outerMethod);
                print("outer method desc:"+cn.outerMethodDesc);
                System.out.println("Interfaces:");
                for(String s : cn.interfaces){
                    print(s);
                }
            }




            double tmp = cn.methods.size();
            cn.methods.removeIf(mn -> isBadMethod(mn, cn.name));


            possibility = num / tmp;
            System.out.println("The danger-value of class "+cn.name+":"+possibility);

            if(possibility > 0.6d){
                System.out.println(cn.name+"is too dangerous. Destroy it.");
                FuckClass(cn);
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
                if(isBadField(field))BadFields.add(field);
            }


            ClassWriter cw = new ClassWriter(0);

            cn.accept(cw);

            return cw.toByteArray();
        }
        return basicClass;
    }


    //ShitMountain #2
    private static boolean BadInvoke(String str){
        return str.matches("(.*)func_110143_aJ") || str.matches("(.*)func_70106_y") || str.equals("net/minecraft/entity/EntityLivingBase.func_70659_e") ||
                str.matches("(.*)func_70645_a") || str.matches("(.*)func_130011_c") || str.matches("(.*)func_70606_j") ||
                str.matches("(.*)func_70097_a") || str.equals("net/minecraft/entity/ai/attributes/IAttributeInstance.func_111128_a") || str.equals("net/minecraft/world/World.func_175681_c") ||
                str.equals("net/minecraft/world/World.func_72847_b") || str.equals("net/minecraft/world/chunk/Chunk.func_76622_b") || str.equals("net/minecraft/world/World.func_72960_a") ||
                str.matches("(.*)func_110142_aN") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_174925_a") ||
                str.equals("net/minecraft/util/CombatTracker.func_94547_a") || str.equals("net/minecraft/util/DamageSource.func_76359_i") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_70074_a") || str.equals("net/minecraft/entity/player/EntityPlayer.func_70103_a") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_71053_j") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_70436_m") || str.matches("(.*)func_70674_bp") ||
                str.equals("net/minecraft/network/NetHandlerPlayServer.func_194028_b");
    }

    private static boolean isBadMethod(MethodNode method,String className){
        boolean result = false;
        if(decompiler){
            try {
                List<String> codes = CodeDecompiler.diagnose(className,method);
                int number = 0;
                for(String s : codes){
                    if(BadInvoke(s))number++;
                }
                if(number>3) {
                    System.out.println("The decompiler finds a bad method:"+method.name+",fucking it.");
                    return true;
                }

            } catch (AnalyzerException ignored) {
            }
        }
            String s = method.name.toLowerCase();
            if (s.matches("<(.*)init(.*)>")) {
                return false;
            }

            //ShitMountain #3
            result = s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)entity(.*)") ||
                    s.matches("(.*)attack(.*)player(.*)") || s.matches("(.*)drop(.*)item(.*)") ||
                    s.matches("(.*)clear(.*)inventory(.*)") || s.matches("(.*)remove(.*)entity(.*)") ||
                    s.matches("(.*)entity(.*)remove(.*)") || result;

            if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {
                print("Method name:" + method.name);
            }

            if (method.parameters != null) for (ParameterNode parameter : method.parameters) {
                if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {
                    print("parameter name:" + parameter.name);
                }
            }
            if (method.visibleTypeAnnotations != null)
                for (TypeAnnotationNode typeAnnotation : method.visibleTypeAnnotations) {
                    if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {
                        print("typeAnnotation desc:" + typeAnnotation.desc);
                        print("typeAnnotation typePath:" + typeAnnotation.typePath.toString());
                    }
                }
            if (method.invisibleTypeAnnotations != null)
                for (TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                    if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {
                        print("invisibleTypeAnnotation desc:" + invisibleTypeAnnotation.desc);
                        print("invisibleTypeAnnotation typePath:" + invisibleTypeAnnotation.typePath.toString());
                    }
                }

            if (method.attrs != null) {
                for (Attribute attr : method.attrs) {
                    if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {
                        print("attr type:" + attr.type);
                    }
                }
            }

            if (method.localVariables != null) {
                for (LocalVariableNode localVariable : method.localVariables) {
                    if ((boolean) Sqlite.GetValueFromTable("debug", "CONFIG", 0)) {

                        print("localVariable name:" + localVariable.name);
                        if (localVariable.signature != null)
                            System.out.println("localVariable sign:" + localVariable.signature);
                        print("localVariable desc:" + localVariable.desc);
                        s = localVariable.desc;
                        if (isBadVariable(s)) {
                            System.out.println("Found bad variable:" + localVariable.name);
                            num++;
                            cached_methods.add(method);
                            break;
                        }
                    }
                }
            }

        if(result){
            System.out.println("Find bad method:"+method.name+",fucking it.");
            return true;
        }

        return false;
    }


    //ShitMountain #4
    private static boolean isGoodClass(String clazz){
        boolean result = clazz.matches("net.minecraft.(.*)") || clazz.matches("net.minecraftforge.(.*)") ||
                clazz.matches("miku.(.*)") || clazz.matches("paulscode.(.*)") || clazz.matches("org.objectweb.(.*)") ||
                clazz.matches("com.google.(.*)") || clazz.matches("java.(.*)") || clazz.matches("io.netty.(.*)") ||
                clazz.matches("org.apache.(.*)") || clazz.matches("com.mojang.(.*)") || clazz.matches("com.sun.(.*)") ||
                clazz.matches("org.lwjgl.(.*)") || clazz.matches("org.spongepowered.(.*)") || clazz.matches("scala.(.*)") ||
                clazz.matches("net.optifine(.*)") || clazz.matches("org.sqlite.") || clazz.matches("com.intellij.(.*)") ||
                clazz.matches("joptsimple.(.*)") || clazz.matches("org.jline(.*)") || clazz.matches("net.java.(.*)") ||
                clazz.matches("com.ibm.(.*)") || clazz.matches("it.unimi.dsi.(.*)") || clazz.matches("com.typesafe.(.*)") ||
                clazz.matches("com.jcraft.(.*)") || clazz.matches("com.github.(.*)");

        for(String s : white_list){
            if (clazz.matches(s)) {
                result = true;
                break;
            }
        }

        if(result){
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0) && (boolean) Sqlite.GetValueFromTable("class_info","LOG_CONFIG",0) && !shouldNotPrint(clazz))System.out.println("Ignore good class:"+clazz);
        }

        return result;
    }


    //ShitMountain #5
    private static boolean shouldNotPrint(String s){
        return s.matches("net.minecraft.(.*)") || s.matches("net.optifine.(.*)") || s.matches("com.google.(.*)") || s.matches("com.sun.(.*)") || s.matches("java.(.*)") || s.matches("it.unimi.dsi.(.*)") ||
               s.matches("paulscode.(.*)") || s.matches("io.netty.(.*)") || s.matches("com.mojang.(.*)") || s.matches("miku.(.*)") || s.matches("joptsimple.(.*)");
    }

    private static boolean isBadField(FieldNode field){
        boolean result = false;

        if(field.signature!=null){
            String s = field.signature.toLowerCase();
            if((boolean) Sqlite.GetValueFromTable("debug","CONFIG",0) && (boolean) Sqlite.GetValueFromTable("field_info","LOG_CONFIG",0)){
                print("name:"+field.name);
                print("sign:"+field.signature);
                print("desc:"+field.desc);
            }
            result = (s.matches("(.*)/set(.*)entity(.*)") || s.matches("(.*)/list(.*)entity(.*)"))&& !s.matches("(.*)net/minecraft/(.*)");
        }
        if(result){
            System.out.println("Find bad field:"+field.name+",fucking it.");
        }

        return result;
    }


    //ShitMountain #6
    private static boolean isBadVariable(String s){
        return s.matches("(.*)LivingUpdateEvent(.*)") || s.matches("(.*)ServerTickEvent(.*)") ||
                s.matches("(.*)LivingHurtEvent(.*)") || s.matches("(.*)PlayerTickEvent(.*)") ||
                s.matches("(.*)WorldTickEvent(.*)") || s.matches("(.*)LivingDeathEvent(.*)") ||
                s.matches("(.*)LivingAttackEvent(.*)") || s.matches("(.*)GuiOpenEvent(.*)") ||
                s.matches("(.*)EntityJoinWorldEvent(.*)") || s.matches("(.*)AttackEntityEvent(.*)") ||
                s.matches("(.*)LivingSetAttackTargetEvent(.*)") || s.matches("(.*)PlayerInteractEvent(.*)");
    }

    private static void FuckClass(ClassNode cn){
        cn.methods.removeIf(mn -> !mn.name.matches("<(.*)init(.*)>"));
    }


    //ShitMountain #7
    private static boolean isBadClass(@Nonnull String s){
        s = s.toLowerCase();
        return s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)entity(.*)") ||
                s.matches("(.*)attack(.*)player(.*)") || s.matches("(.*)drop(.*)item(.*)") ||
                s.matches("(.*)clear(.*)inventory(.*)") || s.matches("(.*)remove(.*)entity(.*)") ||
                s.matches("(.*)entity(.*)remove(.*)") || s.matches("(.*)entity(.*)util(.*)") ||
                s.matches("(.*)entity(.*)tool(.*)");
    }

    public static void print(String s){
        if(s.matches("(.*):null(.*)"))return;
        System.out.println(s);
    }
}
