package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Biome.class)
public abstract class MixinBiome extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<Biome> {
    @Shadow
    @Final
    private int waterColor;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public int getWaterColorMultiplier() {
        net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor event = new net.minecraftforge.event.terraingen.BiomeEvent.GetWaterColor((Biome) (Object) this, waterColor);
        MikuLib.MikuEventBus().post(event);
        return event.getNewColor();
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public int getModdedBiomeGrassColor(int original) {
        net.minecraftforge.event.terraingen.BiomeEvent.GetGrassColor event = new net.minecraftforge.event.terraingen.BiomeEvent.GetGrassColor((Biome) (Object) this, original);
        MikuLib.MikuEventBus().post(event);
        return event.getNewColor();
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public int getModdedBiomeFoliageColor(int original) {
        net.minecraftforge.event.terraingen.BiomeEvent.GetFoliageColor event = new net.minecraftforge.event.terraingen.BiomeEvent.GetFoliageColor((Biome) (Object) this, original);
        MikuLib.MikuEventBus().post(event);
        return event.getNewColor();
    }
}
