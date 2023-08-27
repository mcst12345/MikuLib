package miku.lib.common.core.proxy;

import miku.lib.client.event.KeyBoardEvent;
import miku.lib.client.model.ModelLain;
import miku.lib.client.render.RenderLain;
import miku.lib.common.entity.Lain;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy{
    public ClientProxy(){}

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        System.out.println("MikuInfo:Registering Keys.");
        KeyBoardEvent.Init();//Register key bindings.
        MinecraftForge.EVENT_BUS.register(new KeyBoardEvent());//Register keyboard event.
        System.out.println("MikuInfo:Registering renders.");
        RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelLain(), 0.0f));
    }

    @Override
    public void init(FMLInitializationEvent event){
        super.init(event);
    }
}
