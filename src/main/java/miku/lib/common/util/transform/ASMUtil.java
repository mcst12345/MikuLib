package miku.lib.common.util.transform;

import miku.lib.common.core.InvokeDecompiler;
import miku.lib.common.core.MikuTransformer;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.JarFucker;
import miku.lib.common.util.Misc;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.annotation.Nonnull;
import java.util.List;

import static miku.lib.common.core.MikuTransformer.DEBUG;

public class ASMUtil {

    //ShitMountain #2
    private static boolean BadInvoke(String str) {
        return str.endsWith("func_110143_aJ") || str.endsWith("func_70106_y") || str.equals("net/minecraft/entity/EntityLivingBase.func_70659_e") ||
                str.endsWith("func_70645_a") || str.endsWith("func_130011_c") || str.endsWith("func_70606_j") ||
                str.endsWith("func_70097_a") || str.equals("net/minecraft/entity/ai/attributes/IAttributeInstance.func_111128_a") || str.equals("net/minecraft/world/World.func_175681_c") ||
                str.equals("net/minecraft/world/World.func_72847_b") || str.equals("net/minecraft/world/chunk/Chunk.func_76622_b") || str.equals("net/minecraft/world/World.func_72960_a") ||
                str.endsWith("func_110142_aN") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_174925_a") ||
                str.equals("net/minecraft/util/CombatTracker.func_94547_a") || str.equals("net/minecraft/util/DamageSource.func_76359_i") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_70074_a") || str.equals("net/minecraft/entity/player/EntityPlayer.func_70103_a") ||
                str.equals("net/minecraft/entity/player/EntityPlayer.func_71053_j") || str.equals("net/minecraft/entity/player/InventoryPlayer.func_70436_m") || str.endsWith("func_70674_bp") ||
                str.equals("net/minecraft/network/NetHandlerPlayServer.func_194028_b") || str.endsWith("func_72900_e") || str.equals("net/minecraft/entity/Entity.func_82142_c") || str.endsWith("func_70665_d") ||
                str.endsWith("func_70103_a");
    }

