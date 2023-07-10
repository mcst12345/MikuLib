package miku.lib.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class MikuAccessTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!transformedName.matches("net.minecraft.(.*)") && !transformedName.matches("net.minecraftforge.(.*)")){

        }
        return basicClass;
    }
}
