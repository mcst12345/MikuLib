package miku.lib.mixins.minecraftforge;

import miku.lib.api.iGuiModList;
import miku.lib.util.INFO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.WHITE;

@Mixin(value = GuiModList.class,remap = false)
public abstract class MixinGuiModList extends GuiScreen implements iGuiModList {
    @Shadow private GuiButton configModButton;

    @Shadow private GuiButton disableModButton;

    @Shadow private GuiScrollingList modInfo;

    @Shadow private ModContainer selectedMod;

    @Shadow private int listWidth;

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    private void updateCache()
    {
        configModButton.visible = false;
        disableModButton.visible = false;
        modInfo = null;

        if (selectedMod == null)
            return;

        System.out.println("Mod id is:"+selectedMod.getModId());

        boolean miku = Objects.equals(selectedMod.getModId(), "mikulib");

        ResourceLocation logoPath = null;
        Dimension logoDims = new Dimension(0, 0);
        List<String> lines = new ArrayList<>();
        ForgeVersion.CheckResult vercheck = ForgeVersion.getResult(selectedMod);

        String logoFile = selectedMod.getMetadata().logoFile;
        if (!logoFile.isEmpty())
        {
            TextureManager tm = mc.getTextureManager();
            IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(selectedMod.getModId());
            try
            {
                BufferedImage logo = null;
                if (pack != null)
                {
                    logo = pack.getPackImage();
                }
                else
                {
                    InputStream logoResource = getClass().getResourceAsStream(logoFile);
                    if (logoResource != null)
                        logo = TextureUtil.readBufferedImage(logoResource);
                }
                if (logo != null)
                {
                    logoPath = tm.getDynamicTextureLocation("modlogo", new DynamicTexture(logo));
                    if(miku)logoPath = new ResourceLocation("miku","textures/cover.png");
                    logoDims = new Dimension(logo.getWidth(), logo.getHeight());
                }
            }
            catch (IOException ignored) { }
        }

        String s = "Update Available: " + (vercheck.url == null ? "" : vercheck.url);
        if (!selectedMod.getMetadata().autogenerated)
        {
            disableModButton.visible = true;
            disableModButton.enabled = true;
            disableModButton.packedFGColour = 0;
            ModContainer.Disableable disableable = selectedMod.canBeDisabled();
            if (disableable == ModContainer.Disableable.RESTART)
            {
                disableModButton.packedFGColour = 0xFF3377;
            }
            else if (disableable != ModContainer.Disableable.YES)
            {
                disableModButton.enabled = false;
            }

            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(selectedMod);
            configModButton.visible = true;
            configModButton.enabled = false;
            if (guiFactory != null)
            {
                configModButton.enabled = guiFactory.hasConfigGui();
            }
            lines.add(selectedMod.getMetadata().name);
            lines.add(String.format("Version: %s (%s)", selectedMod.getDisplayVersion(), selectedMod.getVersion()));
            lines.add(String.format("Mod ID: '%s' Mod State: %s", selectedMod.getModId(), Loader.instance().getModState(selectedMod)));

            if (!selectedMod.getMetadata().credits.isEmpty())
            {
                lines.add("Credits: " + selectedMod.getMetadata().credits);
            }

            lines.add("Authors: " + selectedMod.getMetadata().getAuthorList());
            lines.add("URL: " + selectedMod.getMetadata().url);

            if (selectedMod.getMetadata().childMods.isEmpty())
                lines.add("No child mods for this mod");
            else
                lines.add("Child mods: " + selectedMod.getMetadata().getChildModList());

            if (vercheck.status == ForgeVersion.Status.OUTDATED || vercheck.status == ForgeVersion.Status.BETA_OUTDATED)
                lines.add(s);

            lines.add(null);
            lines.add(selectedMod.getMetadata().description);
            if(miku)lines.clear();
        }
        else
        {
            lines.add(WHITE + selectedMod.getName());
            lines.add(WHITE + "Version: " + selectedMod.getVersion());
            lines.add(WHITE + "Mod State: " + Loader.instance().getModState(selectedMod));
            if (vercheck.status == ForgeVersion.Status.OUTDATED || vercheck.status == ForgeVersion.Status.BETA_OUTDATED)
                lines.add(s);

            lines.add(null);
            lines.add(RED + "No mod information found");
            lines.add(RED + "Ask your mod author to provide a mod mcmod.info file");
        }

        if ((vercheck.status == ForgeVersion.Status.OUTDATED || vercheck.status == ForgeVersion.Status.BETA_OUTDATED) && vercheck.changes.size() > 0)
        {
            lines.add(null);
            lines.add("Changes:");
            for (Map.Entry<ComparableVersion, String> entry : vercheck.changes.entrySet())
            {
                lines.add("  " + entry.getKey() + ":");
                lines.add(entry.getValue());
                lines.add(null);
            }
        }

        modInfo = new INFO(this.width - this.listWidth - 30, lines, logoPath, logoDims,(GuiModList) (Object)this);
    }



    public Minecraft getMC(){
        return mc;
    }

    public int getListWidth(){
        return listWidth;
    }

    public float getzLevel(){
        return zLevel;
    }

    public FontRenderer GetfontRenderer(){
        return fontRenderer;
    }
}
