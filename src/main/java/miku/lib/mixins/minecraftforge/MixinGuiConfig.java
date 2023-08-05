package miku.lib.mixins.minecraftforge;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiConfig.class, remap = false)
public abstract class MixinGuiConfig extends GuiScreen {
    @Mutable
    @Shadow
    @Final
    public boolean isWorldRunning;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;)V")
    public void GuiConfig(GuiScreen parentScreen, List configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2, CallbackInfo ci) {
        this.isWorldRunning = ((iMinecraft) mc).MikuWorld() != null;
    }
}
