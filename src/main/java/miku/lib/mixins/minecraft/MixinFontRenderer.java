package miku.lib.mixins.minecraft;

import miku.lib.api.iFontRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer implements iFontRenderer {

    @Shadow(remap = false) protected abstract void enableAlpha();

    @Shadow protected abstract void resetStyles();

    @Shadow private boolean bidiFlag;

    @Shadow protected abstract String bidiReorder(String text);

    @Shadow private float red;

    @Shadow private float blue;

    @Shadow private float green;

    @Shadow private float alpha;

    @Shadow protected float posX;

    @Shadow protected float posY;

    @Shadow(remap = false) protected abstract void doDraw(float f);

    @Override
    public void EnableAlpha() {
        this.enableAlpha();
    }
    public void ResetStyles(){
        this.resetStyles();
    }
    public boolean bidiFlag(){
        return this.bidiFlag;
    }
    public String BidiReorder(String text){
        return this.bidiReorder(text);
    }
    public void SetRed(float f){
        this.red = f;
    }
    public void SetBlue(float f){
        this.blue = f;
    }
    public void SetGreen(float f){
        this.green = f;
    }
    public void SetAlpha(float f){
        this.alpha = f;
    }
    public void setColor(){
        GlStateManager.color(this.red,this.green,this.blue,this.alpha);
    }
    public void SetX(float f){
        this.posX = f;
    }
    public void SetY(float f){
        this.posY = f;
    }
    public void renderStringAtPos(String text){
        for (int i = 0; i < text.length(); ++i)
        {
            doDraw(0);
        }
    }
}
