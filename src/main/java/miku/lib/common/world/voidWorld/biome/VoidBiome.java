package miku.lib.common.world.voidWorld.biome;

import net.minecraft.world.biome.Biome;

public class VoidBiome extends Biome {
    public VoidBiome() {
        super(new BiomeProperties("void").setBaseHeight(0.0f).setRainDisabled().setTemperature(0.0f).setWaterColor(0x39c5bb));
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
