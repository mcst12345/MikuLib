package miku.lib.mixins.minecraftforge;

import net.minecraftforge.registries.ObjectHolderRegistry;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ObjectHolderRegistry.class)
public class MixinObjectHolderRegistry {
}
