package miku.lib.mixins.minecraft;

import miku.lib.client.api.iSimpleReloadableResourceManager;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager implements iSimpleReloadableResourceManager {
    @Shadow
    @Final
    private Map<String, FallbackResourceManager> domainResourceManagers;

    @Override
    public Map<String, FallbackResourceManager> getDomainResourceManagers() {
        return domainResourceManagers;
    }
}
