package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class MixinTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
