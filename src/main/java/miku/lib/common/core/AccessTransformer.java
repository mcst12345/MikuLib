package miku.lib.common.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class AccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("")) {
        }
        return basicClass;
    }
}
