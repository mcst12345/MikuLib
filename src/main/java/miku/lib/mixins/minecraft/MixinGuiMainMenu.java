package miku.lib.mixins.minecraft;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {
    @Mutable
    @Shadow @Final private static ResourceLocation SPLASH_TEXTS;

    @Mutable
    @Shadow @Final private static ResourceLocation field_194400_H;

    @Inject(at=@At("HEAD"),method = "drawPanorama")
    public void init(int mouseX, int mouseY, float partialTicks,CallbackInfo ci){
        if(!SPLASH_TEXTS.getNamespace().equals("miku"))SPLASH_TEXTS = new ResourceLocation("miku:texts/splashes.txt");
    }

    @Inject(at=@At("HEAD"),method = "drawScreen")
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci){
        if(!field_194400_H.getNamespace().equals("miku"))field_194400_H = new ResourceLocation("miku:textures/gui/Miku-Edition.png");
    }
}
