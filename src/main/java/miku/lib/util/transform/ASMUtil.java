package miku.lib.util.transform;

import miku.lib.core.CodeDecompiler;
import miku.lib.core.MikuTransformer;
import miku.lib.sqlite.Sqlite;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.annotation.Nonnull;
import java.util.List;

import static miku.lib.sqlite.Sqlite.DEBUG;

public class ASMUtil {
    //ShitMountain #4
    public static boolean isGoodClass(String clazz) {
        boolean result = clazz.matches("(.*)net.minecraft.(.*)") || clazz.matches("(.*)net.minecraftforge.(.*)") ||
                clazz.matches("(.*)miku.(.*)") || clazz.matches("(.*)paulscode.(.*)") || clazz.matches("(.*)org.objectweb.(.*)") ||
                clazz.matches("(.*)com.google.(.*)") || clazz.matches("(.*)java.(.*)") || clazz.matches("(.*)io.netty.(.*)") ||
                clazz.matches("(.*)org.apache.(.*)") || clazz.matches("(.*)com.mojang.(.*)") || clazz.matches("(.*)com.sun.(.*)") ||
                clazz.matches("(.*)org.lwjgl.(.*)") || clazz.matches("(.*)org.spongepowered.(.*)") || clazz.matches("(.*)scala.(.*)") ||
                clazz.matches("(.*)net.optifine(.*)") || clazz.matches("(.*)org.sqlite.") || clazz.matches("(.*)com.intellij.(.*)") ||
                clazz.matches("(.*)joptsimple.(.*)") || clazz.matches("(.*)org.jline(.*)") || clazz.matches("(.*)net.java.(.*)") ||
                clazz.matches("(.*)com.ibm.(.*)") || clazz.matches("(.*)it.unimi.dsi.(.*)") || clazz.matches("(.*)com.typesafe.(.*)") ||
                clazz.matches("(.*)com.jcraft.(.*)") || clazz.matches("(.*)com.github.(.*)") || clazz.matches("(.*)optifine.(.*)");

        for (String s : MikuTransformer.white_list) {
            if (clazz.matches(s)) {
                result = true;
                break;
            }
        }

        if (clazz.matches("(.*)mixin(.*)") && !clazz.matches("(.*)org.spongepowered.(.*)") && !clazz.matches("(.*).mixinbooter.(.*)")) {
            return =false;
        }

        if (result) {
            if (DEBUG() && (boolean) Sqlite.GetValueFromTable("class_info", "LOG_CONFIG", 0) && !MikuTransformer.shouldNotPrint(clazz))
                System.out.println("Ignore good class:" + clazz);
        } else {
            if (DEBUG() && (boolean) Sqlite.GetValueFromTable("class_info", "LOG_CONFIG", 0) && !MikuTransformer.shouldNotPrint(clazz)) {
                System.out.println("Examine class:" + clazz);
            }
        }

        return result;
    }

