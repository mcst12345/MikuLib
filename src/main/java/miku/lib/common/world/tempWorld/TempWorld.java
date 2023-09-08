package miku.lib.common.world.tempWorld;

import miku.lib.common.world.tempWorld.biome.TempBiome;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class TempWorld {
    public static final Biome TempBiome = new TempBiome();
    public static final int ID = -114514;
    public static final DimensionType Temp = DimensionType.register("temp", "temp", ID, TempProvider.class, true);

    public static void Init() {
        DimensionManager.registerDimension(ID, Temp);
    }

    @SubscribeEvent
    public void onRegisterBiomeEvent(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(TempBiome.setRegistryName("miku:temp"));
    }
}
