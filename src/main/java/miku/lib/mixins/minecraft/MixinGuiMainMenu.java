package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.*;
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
    @Shadow private float panoramaTimer;

    @Shadow protected abstract void renderSkybox(int mouseX, int mouseY, float partialTicks);

    @Shadow @Final private static ResourceLocation MINECRAFT_TITLE_TEXTURES;
    @Shadow @Final private float minceraftRoll;
    @Shadow private int widthCopyright;
    @Shadow private String openGLWarning1;
    @Shadow private int openGLWarningX1;
    @Shadow private int openGLWarningY1;
    @Shadow private int openGLWarningX2;
    @Shadow private String openGLWarning2;
    @Shadow private int openGLWarning2Width;
    @Shadow private int widthCopyrightRest;
    @Shadow private GuiScreen realmsNotification;

    @Shadow protected abstract boolean areRealmsNotificationsEnabled();

    @Shadow private int openGLWarningY2;
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
//if(!field_194400_H.getNamespace().equals("miku"))field_194400_H = new ResourceLocation("miku:textures/gui/miku-edition.png");

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        field_194400_H = new ResourceLocation("miku:textures/gui/miku-edition.png");
        this.panoramaTimer += partialTicks;
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if ((double)this.minceraftRoll < 1.0E-4D)
        {
            this.drawTexturedModalRect(j + 0, 30, 0, 0, 99, 44);
            this.drawTexturedModalRect(j + 99, 30, 129, 0, 27, 44);
            this.drawTexturedModalRect(j + 99 + 26, 30, 126, 0, 3, 44);
            this.drawTexturedModalRect(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        }
        else
        {
            this.drawTexturedModalRect(j + 0, 30, 0, 0, 155, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        }

        this.mc.getTextureManager().bindTexture(field_194400_H);
        drawModalRectWithCustomSizedTexture(j + 50, 50, 0.0F, 0.0F, 250, 30, 172.0F, 34.0F);

        this.splashText = net.minecraftforge.client.ForgeHooksClient.renderMainMenu((GuiMainMenu) (Object)this, this.fontRenderer, this.width, this.height, this.splashText);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
        f = f * 100.0F / (float)(this.fontRenderer.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(f, f, f);
        this.drawCenteredString(this.fontRenderer, this.splashText, 0, -8, -256);
        GlStateManager.popMatrix();
        String s = "Minecraft 1.12.2";

        if (this.mc.isDemo())
        {
            s = s + " Demo";
        }
        else
        {
            s = s + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType());
        }

        java.util.List<String> brandings = com.google.common.collect.Lists.reverse(net.minecraftforge.fml.common.FMLCommonHandler.instance().getBrandings(true));
        for (int brdline = 0; brdline < brandings.size(); brdline++)
        {
            String brd = brandings.get(brdline);
            if (!com.google.common.base.Strings.isNullOrEmpty(brd))
            {
                this.drawString(this.fontRenderer, brd, 2, this.height - ( 10 + brdline * (this.fontRenderer.FONT_HEIGHT + 1)), 16777215);
            }
        }

        this.drawString(this.fontRenderer, "Copyright Mojang AB. Do not distribute!", this.widthCopyrightRest, this.height - 10, -1);

        if (mouseX > this.widthCopyright && mouseX < this.widthCopyrightRest + this.widthCopyright && mouseY > this.height - 10 && mouseY < this.height && Mouse.isInsideWindow())
        {
            drawRect(this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, -1);
        }

        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty())
        {
            drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 1428160512);
            this.drawString(this.fontRenderer, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
            this.drawString(this.fontRenderer, this.openGLWarning2, (this.width - this.openGLWarning2Width) / 2, (this.buttonList.get(0)).y - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.areRealmsNotificationsEnabled())
        {
            this.realmsNotification.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