    public static boolean isBadMethod(MethodNode method, String className) {
        if (method == null) return false;
        boolean result = false;
        try {
            List<String> codes = InvokeDecompiler.diagnose(className, method);
            int number = 0;
            for (String s : codes) {
                if (BadInvoke(s)) number++;
            }
            if (number > 3) {
                result = true;
            }

        } catch (AnalyzerException ignored) {
        }
        if (method.name == null) return false;
        String s = method.name.toLowerCase();
        if (s.startsWith("<") && s.endsWith(">") && s.contains("init")) {//Skip the constructor
            return false;
        }

        //ShitMountain #3
        result = s.contains("kill") || s.contains("attack") || (s.contains("drop") && s.contains("item")) || s.contains("fuck") ||
                (s.contains("clear") && s.contains("inventory")) || (s.contains("remove") && s.contains("entity")) || result;


        if (Launch.sqliteLoaded) if (DEBUG) {
            Misc.print("Method name:" + method.name);
        }

        if (method.parameters != null) for (ParameterNode parameter : method.parameters) {
            if (Launch.sqliteLoaded) if (DEBUG) {
                Misc.print("parameter name:" + parameter.name);
            }
        }
        if (method.visibleTypeAnnotations != null)
            for (TypeAnnotationNode typeAnnotation : method.visibleTypeAnnotations) {
                if (Launch.sqliteLoaded) if (DEBUG) {
                    Misc.print("typeAnnotation desc:" + typeAnnotation.desc);
                    Misc.print("typeAnnotation typePath:" + typeAnnotation.typePath.toString());
                }
            }
        if (method.invisibleTypeAnnotations != null)
            for (TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                if (Launch.sqliteLoaded) if (DEBUG) {
                    Misc.print("invisibleTypeAnnotation desc:" + invisibleTypeAnnotation.desc);
                    Misc.print("invisibleTypeAnnotation typePath:" + invisibleTypeAnnotation.typePath.toString());
                }
            }

        if (method.attrs != null) {
            for (Attribute attr : method.attrs) {
                if (Launch.sqliteLoaded) if (DEBUG) {
                    Misc.print("attr type:" + attr.type);
                }
            }
        }

        if (method.localVariables != null) {
            for (LocalVariableNode localVariable : method.localVariables) {
                if (Launch.sqliteLoaded) if (DEBUG) {

                    Misc.print("localVariable name:" + localVariable.name);
                    if (localVariable.signature != null)
                        System.out.println("localVariable sign:" + localVariable.signature);
                    Misc.print("localVariable desc:" + localVariable.desc);
                    s = localVariable.desc;
                    if (isBadVariable(s)) {
                        System.out.println("Found bad variable:" + localVariable.name);
                        MikuTransformer.num++;
                        JarFucker.num++;
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
            if (Launch.sqliteLoaded) if (DEBUG && (boolean) Sqlite.GetValueFromTable("field_info", "LOG_CONFIG", 0)) {
                Misc.print("name:" + field.name);
                Misc.print("sign:" + field.signature);
                Misc.print("desc:" + field.desc);
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
        return s.contains("LivingUpdateEvent") || s.contains("ServerTickEvent") ||
                s.contains("LivingHurtEvent") || s.contains("PlayerTickEvent") ||
                s.contains("WorldTickEvent") || s.contains("LivingDeathEvent") ||
                s.contains("LivingAttackEvent") || s.contains("GuiOpenEvent") ||
                s.contains("EntityJoinWorldEvent") || s.contains("AttackEntityEvent") ||
                s.contains("LivingSetAttackTargetEvent") || s.contains("PlayerInteractEvent") ||
                s.contains("RenderGameOverlayEvent");
    }

    public static void FuckClass(ClassNode cn) {
        if (cn.methods != null) {
            cn.methods.removeIf(mn -> !(mn.name.startsWith("<") && mn.name.endsWith(">") && mn.name.contains("init")));
            //for (int i = 0; i < cn.methods.size(); i++) {
            //String name = cn.methods.get(i).name;
            //String desc = cn.methods.get(i).desc;
            //String sign = cn.methods.get(i).signature;
            //cn.methods.set(i, new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC, name, desc, sign, new String[0]));
            //}
            for (MethodNode mn : cn.methods) {
                mn.visitCode();
                Label label0 = new Label();
                mn.visitLabel(label0);
                mn.visitLineNumber(3, label0);
                mn.visitVarInsn(Opcodes.ALOAD, 0);
                mn.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mn.visitInsn(Opcodes.RETURN);
                mn.visitMaxs(1, 1);
                mn.visitEnd();
            }
        }
        if (cn.fields != null) cn.fields.clear();
        if (cn.interfaces != null) cn.interfaces.clear();
        if (cn.innerClasses != null) cn.innerClasses.clear();
        if (cn.visibleAnnotations != null) cn.visibleAnnotations.clear();
        if (cn.invisibleAnnotations != null) cn.invisibleAnnotations.clear();
        if (cn.invisibleTypeAnnotations != null) cn.invisibleTypeAnnotations.clear();
    }

    //ShitMountain #7
    public static boolean isBadClass(@Nonnull String s) {
        s = s.toLowerCase();
        boolean result = s.contains("kill") || (s.contains("attack") && s.contains("entity")) || s.contains("fuck") ||
                (s.contains("attack") && s.contains("player")) || (s.contains("drop") && s.contains("item")) ||
                (s.contains("clear") && s.contains("inventory")) || (s.contains("remove") && s.contains("entity")) ||
                (s.contains("entity") && s.contains("helper")) || s.contains("lwjgl") || s.contains("opengl") ||
                ((s.contains("asm") || s.contains("mixin") || s.contains("entity") || s.contains("core") || s.contains("tweak") || s.contains("transform") || s.contains("bad") || s.contains("event") || s.contains("fake")) && (s.contains("tool") || s.contains("helper") || s.contains("util")));
        if (result) System.out.println("Find bad class:" + s);
        return result;
    }

    public static boolean isBadInterface(String s) {
        System.out.println(s);
        boolean result = s.equals("net/minecraftforge/fml/relauncher/IFMLLoadingPlugin") || s.equals("net/minecraft/launchwrapper/IClassTransformer") ||
                s.equals("net/minecraft/launchwrapper/ITweaker");
        if (result) System.out.println("removing interface:" + s);
        return result;
    }
}
