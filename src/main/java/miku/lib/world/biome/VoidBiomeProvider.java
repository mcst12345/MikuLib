package miku.lib.world.biome;

import miku.lib.world.Void;
import net.minecraft.world.biome.BiomeProviderSingle;

public class VoidBiomeProvider extends BiomeProviderSingle {
    public VoidBiomeProvider() {
        super(Void.VoidBiome);
    }
}
