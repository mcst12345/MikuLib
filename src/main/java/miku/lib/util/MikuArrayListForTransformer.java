package miku.lib.util;

import miku.lib.core.MikuTweaker;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;

public class MikuArrayListForTransformer<E> extends ArrayList<E> {
    public MikuArrayListForTransformer(int var1) {
        super(var1);
    }

    protected static boolean isGoodTransformer(Object var1) {
        String s = var1.getClass().toString().substring(5).trim();
        return s.matches("(.*)guichaguri.betterfps.tweaker.BetterFpsTweaker(.*)") || s.matches("(.*)org.spongepowered.asm.launch.MixinTweaker(.*)") ||
                s.matches("(.*)miku.lib.core.MikuTweaker(.*)") || s.matches("(.*)optifine.OptiFineForgeTweaker(.*)") || s.matches("(.*)net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker(.*)") ||
                s.matches("(.*)net.minecraftforge.fml.common.launcher.FMLTweaker(.*)");
    }

    @Override
    public boolean add(E var1) {
        if (var1 instanceof IClassTransformer) {
            if (!MikuTweaker.isGoodClass(var1.getClass().toString().substring(5).trim()) && !isGoodTransformer(var1))
                return false;
        }
        return super.add(var1);
    }
}
//