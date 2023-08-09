package miku.lib.mixins.minecraft;

import miku.lib.client.api.iFallbackResourceManager;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = FallbackResourceManager.class)
public class MixinFallbackResourceManager implements iFallbackResourceManager {

    @Shadow
    @Final
    protected List<IResourcePack> resourcePacks;

    @Override
    public List<IResourcePack> getResourcePacks() {
        return resourcePacks;
    }
}
