package miku.lib.mixins;

import miku.lib.api.iMinecraft;
import miku.lib.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class)
public class MixinMinecraft implements iMinecraft {
    @Shadow public EntityPlayerSP player;

    @Shadow private ModelManager modelManager;

    @Inject(at = @At("HEAD"), method = "displayGuiScreen", cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        if(EntityUtil.isProtected(player)){
            if (guiScreenIn instanceof GuiGameOver) {
                guiScreenIn.onGuiClosed();
                ci.cancel();
            }
            if (guiScreenIn != null) {
                if (guiScreenIn.toString() != null) {
                    if (guiScreenIn.toString().toLowerCase().matches("(.*)dead(.*)") || guiScreenIn.toString().toLowerCase().matches("(.*)gameover(.*)")) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Override
    public ModelManager GetModelManager() {
        return modelManager;
    }
}
