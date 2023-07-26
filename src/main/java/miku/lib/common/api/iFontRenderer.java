package miku.lib.common.api;

public interface iFontRenderer {
    void EnableAlpha();
    void ResetStyles();
    boolean bidiFlag();
    String BidiReorder(String text);
    void SetRed(float f);
    void SetBlue(float f);
    void SetGreen(float f);
    void SetAlpha(float f);
    void setColor();
    void SetX(float f);
    void SetY(float y);
    void renderStringAtPos(String s);
}
