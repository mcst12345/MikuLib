package miku.lib.mixins.minecraftforge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MinecraftForge.class, remap = false)
public abstract class MixinMinecraftForge {
    @Mutable
    @Shadow
    public static EventBus EVENT_BUS;

}
