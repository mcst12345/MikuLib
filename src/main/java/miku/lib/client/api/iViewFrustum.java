package miku.lib.client.api;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;

public interface iViewFrustum {
    RenderChunk GetRenderChunk(BlockPos pos);
}
