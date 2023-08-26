package miku.lib.client.render;

import miku.lib.common.entity.Lain;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderLain extends RenderLiving<Lain> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("miku", "textures/entities/lain.png");
    private final ModelPlayer lain;

    public RenderLain(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
        this.lain = new ModelPlayer(1.0f, true);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull Lain entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(@Nonnull Lain entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (Sqlite.DEBUG())
            System.out.println("MikuInfo:Rendering lain at X:" + x + " Y:" + y + " Z:" + z + " Yaw:" + entityYaw + " partialTicks:" + partialTicks);
        mainModel = lain;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