    //ShitMountain #2
    private static boolean BadInvoke(String str) {
        return str.matches("(.*)func_110143_aJ") || str.matches("(.*)func_70106_y") || str.equals("net/minecraft/entity/EntityLivingBase.func_70659_e") ||
                str.matches("(.*)func_70645_a") || str.matches("(.*)func_130011_c") || str.matches("(.*)func_70606_j") ||
                str.matches("(.*)func_70097_a") || str.equals("net/minecraft/entity/ai/attributes/IAttributeInstance.func_111128_a") || str.equals("net/minecraft/world/World.func_175681_c") ||
                str.equals("net/minecraft/world/World.func_72847_b") || str.equals("net/minecraft/world/chunk/Chunk.func_76622_b") || str.equals("net/minecraft/world/World.func_72960_a") ||
                str.matches("(.*)func_110142_aN") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_174925_a") ||
                str.equals("net/minecraft/util/CombatTracker.func_94547_a") || str.equals("net/minecraft/util/DamageSource.func_76359_i") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_70074_a") || str.equals("net/minecraft/entity/player/EntityPlayer.func_70103_a") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_71053_j") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_70436_m") || str.matches("(.*)func_70674_bp") ||
                str.equals("net/minecraft/network/NetHandlerPlayServer.func_194028_b") || str.matches("(.*)func_72900_e") || str.equals("net/minecraft/entity/Entity.func_82142_c") || str.matches("(.*)func_70665_d") ||
                str.matches("(.*)func_70103_a");
    }

    public static boolean isBadMethod(MethodNode method, String className) {
        boolean result = false;
        if (MikuTransformer.decompiler) {
            try {
                List<String> codes = CodeDecompiler.diagnose(className, method);
                int number = 0;
                for (String s : codes) {
                    if (BadInvoke(s)) number++;
                }
                if (number > 3) {
                    result = true;
                }

            } catch (AnalyzerException ignored) {
            }
        }
        String s = method.name.toLowerCase();
        if (s.matches("<(.*)init(.*)>")) {//Skip the constructor
            return false;
        }

        //ShitMountain #3
        result = s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)") ||
                s.matches("(.*)attack(.*)player(.*)") || s.matches("(.*)drop(.*)item(.*)") ||
                s.matches("(.*)clear(.*)inventory(.*)") || s.matches("(.*)remove(.*)entity(.*)") ||
                s.matches("(.*)entity(.*)remove(.*)") || result;


        if (DEBUG()) {
            MikuTransformer.print("Method name:" + method.name);
        }

        if (method.parameters != null) for (ParameterNode parameter : method.parameters) {
            if (DEBUG()) {
                MikuTransformer.print("parameter name:" + parameter.name);
            }
        }
        if (method.visibleTypeAnnotations != null)
            for (TypeAnnotationNode typeAnnotation : method.visibleTypeAnnotations) {
                if (DEBUG()) {
                    MikuTransformer.print("typeAnnotation desc:" + typeAnnotation.desc);
                    MikuTransformer.print("typeAnnotation typePath:" + typeAnnotation.typePath.toString());
                }
            }
        if (method.invisibleTypeAnnotations != null)
            for (TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                if (DEBUG()) {
                    MikuTransformer.print("invisibleTypeAnnotation desc:" + invisibleTypeAnnotation.desc);
                    MikuTransformer.print("invisibleTypeAnnotation typePath:" + invisibleTypeAnnotation.typePath.toString());
                }
            }

        if (method.attrs != null) {
            for (Attribute attr : method.attrs) {
                if (DEBUG()) {
                    MikuTransformer.print("attr type:" + attr.type);
                }
            }
        }

        if (method.localVariables != null) {
            for (LocalVariableNode localVariable : method.localVariables) {
                if (DEBUG()) {

                    MikuTransformer.print("localVariable name:" + localVariable.name);
                    if (localVariable.signature != null)
                        System.out.println("localVariable sign:" + localVariable.signature);
                    MikuTransformer.print("localVariable desc:" + localVariable.desc);
                    s = localVariable.desc;
                    if (isBadVariable(s)) {
                        System.out.println("Found bad variable:" + localVariable.name);
                        MikuTransformer.num++;
                        MikuTransformer.cached_methods.add(method);
                        break;
                    }
                }
            }
        }

        if (result) {
            System.out.println("Find bad method:" + method.name + ",fucking it.");
            return true;
        }

        return false;
    }

    public static boolean isBadField(FieldNode field) {
        boolean result = false;

        if (field.signature != null) {
            String s = field.signature.toLowerCase();
            if (DEBUG() && (boolean) Sqlite.GetValueFromTable("field_info", "LOG_CONFIG", 0)) {
                MikuTransformer.print("name:" + field.name);
                MikuTransformer.print("sign:" + field.signature);
                MikuTransformer.print("desc:" + field.desc);
            }
            result = (s.matches("(.*)/set(.*)entity(.*)") || s.matches("(.*)/list(.*)entity(.*)")) && !s.matches("(.*)net/minecraft/(.*)");
        }
        if (result) {
            System.out.println("Find bad field:" + field.name + ",fucking it.");
        }

        return result;
    }

    //ShitMountain #6
    public static boolean isBadVariable(String s) {
        return s.matches("(.*)LivingUpdateEvent(.*)") || s.matches("(.*)ServerTickEvent(.*)") ||
                s.matches("(.*)LivingHurtEvent(.*)") || s.matches("(.*)PlayerTickEvent(.*)") ||
                s.matches("(.*)WorldTickEvent(.*)") || s.matches("(.*)LivingDeathEvent(.*)") ||
                s.matches("(.*)LivingAttackEvent(.*)") || s.matches("(.*)GuiOpenEvent(.*)") ||
                s.matches("(.*)EntityJoinWorldEvent(.*)") || s.matches("(.*)AttackEntityEvent(.*)") ||
                s.matches("(.*)LivingSetAttackTargetEvent(.*)") || s.matches("(.*)PlayerInteractEvent(.*)");
    }

    public static void FuckClass(ClassNode cn) {
        cn.methods.removeIf(mn -> !mn.name.matches("<(.*)init(.*)>"));
    }

    //ShitMountain #7
    public static boolean isBadClass(@Nonnull String s) {
        s = s.toLowerCase();
        return s.matches("(.*)kill(.*)") || s.matches("(.*)attack(.*)entity(.*)") ||
                s.matches("(.*)attack(.*)player(.*)") || s.matches("(.*)drop(.*)item(.*)") ||
                s.matches("(.*)clear(.*)inventory(.*)") || s.matches("(.*)remove(.*)entity(.*)") ||
                s.matches("(.*)entity(.*)remove(.*)") || s.matches("(.*)entity(.*)util(.*)") ||
                s.matches("(.*)entity(.*)tool(.*)") || s.matches("(.*)entity(.*)helper(.*)");
    }
}
