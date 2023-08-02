package miku.lib.mixins.minecraftforge;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FMLCommonHandler.class)
public class MixinFMLCommonHandler {
}
