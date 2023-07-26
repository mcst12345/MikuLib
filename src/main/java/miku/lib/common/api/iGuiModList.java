package miku.lib.common.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public interface iGuiModList {
    Minecraft getMC();
    int getListWidth();
    float getzLevel();
    FontRenderer GetfontRenderer();
}
