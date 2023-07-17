package miku.lib.util;

import miku.lib.api.iGuiModList;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//ShitMountain.
//Just for a biggggggggggggggger mod cover.
public class INFO extends GuiScrollingList {
    private final GuiModList instance;
    @Nullable
    private final ResourceLocation logoPath;
    private final Dimension logoDims;
    private final java.util.List<ITextComponent> lines;

    public INFO(int width, java.util.List<String> lines, @Nullable ResourceLocation logoPath, Dimension logoDims,GuiModList instance)
    {

        super(((iGuiModList)instance).getMC(),
                width,
                instance.height,
                32, instance.height - 88 + 4,
                ((iGuiModList)instance).getListWidth() + 20, 60,
                instance.width,
                instance.height);
        this.instance = instance;
        this.lines    = resizeContent(lines);
        this.logoPath = logoPath;
        this.logoDims = logoDims;

        this.setHeaderInfo(true, getHeaderHeight());
    }

    @Override protected int getSize() { return 0; }
    @Override protected void elementClicked(int index, boolean doubleClick) { }
    @Override protected boolean isSelected(int index) { return false; }
    @Override protected void drawBackground() {}
    @Override protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) { }

    private java.util.List<ITextComponent> resizeContent(java.util.List<String> lines)
    {
        List<ITextComponent> ret = new ArrayList<>();
        for (String line : lines)
        {
            if (line == null)
            {
                ret.add(null);
                continue;
            }

            ITextComponent chat = ForgeHooks.newChatWithLinks(line, false);
            int maxTextLength = this.listWidth - 8;
            if (maxTextLength >= 0)
            {
                ret.addAll(GuiUtilRenderComponents.splitText(chat, maxTextLength, ((iGuiModList)instance).GetfontRenderer(), false, true));
            }
        }
        return ret;
    }

    private int getHeaderHeight()
    {
        int height = 0;
        if (logoPath != null)
        {
            double scaleX = logoDims.width / 200.0;
            double scaleY = logoDims.height / 65.0;
            double scale = 1.0;
            if (scaleX > 1 || scaleY > 1)
            {
                scale = 1.0 / Math.max(scaleX, scaleY);
            }
            logoDims.width *= scale;
            logoDims.height *= scale;

            height += logoDims.height;
            height += 10;
        }
        height += (lines.size() * 10);
        if (height < this.bottom - this.top - 8) height = this.bottom - this.top - 8;
        return height;
    }


    @Override
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess)
    {
        int top = relativeY;

        if (logoPath != null)
        {
            GlStateManager.enableBlend();
            instance.mc.renderEngine.bindTexture(logoPath);
            boolean miku = logoPath.getNamespace().equals("miku") && logoPath.getPath().equals("textures/cover.png");
            BufferBuilder wr = tess.getBuffer();
            int offset = (this.left + this.listWidth/2) - (logoDims.width / 2)*5;
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            if(miku) {
                wr.pos(offset, top + logoDims.height * 5, ((iGuiModList) instance).getzLevel()).tex(0, 1).endVertex();
                wr.pos(offset + logoDims.width * 5, top + logoDims.height * 5, ((iGuiModList) instance).getzLevel()).tex(1, 1).endVertex();
                wr.pos(offset + logoDims.width * 5, top, ((iGuiModList) instance).getzLevel()).tex(1, 0).endVertex();
            }
            else {
                wr.pos(offset, top + logoDims.height, ((iGuiModList) instance).getzLevel()).tex(0, 1).endVertex();
                wr.pos(offset + logoDims.width, top + logoDims.height, ((iGuiModList) instance).getzLevel()).tex(1, 1).endVertex();
                wr.pos(offset + logoDims.width, top, ((iGuiModList) instance).getzLevel()).tex(1, 0).endVertex();
            }
            wr.pos(offset, top, ((iGuiModList) instance).getzLevel()).tex(0, 0).endVertex();
            tess.draw();
            GlStateManager.disableBlend();
            top += logoDims.height + 10;
        }

        for (ITextComponent line : lines)
        {
            if (line != null)
            {
                GlStateManager.enableBlend();
                ((iGuiModList)instance).GetfontRenderer().drawStringWithShadow(line.getFormattedText(), this.left + 4, top, 0xFFFFFF);
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
            }
            top += 10;
        }
    }

    @Override
    protected void clickHeader(int x, int y)
    {
        int offset = y;
        if (logoPath != null) {
            offset -= logoDims.height + 10;
        }
        if (offset <= 0)
            return;

        int lineIdx = offset / 10;
        if (lineIdx >= lines.size())
            return;

        ITextComponent line = lines.get(lineIdx);
        if (line != null)
        {
            int k = -4;
            for (ITextComponent part : line) {
                if (!(part instanceof TextComponentString))
                    continue;
                k += ((iGuiModList)instance).GetfontRenderer().getStringWidth(((TextComponentString)part).getText());
                if (k >= x)
                {
                    instance.handleComponentClick(part);
                    break;
                }
            }
        }
    }
}
