package miku.lib.common.util.transform;

import miku.lib.common.util.Misc;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.util.Objects;

import static miku.lib.common.sqlite.Sqlite.DEBUG;

public class MixinUtil {
    public static boolean isBadInject(String s) {//Holy Shit.
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

    public static boolean isBadOverwrite(String s) {//Shit
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

    public static void FuckMixinClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            boolean removed = false;
            if (mn.invisibleTypeAnnotations != null) {
                System.out.println("invisibleTypeAnnotations of " + mn.name);
                for (TypeAnnotationNode an : mn.invisibleTypeAnnotations) {
                    if (Launch.sqliteLoaded) if (DEBUG()) {
                        Misc.print(an.desc);
                        if (an.values != null) Misc.print(an.values.toString());
                    }
                    if (Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/injection/Inject;")) {
                        if (an.values != null) {
                            String s = an.values.toString();
                            if (isBadInject(s)) {
                                cn.methods.remove(mn);
                                removed = true;
                                break;
                            }
                        }
                    }
                    if (Objects.equals(an.desc, "Lorg/spongepowered/asm/mixin/Overwrite")) {
                        if (isBadOverwrite(mn.name)) {
                            cn.methods.remove(mn);
                            removed = true;
                            break;
                        }
                    }
                }
            }
            if (removed) continue;
            if (mn.visibleAnnotations != null) {
                System.out.println("invisibleTypeAnnotations of " + mn.name);
                for (AnnotationNode an : mn.visibleAnnotations) {
                    if (Launch.sqliteLoaded) if (DEBUG()) {
                        Misc.print(an.desc);
                        if (an.values != null) Misc.print(an.values.toString());
                    }
                }
            }
            if (mn.invisibleAnnotations != null) {
                System.out.println("invisibleAnnotations of " + mn.name);
                for (AnnotationNode an : mn.invisibleAnnotations) {
                    if (Launch.sqliteLoaded) if (DEBUG()) {
                        Misc.print(an.desc);
                        if (an.values != null) Misc.print(an.values.toString());
                    }
                }
            }
        }
    }
}
