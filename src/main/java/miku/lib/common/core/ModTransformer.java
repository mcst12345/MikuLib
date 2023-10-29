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

import java.util.ArrayList;
import java.util.List;

public class ModTransformer implements IClassTransformer {
    private final List<Integer> tmp = new ArrayList<>();

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
            tmp.clear();
            for (AbstractInsnNode in : mn.instructions.toArray()) {
                if (in instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) in;
                    boolean removePre = false;
                    switch (fin.owner) {
                        case "net/minecraft/client/Minecraft":
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
                            } else if (fin.name.equals("entityRenderer") && fin.desc.equals("Lnet/minecraft/client/renderer/EntityRenderer;")) {
                                if (fin.getOpcode() == Opcodes.GETFIELD) {
                                    fin.name = "MikuEntityRenderer";
                                } else {
                                    if (goodClass) {
                                        fin.name = "MikuEntityRenderer";
                                    }
                                }
                            }
                            break;
                        case "net/minecraftforge/common/MinecraftForge":
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
                            break;
                        case "net/minecraftforge/fml/common/FMLCommonHandler":
                            if (fin.name.equals("eventBus") && fin.desc.equals("Lnet/minecraftforge/fml/common/eventhandler/EventBus;")) {
                                if (fin.getOpcode() == Opcodes.GETFIELD) {
                                    fin.setOpcode(Opcodes.GETSTATIC);
                                    fin.owner = "miku/lib/common/core/MikuLib";
                                    fin.name = "MikuEventBus";
                                    removePre = true;
                                } else {
                                    if (goodClass) {
                                        fin.setOpcode(Opcodes.PUTSTATIC);
                                        fin.owner = "miku/lib/common/core/MikuLib";
                                        fin.name = "MikuEventBus";
                                        removePre = true;
                                    }
                                }
                            }
                            break;
                    }
                    if (removePre) {
                        tmp.add(mn.instructions.indexOf(fin) - 1);
                    }
                }
            }
            tmp.forEach(index -> mn.instructions.remove(mn.instructions.get(index)));
        }
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
