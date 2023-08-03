package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug extends Gui {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    protected abstract void renderDebugInfoLeft();

    @Shadow
    protected abstract void renderDebugInfoRight(ScaledResolution scaledRes);

    @Shadow
    public abstract void renderLagometer();

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("debug");
        GlStateManager.pushMatrix();
        this.renderDebugInfoLeft();
        this.renderDebugInfoRight(scaledResolutionIn);
        GlStateManager.popMatrix();

        if (this.mc.gameSettings.showLagometer) {
            this.renderLagometer();
        }

        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }
}
