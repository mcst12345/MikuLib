package miku.lib.common.core.proxy;

import miku.lib.client.event.KeyBoardEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
    public ClientProxy(){}

    @Override
    public void preInit(FMLPreInitializationEvent event){
        super.preInit(event);
        KeyBoardEvent.Init();//Register key bindings.
        MinecraftForge.EVENT_BUS.register(new KeyBoardEvent());//Register keyboard event.
    }

    @Override
    public void init(FMLInitializationEvent event){
        super.init(event);
    }
}
