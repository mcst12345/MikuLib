package miku.lib.common.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class ModTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return basicClass;
    }
}
