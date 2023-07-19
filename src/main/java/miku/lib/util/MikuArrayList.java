package miku.lib.util;

import miku.lib.util.transform.ASMUtil;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;

public class MikuArrayList<E> extends ArrayList<E> {
    @Override
    public boolean add(E var1) {
        if (var1 instanceof IClassTransformer) {
            if (!ASMUtil.isGoodClass(var1.getClass().toString().substring(5).trim())) return false;
        }
        if (var1 instanceof Entity) {
            if (EntityUtil.isDEAD((Entity) var1)) return false;
        }
        return super.add(var1);
    }
}
