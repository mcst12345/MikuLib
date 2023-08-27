package miku.lib.common.core;

import miku.lib.common.entity.Lain;
import miku.lib.common.entity.MikuArrow;
import miku.lib.common.entity.MikuTNT;
import miku.lib.common.util.Register;
import miku.lib.common.world.Void;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

@Mod.EventBusSubscriber
public class LibLoader {
    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;
        Register.RegisterEntity("mikulib", "miku_tnt", MikuTNT.class, "MikuTNT", id++, MikuLib.INSTANCE);
        Register.RegisterEntity("mikulib", "miku_arrow", MikuArrow.class, "MikuArrow", id++, MikuLib.INSTANCE);
        Register.RegisterEntity("mikulib", "lain", Lain.class, "Lain", id++, MikuLib.INSTANCE);
        EntityRegistry.registerEgg(new ResourceLocation("mikulib", "lain"), 0x39C5BB, 0x39C5BB);
    }

    @SubscribeEvent
    public void onRegisterBiomeEvent(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(Void.VoidBiome.setRegistryName("miku:void"));
    }
}
