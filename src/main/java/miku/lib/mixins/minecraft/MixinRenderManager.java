package miku.lib.mixins.minecraft;

import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(value = RenderManager.class)
public abstract class MixinRenderManager {
    @Shadow
    @Final
    public Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap;

    @Shadow
    @Nullable
    public abstract <T extends Entity> Render<T> getEntityRenderObject(Entity entityIn);

    @Shadow
    public TextureManager renderEngine;

    @Shadow
    private boolean renderOutlines;

    @Shadow
    private boolean debugBoundingBox;

    @Shadow
    protected abstract void renderDebugBoundingBox(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks);

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public <T extends Entity> Render<T> getEntityClassRenderObject(Class<? extends Entity> entityClass) {
        if (Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("render_info", "LOG_CONFIG", 0)) {
            System.out.println("MikuInfo:Getting render for entity:" + entityClass);
        }
        Render<T> render = (Render<T>) this.entityRenderMap.get(entityClass);

        if (render == null && entityClass != Entity.class) {
            render = this.getEntityClassRenderObject((Class<? extends Entity>) entityClass.getSuperclass());
            this.entityRenderMap.put(entityClass, render);
        }

        return render;
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public boolean shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ) {
        Render<Entity> render = this.getEntityRenderObject(entityIn);
        if (EntityUtil.isProtected(entityIn)) {
            boolean result = render != null;
            if (result && !(entityIn instanceof EntityPlayer))
                System.out.println("MikuWarn:The fuck? Can't get render of a protected entity! " + entityIn.getClass());
            return result;
        } else if (EntityUtil.isDEAD(entityIn)) {
            return false;
        } else {
            return render != null && render.shouldRender(entityIn, camera, camX, camY, camZ);
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void renderEntity(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_) {
        if (EntityUtil.isDEAD(entityIn)) return;

        Render<Entity> render;

        try {
            render = this.getEntityRenderObject(entityIn);

            if (render != null && this.renderEngine != null) {
                if (Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("render_info", "LOG_CONFIG", 0))
                    System.out.println("MikuInfo:Rendering entity at X:" + x + " Y:" + x + " Z:" + z + " Yaw:" + yaw + " partialTicks:" + partialTicks);
                try {
                    render.setRenderOutlines(this.renderOutlines);
                    render.doRender(entityIn, x, y, z, yaw, partialTicks);
                } catch (Throwable throwable1) {
                    System.out.println("MikuWarn:Catch exception rendering entity:" + entityIn.getClass());
                    throwable1.printStackTrace();
                }

                try {
                    if (!this.renderOutlines) {
                        render.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
                    }
                } catch (Throwable throwable2) {
                    System.out.println("MikuWarn:Catch exception rendering entity:" + entityIn.getClass());
                    throwable2.printStackTrace();
                }

                if (this.debugBoundingBox && !entityIn.isInvisible() && !p_188391_10_ && !Minecraft.getMinecraft().isReducedDebug()) {
                    try {
                        this.renderDebugBoundingBox(entityIn, x, y, z, yaw, partialTicks);
                    } catch (Throwable throwable) {
                        System.out.println("MikuWarn:Catch exception rendering entity:" + entityIn.getClass());
                        throwable.printStackTrace();
                    }
                }
            } else {
                System.out.println("MikuWarn:Failed to render entity:" + entityIn.getClass() + ",maybe render is null or renderEngine is null");
            }
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception rendering entity:" + entityIn.getClass());
            t.printStackTrace();
        }
    }
}
