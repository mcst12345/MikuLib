package miku.lib.common.core.proxy;

import miku.lib.common.world.Void;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public CommonProxy(){}
    public void preInit(FMLPreInitializationEvent event){
        Void.Init();//Register the void world.
    }

    public void init(FMLInitializationEvent event){

    }

}
