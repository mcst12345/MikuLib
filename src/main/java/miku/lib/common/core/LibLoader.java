package miku.lib.common.core;

import miku.lib.common.world.Void;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class LibLoader {
    @SubscribeEvent
    public void onRegisterBiomeEvent(RegistryEvent.Register<Biome> event){
        event.getRegistry().register(Void.VoidBiome.setRegistryName("miku:void"));
    }
}
