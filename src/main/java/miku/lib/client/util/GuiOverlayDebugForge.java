package miku.lib.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;

import java.util.List;

public class GuiOverlayDebugForge extends GuiOverlayDebug {
    private final Minecraft mc;

    public GuiOverlayDebugForge(Minecraft mc) {
        super(mc);
        this.mc = mc;
    }

    @Override
    protected void renderDebugInfoLeft() {
    }

    @Override
    protected void renderDebugInfoRight(ScaledResolution res) {
    }

    public List<String> getLeft() {
        List<String> ret = this.call();
        ret.add("");
        ret.add("Debug: Pie [shift]: " + (this.mc.gameSettings.showDebugProfilerChart ? "visible" : "hidden") + " FPS [alt]: " + (this.mc.gameSettings.showLagometer ? "visible" : "hidden"));
        ret.add("For help: press F3 + Q");
        return ret;
    }

    public List<String> getRight() {
        return this.getDebugInfoRight();
    }
}
