package miku.lib.thread;

import miku.lib.util.MikuArrayListEarly;
import miku.lib.util.transform.ASMUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.util.List;

public class TransformerFucker extends Thread {
    @Override
    public void run() {
        while (true) {
            System.out.println("TransformerFucker is running.");
            try {
                Field transformers = Launch.classLoader.getClass().getDeclaredField("transformers");
                transformers.setAccessible(true);
                List<IClassTransformer> t = (List<IClassTransformer>) transformers.get(Launch.classLoader);
                t.removeIf(transformer -> !ASMUtil.isGoodClass(transformer.getClass().toString().substring(5).trim()));//Fuck other transformers.
                if (!(t instanceof MikuArrayListEarly)) {
                    MikuArrayListEarly<IClassTransformer> fucked = new MikuArrayListEarly<IClassTransformer>(2);
                    for (IClassTransformer i : t) {
                        fucked.add(i);
                    }
                    transformers.set(Launch.classLoader, fucked);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
