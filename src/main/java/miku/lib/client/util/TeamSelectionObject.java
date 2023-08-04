package miku.lib.client.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class TeamSelectionObject implements ISpectatorMenuObject {
    private final ScorePlayerTeam team;
    private final ResourceLocation location;
    private final List<NetworkPlayerInfo> players;

    public TeamSelectionObject(ScorePlayerTeam teamIn) {
        this.team = teamIn;
        this.players = Lists.newArrayList();

        for (String s : teamIn.getMembershipCollection()) {
            NetworkPlayerInfo networkplayerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(s);

            if (networkplayerinfo != null) {
                this.players.add(networkplayerinfo);
            }
        }

        if (this.players.isEmpty()) {
            this.location = DefaultPlayerSkin.getDefaultSkinLegacy();
        } else {
            String s1 = this.players.get((new Random()).nextInt(this.players.size())).getGameProfile().getName();
            this.location = AbstractClientPlayer.getLocationSkin(s1);
            AbstractClientPlayer.getDownloadImageSkin(this.location, s1);
        }
    }

    public void selectItem(SpectatorMenu menu) {
        menu.selectCategory(new TeleportToPlayer(this.players));
    }

    @Nonnull
    public ITextComponent getSpectatorName() {
        return new TextComponentString(this.team.getDisplayName());
    }

    public void renderIcon(float brightness, int alpha) {
        int i = -1;
        String s = FontRenderer.getFormatFromString(this.team.getPrefix());

        if (s.length() >= 2) {
            i = Minecraft.getMinecraft().fontRenderer.getColorCode(s.charAt(1));
        }

        if (i >= 0) {
            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            Gui.drawRect(1, 1, 15, 15, MathHelper.rgb(f * brightness, f1 * brightness, f2 * brightness) | alpha << 24);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.location);
        GlStateManager.color(brightness, brightness, brightness, (float) alpha / 255.0F);
        Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
        Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
    }

    public boolean isEnabled() {
        return !this.players.isEmpty();
    }
}
