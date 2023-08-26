package miku.lib.client.render;

import miku.lib.common.entity.Lain;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderLain extends RenderLiving<Lain> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("miku", "textures/entities/lain.png");

    public RenderLain(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(Lain entity) {
        return null;
    }
}
