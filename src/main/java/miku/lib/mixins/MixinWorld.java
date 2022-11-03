package miku.lib.mixins;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = World.class)
public abstract class MixinWorld {
}
