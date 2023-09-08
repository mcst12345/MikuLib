package miku.lib.common.world.tempWorld.biome;

import miku.lib.common.world.tempWorld.TempWorld;
import net.minecraft.world.biome.BiomeProviderSingle;

public class TempBiomeProvider extends BiomeProviderSingle {
    public TempBiomeProvider() {
        super(TempWorld.TempBiome);
    }
}
