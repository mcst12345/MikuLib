package miku.lib.mixins.minecraftforge;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLContainer;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Locale;

@Mixin(value = GameData.class, remap = false)
public abstract class MixinGameData {

    /**
     * @author mcst12345
     * @reason Don't check my prefix.
     */
    @Overwrite
    public static ResourceLocation checkPrefix(String name, boolean warnOverrides) {
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index).toLowerCase(Locale.ROOT);
        name = index == -1 ? name : name.substring(index + 1);
        ModContainer mc = Loader.instance().activeModContainer();
        String prefix = mc == null || (mc instanceof InjectedModContainer && ((InjectedModContainer) mc).wrappedContainer instanceof FMLContainer) ? "minecraft" : mc.getModId().toLowerCase(Locale.ROOT);
        if (warnOverrides && !oldPrefix.equals(prefix) && oldPrefix.length() > 0) {
            prefix = oldPrefix;
        }
        return new ResourceLocation(prefix, name);
    }
}
