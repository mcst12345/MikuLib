package miku.lib.common.core.proxy;

import miku.lib.common.world.tempWorld.TempWorld;
import miku.lib.common.world.voidWorld.VoidWorld;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public CommonProxy(){}
    public void preInit(FMLPreInitializationEvent event) {
        VoidWorld.Init();//Register the void world.
        TempWorld.Init();//Register the temp world.
    }

    public void init(FMLInitializationEvent event){

    }

}
