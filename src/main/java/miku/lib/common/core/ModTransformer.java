package miku.lib.common.core;

import miku.lib.common.util.ClassUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ModTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        boolean goodClass = false;
        if (!name.equals(transformedName)) {
            if (ClassUtil.isMinecraftClass(name)) {
                goodClass = true;
            }
        }
        goodClass = ClassUtil.isGoodClass(name) || goodClass || ClassUtil.isLibraryClass(name);
        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode in : mn.instructions.toArray()) {
                if (in instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) in;
                    if (fin.owner.equals("net/minecraft/client/Minecraft")) {
                        if (fin.name.equals("player") && fin.desc.equals("Lnet/minecraft/client/entity/EntityPlayerSP;")) {
                            if (fin.getOpcode() == Opcodes.GETFIELD) {
                                fin.name = "MikuPlayer";
                            } else {
                                if (goodClass) {
                                    fin.name = "MikuPlayer";
                                }
                            }
                        } else if (fin.name.equals("world") && fin.desc.equals("Lnet/minecraft/client/multiplayer/WorldClient;")) {
                            if (fin.getOpcode() == Opcodes.GETFIELD) {
                                fin.name = "MikuWorld";
                            } else {
                                if (goodClass) {
                                    fin.name = "MikuWorld";
                                }
                            }
                        } else if (fin.name.equals("profiler") && fin.desc.equals("Lnet/minecraft/profiler/Profiler;")) {
                            if (fin.getOpcode() == Opcodes.GETFIELD) {
                                fin.name = "MikuProfiler";
                            } else {
                                if (goodClass) {
                                    fin.name = "MikuProfiler";
                                }
                            }
                        }
                    } else if (fin.owner.equals("net/minecraftforge/common/MinecraftForge")) {
                        if (fin.name.equals("EVENT_BUS") && fin.desc.equals("Lnet/minecraftforge/fml/common/eventhandler/EventBus;")) {
                            if (fin.getOpcode() == Opcodes.GETSTATIC) {
                                fin.owner = "miku/lib/common/core/MikuLib";
                                fin.name = "MikuEventBus";
                            } else {
                                if (goodClass) {
                                    fin.owner = "miku/lib/common/core/MikuLib";
                                    fin.name = "MikuEventBus";
                                }
                            }
                        }
                    }
                }
            }
        }
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
