package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

@Mixin(value = GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Mutable
    @Shadow @Final private static ResourceLocation field_194400_H;

    @Shadow private String splashText;
    @Shadow @Final private static Random RANDOM;
    private static final ResourceLocation MikuText = new ResourceLocation("miku:texts/splashes.txt");

    @Inject(at=@At("TAIL"),method = "<init>")
    public void init(CallbackInfo ci){
        try {
            List<String> list = Lists.newArrayList();
            IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(MikuText);
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null) {
                s = s.trim();

                if (!s.isEmpty()) {
                    list.add(s);
                }
            }

            if (!list.isEmpty())
            {

                do {
                    this.splashText = list.get(RANDOM.nextInt(list.size()));

                } while (this.splashText.hashCode() == 125780783);//What the fuck is this number?
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Inject(at=@At("HEAD"),method = "drawScreen")
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci){
        if(!field_194400_H.getNamespace().equals("miku"))field_194400_H = new ResourceLocation("miku:textures/gui/miku-edition-disabled.png");
    }
}
