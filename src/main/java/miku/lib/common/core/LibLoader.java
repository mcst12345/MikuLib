package miku.lib.common.core;

import miku.lib.client.model.ModelLain;
import miku.lib.client.render.RenderLain;
import miku.lib.common.entity.Lain;
import miku.lib.common.entity.MikuArrow;
import miku.lib.common.entity.MikuTNT;
import miku.lib.common.util.Register;
import miku.lib.common.world.Void;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class LibLoader {
    @SubscribeEvent
    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;
        Register.RegisterEntity("mikulib", "miku_tnt", MikuTNT.class, "MikuTNT", id++, MikuLib.INSTANCE);
        Register.RegisterEntity("mikulib", "miku_arrow", MikuArrow.class, "MikuArrow", id++, MikuLib.INSTANCE);
        Register.RegisterEntity("mikulib", "lain", Lain.class, "Lain", id++, MikuLib.INSTANCE);
    }

    @SubscribeEvent
    public void onRegisterBiomeEvent(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(Void.VoidBiome.setRegistryName("miku:void"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void RegisterModel(ModelRegistryEvent event) {
        System.out.println("MikuInfo:Registering renders.");
        RenderingRegistry.registerEntityRenderingHandler(Lain.class, manager -> new RenderLain(manager, new ModelLain(), 0.0f));
    }
}
