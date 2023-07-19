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

import static miku.lib.sqlite.Sqlite.DEBUG;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

public class MikuTransformer implements IClassTransformer {
    private static boolean decompiler = false;

    public MikuTransformer(){
        try {
            InputStream stream = MikuTweaker.class.getResourceAsStream("/decompiler");
            assert stream != null;
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
    protected static final String[] white_list = new String[]{"zone.rong.(.*)", "pl.asie.(.*)", "micdoodle8.(.*)",
            "noppes.(.*)", "mezz.(.*)", "com.brandon3055.(.*)", "codechicken.(.*)", "twilightforest.(.*)",
            "moze_intel.(.*),", "cofh.(.*)", "alexiy.(.*)", "vazkii.(.*)", "sweetmagic.(.*)", "stevekung.(.*)",
            "com.dhanantry.(.*)", "com.therandomlabs.(.*)", "thebetweenlands.(.*)", "java.com.pg85.otg.forge.(.*)",
            "com.gildedgames.(.*)", "com.lulan.shincolle.(.*)", "net.blay09.mods.waystones.(.*)", "com.xwm.(.*)",
            "snownee.(.*)", "mcp.mobius.waila.(.*)", "matteroverdrive.(.*)", "com.mrcrayfish.(.*)", "gregtech.(.*)",
            "xaero.(.*)", "com.sci.(.*)", "com.lilacmods.(.*)", "cn.academy.(.*)", "tragicneko.(.*)", "com.github.alexthe666.(.*)",
            "ic2.(.*)", "com.vicmatskiv.(.*)", "com.shinoow.(.*)", "morph.(.*)", "appeng.(.*)", "com.lucunji.(.*)", "software.bernie.(.*)",
            "com.enderio.(.*)", "forestry.(.*)", "com.pam.harvestcraft.(.*)", "slimeknights.(.*)", "com.rwtema.(.*)", "cn.mcmod.(.*)",
            "cn.mcmod_mmf.(.*)", "galaxyspace.(.*)", "com.gildedgames.(.*)", "blusunrize.(.*)", "com.mega.(.*)", "koala.(.*)", "net.tslat.(.*)",
            "com.ferreusveritas.(.*)", "harmonised.(.*)", "mod.acgaming.(.*)", "openeye.(.*)", "mods.flammpfeil.slashblade.(.*)"};
    public static final List<FieldNode> BadFields = new ArrayList<>();
    protected static final List<MethodNode> cached_methods = new ArrayList<>();
    protected static double possibility;
    protected static double num;

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

        if (result) {
            if (DEBUG() && (boolean) Sqlite.GetValueFromTable("class_info", "LOG_CONFIG", 0) && !shouldNotPrint(clazz))
                System.out.println("Ignore good class:" + clazz);
        } else {
            if (DEBUG() && (boolean) Sqlite.GetValueFromTable("class_info", "LOG_CONFIG", 0) && !shouldNotPrint(clazz)) {
                System.out.println("Examine class:" + clazz);
            }
        }

        return result;
    }

    private static boolean isBadInject(String s) {//Holy Shit.
        return s.contains("setHealth") || s.contains("damageEntity") || s.contains("getHealth") || s.contains("getMaxHealth") || s.contains("setDead") || s.contains("attackEntityFrom") ||
                s.contains("onDeath") || s.contains("replaceItemInInventory") || s.contains("dropItem") || s.contains("setGameType") || s.contains("track") || s.contains("getStackInSlot") ||
                s.contains("clear") || s.contains("clearMatchingItems") || s.contains("dropAllItems") || s.contains("disconnect") || s.contains("spawnEntity") || s.contains("onEntityAdded") ||
                s.contains("onEntityRemoved") || s.contains("removeEntity") || s.contains("removeEntityDangerously") || s.contains("getEntityByID") || s.contains("canAddEntity") ||
                s.contains("setEntityState") || s.contains("handleStatusUpdate") || s.contains("addPotionEffect") || s.contains("execute") || s.contains("tryExecute") ||
                s.contains("recreatePlayerEntity") || s.contains("readPlayerData") || s.contains("writePlayerData") || s.contains("shouldRender") || s.contains("bindEntityTexture") ||
                s.contains("doRender") || s.contains("displayGuiScreen") || s.contains("removeEntityFromWorld") || s.contains("post") || s.contains("setCount") || s.contains("getCount") ||
                s.contains("isItemStackDamageable") || s.contains("isItemDamaged") || s.contains("setItemDamage") || s.contains("getItemDamage") || s.contains("getMaxDamage") || s.contains("attemptDamageItem") ||
                s.contains("canDestroy") || s.contains("onLivingUpdate") || s.contains("onUpdate") || s.contains("writeEntityToNBT") || s.contains("readEntityFromNBT") || s.contains("canDespawn") ||
                s.contains("updateEntityActionState]") || s.contains("getItemStackFromSlot") || s.contains("setItemStackToSlot") || s.contains("setNoAI") || s.contains("addEventListener") ||
                s.contains("removeEventListener") || s.contains("updateEntities") || s.contains("tickPlayers") || s.contains("updateEntity") || s.contains("updateEntityWithOptionalForce") ||
                s.contains("tick") || s.contains("getLoadedEntityList") || s.contains("loadEntities") || s.contains("unloadEntities") || s.contains("func_174812_G") || s.contains("func_70106_y") ||
                s.contains("func_70099_a") || s.contains("func_70089_S") || s.contains("func_70609_aI") || s.contains("func_130011_c") || s.contains("func_110143_aJ") || s.contains("func_70606_j") ||
                s.contains("func_70645_a") || s.contains("func_70665_d") || s.contains("func_110138_aP") || s.contains("func_70103_a") || s.contains("func_70610_aX") || s.contains("func_70659_e") ||
                s.contains("func_70689_ay") || s.contains("func_70652_k") || s.contains("func_70071_h_") || s.contains("func_70014_b") || s.contains("func_70037_a") || s.contains("func_70636_d") ||
                s.contains("func_70692_ba") || s.contains("func_70626_be") || s.contains("func_184582_a") || s.contains("func_184201_a") || s.contains("func_174820_d") || s.contains("func_94061_f") ||
                s.contains("func_72838_d") || s.contains("func_72923_a") || s.contains("func_72847_b") || s.contains("func_72900_e") || s.contains("func_72973_f") || s.contains("func_72954_a") ||
                s.contains("func_72848_b") || s.contains("func_72939_s") || s.contains("func_184147_l") || s.contains("func_72870_g") || s.contains("func_72866_a") || s.contains("func_72835_b") ||
                s.contains("func_72910_y") || s.contains("func_175650_b") || s.contains("func_175681_c") || s.contains("func_177071_a") || s.contains("func_180548_c") || s.contains("func_72368_a") ||
                s.contains("func_77984_f") || s.contains("func_77964_b") || s.contains("func_92097_a") || s.contains("func_152604_a") || s.contains("func_72785_a") || s.contains("func_73028_b") ||
                s.contains("func_72786_a") || s.contains("func_72791_a") || s.contains("func_70301_a") || s.contains("func_174888_l") || s.contains("func_174925_a") || s.contains("func_70436_m") ||
                s.contains("func_194026_b") || s.contains("func_194028_b") || s.contains("func_186052_a") || s.contains("func_73045_a") || s.contains("func_184165_i") || s.contains("func_72960_a") ||
                s.contains("func_70690_d") || s.contains("func_184881_a") || s.contains("func_175786_a") || s.contains("func_70097_a") || s.contains("func_71019_a") || s.contains("func_71040_bB") ||
                s.contains("func_145779_a") || s.contains("func_75752_b") || s.contains("func_75753_a") || s.contains("func_178635_a") || s.contains("func_190074_a") || s.contains("getEntityTexture") ||
                s.contains("func_110775_a") || s.contains("func_76986_a") || s.contains("func_147108_a") || s.contains("func_190920_e") || s.contains("func_190916_E") || s.contains("func_77951_h") ||
                s.contains("func_77952_i") || s.contains("func_77958_k") || s.contains("func_96631_a") || s.contains("func_179544_c");
    }

    private static boolean isBadOverwrite(String s) {//Shit
        return s.equals("func_174812_G") || s.equals("func_70106_y") || s.equals("func_70097_a") || s.equals("func_71019_a") || s.equals("func_71040_bB") || s.equals("func_145779_a") ||
                s.equals("func_70099_a") || s.equals("func_70089_S") || s.equals("func_70609_aI") || s.equals("func_130011_c") || s.equals("func_110143_aJ") || s.equals("func_70606_j") ||
                s.equals("func_70645_a") || s.equals("func_70665_d") || s.equals("func_110138_aP") || s.equals("func_70103_a") || s.equals("func_70610_aX") || s.equals("func_70659_e") ||
                s.equals("func_70689_ay") || s.equals("func_70652_k") || s.equals("func_70071_h_") || s.equals("func_70014_b") || s.equals("func_70037_a") || s.equals("func_70636_d") ||
                s.equals("func_70692_ba") || s.equals("func_70626_be") || s.equals("func_184582_a") || s.equals("func_184201_a") || s.equals("func_174820_d") || s.equals("func_94061_f") ||
                s.equals("func_72838_d") || s.equals("func_72923_a") || s.equals("func_72847_b") || s.equals("func_72900_e") || s.equals("func_72973_f") || s.equals("func_72954_a") ||
                s.equals("func_72848_b") || s.equals("func_72939_s") || s.equals("func_184147_l") || s.equals("func_72870_g") || s.equals("func_72866_a") || s.equals("func_72835_b") ||
                s.equals("func_72910_y") || s.equals("func_175650_b") || s.equals("func_175681_c") || s.equals("func_177071_a") || s.equals("func_180548_c") || s.equals("func_72368_a") ||
                s.equals("func_77984_f") || s.equals("func_77964_b") || s.equals("func_92097_a") || s.equals("func_152604_a") || s.equals("func_72785_a") || s.equals("func_73028_b") ||
                s.equals("func_72786_a") || s.equals("func_70301_a") || s.equals("func_174888_l") || s.equals("func_174925_a") || s.equals("func_70436_m") || s.equals("func_194026_b") ||
                s.equals("func_194028_b") || s.equals("func_186052_a") || s.equals("func_73045_a") || s.equals("func_184165_i") || s.equals("func_72960_a") || s.equals("func_70690_d") ||
                s.equals("func_184881_a") || s.equals("func_175786_a") || s.equals("func_75752_b") || s.equals("func_75753_a") || s.equals("func_178635_a") || s.equals("func_190074_a") ||
                s.equals("func_110775_a") || s.equals("func_76986_a") || s.equals("func_147108_a") || s.equals("post") || s.equals("func_190920_e") || s.equals("func_190916_E") ||
                s.equals("func_77951_h") || s.equals("func_179544_c") || s.equals("func_77952_i") || s.equals("func_77958_k") || s.equals("func_96631_a");
    }

    private static void FuckMixinClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            boolean removed = false;
            if (mn.invisibleTypeAnnotations != null) {
                System.out.println("invisibleTypeAnnotations of " + mn.name);
                for (TypeAnnotationNode an : mn.invisibleTypeAnnotations) {
                    if (DEBUG()) {
                        print(an.desc);
                        if(an.values!=null)print(an.values.toString());
                    }
                    if(Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/injection/Inject;")){
                        if(an.values!=null){
                            String s = an.values.toString();
                            if(isBadInject(s)) {
                                cn.methods.remove(mn);
                                removed = true;
                                break;
                            }
                        }
                    }
                    if(Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/Overwrite")){
                        if(isBadOverwrite(mn.name)){
                            cn.methods.remove(mn);
                            removed = true;
                            break;
                        }
                    }
                }
            }
            if(removed)continue;
            if(mn.visibleAnnotations!=null) {
                System.out.println("invisibleTypeAnnotations of "+mn.name);
                for (AnnotationNode an : mn.visibleAnnotations) {
                    if(DEBUG()){
                        print(an.desc);
                        if(an.values!=null)print(an.values.toString());
                    }
                }
            }
            if(mn.invisibleAnnotations!=null) {
                System.out.println("invisibleAnnotations of "+mn.name);
                for (AnnotationNode an : mn.invisibleAnnotations) {
                    if(DEBUG()){
                        print(an.desc);
                        if(an.values!=null)print(an.values.toString());
                    }
                }
            }
        }
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
                str.equals("net/minecraft/network/NetHandlerPlayServer.func_194028_b") || str.matches("(.*)func_72900_e") || str.equals("net/minecraft/entity/Entity.func_82142_c") || str.matches("(.*)func_70665_d") ||
                str.matches("(.*)func_70103_a");
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
                print("Method name:" + method.name);
            }

            if (method.parameters != null) for (ParameterNode parameter : method.parameters) {
                if (DEBUG()) {
                    print("parameter name:" + parameter.name);
                }
            }
            if (method.visibleTypeAnnotations != null)
                for (TypeAnnotationNode typeAnnotation : method.visibleTypeAnnotations) {
                    if (DEBUG()) {
                        print("typeAnnotation desc:" + typeAnnotation.desc);
                        print("typeAnnotation typePath:" + typeAnnotation.typePath.toString());
                    }
                }
            if (method.invisibleTypeAnnotations != null)
                for (TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                    if (DEBUG()) {
                        print("invisibleTypeAnnotation desc:" + invisibleTypeAnnotation.desc);
                        print("invisibleTypeAnnotation typePath:" + invisibleTypeAnnotation.typePath.toString());
                    }
                }

            if (method.attrs != null) {
                for (Attribute attr : method.attrs) {
                    if (DEBUG()) {
                        print("attr type:" + attr.type);
                    }
                }
            }

            if (method.localVariables != null) {
                for (LocalVariableNode localVariable : method.localVariables) {
                    if (DEBUG()) {

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

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!isGoodClass(transformedName)){
            cached_methods.clear();
            num = 0.0d;

            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();


            cr.accept(cn, 0);

            if (DEBUG()) {
                print("Class name:" + cn.name);
                print("Class sign:" + cn.signature);
                print("outer class:" + cn.outerClass);
                print("outer method:" + cn.outerMethod);
                print("outer method desc:" + cn.outerMethodDesc);
                System.out.println("Interfaces:");
                for (String s : cn.interfaces) {
                    print(s);
                }
            }
            if (cn.visibleAnnotations != null) {
                System.out.println("visibleAnnotations:");
                for (AnnotationNode an : cn.visibleAnnotations) {
                    if (DEBUG()) {
                        print(an.desc);
                        if (an.values != null) print(an.values.toString());
                    }
                }
            }

            if (cn.visibleTypeAnnotations != null) for (TypeAnnotationNode an : cn.visibleTypeAnnotations) {
                {
                    System.out.println("visibleTypeAnnotations:");
                    if (DEBUG()) {
                        print(an.desc);
                        if (an.values != null) print(an.values.toString());
                    }
                }
            }

            if (cn.invisibleAnnotations != null) {
                System.out.println("invisibleAnnotations:");
                for (AnnotationNode an : cn.invisibleAnnotations) {
                    if (DEBUG()) {
                        print(an.desc);
                        if (an.values != null) print(an.values.toString());
                    }
                    if (Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/Mixin;")) {
                        System.out.println("Found mixin class:" + cn.name + ",fucking it.");
                        FuckMixinClass(cn);
                        ClassWriter cw = new ClassWriter(0);
                        cn.accept(cw);
                        return cw.toByteArray();
                    }
                }
            }

            if (cn.invisibleTypeAnnotations != null) {
                System.out.println("invisibleTypeAnnotations:");
                for (TypeAnnotationNode an : cn.invisibleTypeAnnotations) {
                    if (DEBUG()) {
                        print(an.desc);
                        print(an.values.toString());
                    }
                }
            }


            if (transformedName.toLowerCase().matches("(.*)transformer(.*)")) {
                for (MethodNode mn : cn.methods) {
                    if (Objects.equals(mn.name, "transform")) {
                        System.out.println("Find coremod that is not in whitelist. Destroying it.");
                        System.out.println("If this breaks innocent mods,report this on https://github.com/mcst12345/MikuLib/issues");
                        mn.visitCode();
                        Label label0 = new Label();
                        mn.visitLabel(label0);
                        mn.visitLineNumber(8, label0);
                        mn.visitVarInsn(ALOAD, 3);
                        mn.visitInsn(ARETURN);
                        mn.visitMaxs(1, 4);
                        mn.visitEnd();
                        break;
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


    //ShitMountain #5
    private static boolean shouldNotPrint(String s){
        return s.matches("net.minecraft.(.*)") || s.matches("net.optifine.(.*)") || s.matches("com.google.(.*)") || s.matches("com.sun.(.*)") || s.matches("java.(.*)") || s.matches("it.unimi.dsi.(.*)") ||
               s.matches("paulscode.(.*)") || s.matches("io.netty.(.*)") || s.matches("com.mojang.(.*)") || s.matches("miku.(.*)") || s.matches("joptsimple.(.*)");
    }

    private static boolean isBadField(FieldNode field){
        boolean result = false;

        if(field.signature!=null){
            String s = field.signature.toLowerCase();
            if(DEBUG() && (boolean) Sqlite.GetValueFromTable("field_info","LOG_CONFIG",0)){
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
