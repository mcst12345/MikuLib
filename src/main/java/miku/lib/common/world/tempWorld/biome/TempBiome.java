package miku.lib.common.world.tempWorld.biome;

import net.minecraft.world.biome.Biome;

public class TempBiome extends Biome {
    public TempBiome() {
        super(new BiomeProperties("temp").setBaseHeight(0.0f).setRainDisabled().setTemperature(20).setWaterColor(0x39c5bb));
    }

    @Override
    public float getSpawningChance() {
        return 0.0f;
    }

    @Override
    public boolean canRain() {
        return false;
    }
}
