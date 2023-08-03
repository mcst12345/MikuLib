package miku.lib.client.util;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ContainerLocalRenderInformation {
    public final RenderChunk renderChunk;
    public final EnumFacing facing;
    public byte setFacing;
    public final int counter;

    public ContainerLocalRenderInformation(RenderChunk renderChunkIn, @Nullable EnumFacing facingIn, int counterIn) {
        this.renderChunk = renderChunkIn;
        this.facing = facingIn;
        this.counter = counterIn;
    }

    public void setDirection(byte p_189561_1_, EnumFacing p_189561_2_) {
        this.setFacing = (byte) (this.setFacing | p_189561_1_ | 1 << p_189561_2_.ordinal());
    }

    public boolean hasDirection(EnumFacing p_189560_1_) {
        return (this.setFacing & 1 << p_189560_1_.ordinal()) > 0;
    }
}
