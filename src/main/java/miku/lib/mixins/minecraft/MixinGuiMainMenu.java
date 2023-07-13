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

    @Inject(at=@At("TAIL"),method = "<init>")
    public void init(CallbackInfo ci){
        SPLASH_TEXTS = new ResourceLocation("miku:texts/splashes.txt");
    }
}
