package miku.lib.common.core;

import miku.lib.common.entity.MikuArrow;
import miku.lib.common.entity.MikuTNT;
import miku.lib.common.util.Register;
import miku.lib.common.world.Void;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;

@Mod.EventBusSubscriber
public class LibLoader {
    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;
        Register.RegisterEntity("miku_tnt", MikuTNT.class, "MikuTNT", id++,MikuLib.INSTANCE);
        Register.RegisterEntity("miku_arrow", MikuArrow.class, "MikuArrow", id++,MikuLib.INSTANCE);
    }

    @SubscribeEvent
    public void onRegisterBiomeEvent(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(Void.VoidBiome.setRegistryName("miku:void"));
    }

}
