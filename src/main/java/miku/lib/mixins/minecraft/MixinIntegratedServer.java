package miku.lib.mixins.minecraft;

import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = IntegratedServer.class)
public class MixinIntegratedServer {
}
