package miku.lib.mixins;

import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WorldServer.class)
public abstract class MixinWorldServer {
}
