package miku.lib.util;

import miku.lib.util.transform.ASMUtil;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;

public class MikuArrayListForTransformer<E> extends ArrayList<E> {
    public MikuArrayListForTransformer(int var1) {
        super(var1);
    }

    private static boolean isGoodTransformer(String s) {
        return s.matches("(.*)optifine(.*)") || s.matches("(.*)minecraft(.*)");
    }

    @Override
    public boolean add(E var1) {
        if (var1 instanceof IClassTransformer) {
            if (!ASMUtil.isGoodClass(var1.getClass().toString().substring(5).trim()) && !isGoodTransformer(var1.getClass().toString().substring(5).trim()))
                return false;
        }
        return super.add(var1);
    }
}
