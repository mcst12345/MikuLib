package miku.lib.common.world.biome;

import miku.lib.common.world.Void;
import net.minecraft.world.biome.BiomeProviderSingle;

public class VoidBiomeProvider extends BiomeProviderSingle {
    public VoidBiomeProvider() {
        super(Void.VoidBiome);
    }
}
