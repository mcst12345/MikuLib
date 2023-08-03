package miku.lib.mixins.minecraft;

import miku.lib.client.api.iViewFrustum;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = ViewFrustum.class)
public abstract class MixinViewFrustum implements iViewFrustum {

    @Shadow
    @Nullable
    protected abstract RenderChunk getRenderChunk(BlockPos pos);

    @Override
    public RenderChunk GetRenderChunk(BlockPos pos) {
        return getRenderChunk(pos);
    }
}
