package net.mcreator.newmod.launchPluginService;

//By 3.14159265358979
// https://center.mcmod.cn/137427/

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BadLPS implements ILaunchPluginService {
    private final ILaunchPluginService oldLPS;
    public BadLPS(ILaunchPluginService service){
        this.oldLPS=service;
    }
    @Override
    public String name() {
        return oldLPS.name();
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return oldLPS.handlesClass(classType,isEmpty);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public int processClassWithFlags(Phase phase, ClassNode classNode, Type classType, String reason) {
        ClassNode oldClassNode=new ClassNode();
        classNode.accept(oldClassNode);
        int flags= oldLPS.processClassWithFlags(phase, classNode, classType, reason);
        MethodNode oldMethod,newMethod;
        if (flags!=ComputeFlags.NO_REWRITE) {
            for (int i = 0; i < oldClassNode.methods.size(); i++) {
                oldMethod = oldClassNode.methods.get(i);
                int j = classNode.methods.indexOf(new CMP(oldMethod));
                if (j >= 0) {

                    newMethod = classNode.methods.get(j);
                    if (newMethod.instructions.size() != oldMethod.instructions.size()) {
                        System.out.println("Size different method:" + classNode.name + "." + oldMethod.name);
                        classNode.methods.set(j, oldMethod);
                    }else{
                        for (int k = 0; k < newMethod.instructions.size(); k++) {
                            if (!isEquals(newMethod.instructions.get(k),oldMethod.instructions.get(k))) {
                                System.out.println("InsnNode different method:" + classNode.name + "." + oldMethod.name);
                                System.out.println("OldNode:"+oldMethod.instructions.get(k).getClass().getName()+",NewNode:"+newMethod.instructions.get(k).getClass().getName());
                                classNode.methods.set(j, oldMethod);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return flags;
    }

    private static boolean isEquals(AbstractInsnNode a,AbstractInsnNode b){
        if (a==b){
            return true;
        }
        if (a==null||b==null){
            return false;
        }
        if (!a.getClass().getName().equals(b.getClass().getName())){
            return false;
        }
        if (a.getOpcode()!=b.getOpcode()||a.getType()!=b.getType()){
            return false;
        }
        if (a.equals(b)){
            return true;
        }
        if (a instanceof LabelNode){
            return true;
        }else if (a instanceof LineNumberNode){
            return true;
        }else if (a instanceof MethodInsnNode){
            MethodInsnNode calla=(MethodInsnNode) a,callb=(MethodInsnNode) b;
            return calla.name.equals(callb.name)&&calla.owner.equals(callb.owner)&&calla.itf==callb.itf&&calla.desc.equals(callb.desc);
        }else if (a instanceof FieldInsnNode){
            FieldInsnNode calla=(FieldInsnNode) a,callb=(FieldInsnNode) b;
            return calla.owner.equals(callb.owner)&&calla.desc.equals(callb.desc)&&calla.name.equals(callb.name);
        }else if (a instanceof IincInsnNode){
            IincInsnNode calla=(IincInsnNode) a,callb=(IincInsnNode) b;
            return calla.incr== callb.incr&&calla.var==callb.var;
        }else if (a instanceof InsnNode){
            return true;
        }else if (a instanceof FrameNode){
            FrameNode calla=(FrameNode) a,callb=(FrameNode) b;
            return calla.type==callb.type&&safeCMP(calla.stack,callb.stack)&&safeCMP(calla.local,callb.local);
        }else if (a instanceof IntInsnNode){
            IntInsnNode calla=(IntInsnNode) a,callb=(IntInsnNode) b;
            return calla.operand==callb.operand;
        }else if (a instanceof InvokeDynamicInsnNode){
            InvokeDynamicInsnNode calla=(InvokeDynamicInsnNode) a,callb=(InvokeDynamicInsnNode) b;
            return calla.name.equals(callb.name)&&calla.desc.equals(callb.desc)&&calla.bsm.equals(callb.bsm)&& Arrays.equals(calla.bsmArgs,callb.bsmArgs);
        }else if (a instanceof JumpInsnNode){
            return true;
        }else if (a instanceof LdcInsnNode){
            LdcInsnNode calla=(LdcInsnNode) a,callb=(LdcInsnNode) b;
            return calla.cst==callb.cst;
        }else if (a instanceof LookupSwitchInsnNode){
            LookupSwitchInsnNode calla=(LookupSwitchInsnNode) a,callb=(LookupSwitchInsnNode) b;
            return calla.keys==callb.keys||calla.keys.equals(callb.keys);
        }else if (a instanceof MultiANewArrayInsnNode){
            MultiANewArrayInsnNode calla=(MultiANewArrayInsnNode) a,callb=(MultiANewArrayInsnNode) b;
            return calla.desc.equals(callb.desc)&&calla.dims==callb.dims;
        }else if (a instanceof TableSwitchInsnNode){
            TableSwitchInsnNode calla=(TableSwitchInsnNode) a,callb=(TableSwitchInsnNode) b;
            return calla.max==callb.max&&calla.min==callb.min;
        }else if (a instanceof TypeInsnNode){
            TypeInsnNode calla=(TypeInsnNode) a,callb=(TypeInsnNode) b;
            return calla.desc.equals(callb.desc);
        }else if (a instanceof VarInsnNode){
            VarInsnNode calla=(VarInsnNode) a,callb=(VarInsnNode) b;
            return calla.var==callb.var;
        }
        return false;
    }

    private static boolean safeCMP(@Nullable List<Object> a,@Nullable List<Object> b){
        if (a==b){
            return true;
        }
        if (a==null||b==null){
            return false;
        }
        if (a.size()!=b.size()){
            return false;
        }
        for (int i=0;i<a.size();i++){
            Object obj1=a.get(i),obj2=b.get(i);
            if (!(obj1 instanceof LabelNode)&&!(obj2 instanceof LabelNode)){
                if (obj1!=null){
                    if (!obj1.equals(obj2)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static final class CMP{
        private final MethodNode method;
        public CMP(MethodNode method){
            this.method=method;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==null){
                return false;
            }
            if (obj instanceof MethodNode){
                MethodNode a=(MethodNode) obj;
                return a.name.equals(this.method.name) && a.desc.equals(this.method.desc);
            }
            return false;
        }
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return oldLPS.handlesClass(classType,isEmpty,reason);
    }

    @Override
    public void offerResource(Path resource, String name) {
        oldLPS.offerResource(resource,name);
    }

    @Override
    public void addResources(List<Map.Entry<String, Path>> resources) {
        oldLPS.addResources(resources);
    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, Path[] specialPaths) {
        oldLPS.initializeLaunch(transformerLoader,specialPaths);
    }

    @Override
    public <T> T getExtension() {
        return oldLPS.getExtension();
    }

    @Override
    public void customAuditConsumer(String className, Consumer<String[]> auditDataAcceptor) {
        oldLPS.customAuditConsumer(className,auditDataAcceptor);
    }

    @Override
    public boolean equals(Object obj) {
        return oldLPS.equals(obj);
    }

    @Override
    public int hashCode() {
        return oldLPS.hashCode();
    }

    @Override
    public String toString() {
        return oldLPS.toString();
    }
}
