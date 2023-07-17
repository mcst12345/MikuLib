package miku.lib.proxy;

import miku.lib.util.ClassUtil;
import miku.lib.world.Void;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.spongepowered.asm.mixin.Mixins;

public class CommonProxy {
    public CommonProxy(){}
    public void preInit(FMLPreInitializationEvent event){
        Void.Init();//Register the void world.
    }

    public void init(FMLInitializationEvent event){

    }

}
