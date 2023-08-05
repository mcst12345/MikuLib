package miku.lib.mixins.minecraftforge;

import miku.lib.common.api.iMinecraftForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MinecraftForge.class)
public abstract class MixinMinecraftForge implements iMinecraftForge {
    private static final EventBus MikuEventBus = new EventBus();

    public EventBus MikuEventBus() {
        return MikuEventBus;
    }
}
