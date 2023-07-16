package miku.lib.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
    public ClientProxy(){}

    @Override
    public void preInit(FMLPreInitializationEvent event){
        super.preInit(event);
        System.out.println("Register Keys");
        //KeyBoardEvent.Init();
        //MinecraftForge.EVENT_BUS.register(new KeyBoardEvent());
    }

    @Override
    public void init(FMLInitializationEvent event){
        super.init(event);
    }
}
