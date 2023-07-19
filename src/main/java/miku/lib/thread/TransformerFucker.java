package miku.lib.thread;

import miku.lib.util.transform.ASMUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.util.List;

public class TransformerFucker extends Thread {
    @Override
    public void run() {
        try {
            Field transformers = Launch.classLoader.getClass().getDeclaredField("transformers");
            transformers.setAccessible(true);
            List<IClassTransformer> t = (List<IClassTransformer>) transformers.get(Launch.classLoader);
            t.removeIf(transformer -> !ASMUtil.isGoodClass(transformer.getClass().toString().substring(5).trim()));//Fuck other transformers.
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
