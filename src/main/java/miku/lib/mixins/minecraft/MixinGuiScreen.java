package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.core.MikuLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.List;

@Mixin(value = GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {
    @Shadow
    public Minecraft mc;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    public abstract void drawBackground(int tint);

    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    protected GuiButton selectedButton;

    @Shadow
    protected abstract void actionPerformed(GuiButton button) throws IOException;

    @Shadow
    protected RenderItem itemRender;

    @Shadow
    protected FontRenderer fontRenderer;

    @Shadow
    public abstract void initGui();

    @Shadow
    protected boolean mouseHandled;

    @Shadow
    public abstract void handleMouseInput() throws IOException;

    @Shadow
    protected boolean keyHandled;

    @Shadow
    public abstract void handleKeyboardInput() throws IOException;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null) {
                ((iMinecraft) this.mc).SET_INGAME_FOCUS();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void drawWorldBackground(int tint) {
        if (((iMinecraft) (this.mc)).MikuWorld() != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.drawBackground(tint);
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!!
     */
    @Overwrite
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (int i = 0; i < this.buttonList.size(); ++i) {
                GuiButton guibutton = this.buttonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre((GuiScreen) (Object) this, guibutton, this.buttonList);
                    if (MikuLib.MikuEventBus().post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post((GuiScreen) (Object) this, event.getButton(), this.buttonList));
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = width;
        this.height = height;
        if (!MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre((GuiScreen) (Object) this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }
        MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post((GuiScreen) (Object) this, this.buttonList));
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!
     */
    @Overwrite
    public void handleInput() throws IOException {
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
                this.mouseHandled = false;
                if (MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Pre((GuiScreen) (Object) this)))
                    continue;
                this.handleMouseInput();
                if (this.equals(this.mc.currentScreen) && !this.mouseHandled)
                    MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Post((GuiScreen) (Object) this));
            }
        }

        if (Keyboard.isCreated()) {
            while (Keyboard.next()) {
                this.keyHandled = false;
                if (MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Pre((GuiScreen) (Object) this)))
                    continue;
                this.handleKeyboardInput();
                if (this.equals(this.mc.currentScreen) && !this.keyHandled)
                    MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Post((GuiScreen) (Object) this));
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public void drawDefaultBackground() {
        this.drawWorldBackground(0);
        MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
    }
}
