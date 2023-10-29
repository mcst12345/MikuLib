package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = GuiIngame.class)
public abstract class MixinGuiIngame extends Gui {
    @Shadow
    @Final
    protected Minecraft mc;

    @Shadow
    public abstract FontRenderer getFontRenderer();


    @Shadow
    protected abstract void renderPumpkinOverlay(ScaledResolution scaledRes);

    @Shadow
    protected abstract void renderPortal(float timeInPortal, ScaledResolution scaledRes);

    @Shadow
    @Final
    protected GuiSpectator spectatorGui;

    @Shadow
    protected abstract void renderHotbar(ScaledResolution sr, float partialTicks);


    @Shadow
    @Final
    protected GuiBossOverlay overlayBoss;

    @Shadow
    protected abstract void renderPotionEffects(ScaledResolution resolution);

    @Shadow
    @Final
    protected GuiOverlayDebug overlayDebug;

    @Shadow
    protected int overlayMessageTime;

    @Shadow
    protected boolean animateOverlayMessageColor;

    @Shadow
    protected String overlayMessage;

    @Shadow
    @Final
    protected GuiSubtitleOverlay overlaySubtitle;

    @Shadow
    protected int titlesTimer;

    @Shadow
    protected int titleFadeOut;

    @Shadow
    protected int titleDisplayTime;

    @Shadow
    protected int titleFadeIn;

    @Shadow
    protected String displayedTitle;

    @Shadow
    protected String displayedSubTitle;

    @Shadow
    protected abstract void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes);

    @Shadow
    @Final
    protected GuiNewChat persistantChatGUI;

    @Shadow
    protected int updateCounter;

    @Shadow
    @Final
    protected GuiPlayerTabOverlay overlayPlayerList;

    @Shadow
    protected int remainingHighlightTicks;

    @Shadow
    protected ItemStack highlightingItemStack;

    @Shadow
    @Final
    protected Random rand;

    @Shadow
    protected abstract void renderPlayerStats(ScaledResolution scaledRes);

    @Shadow
    protected abstract void renderMountHealth(ScaledResolution p_184047_1_);

    @Shadow
    public abstract void renderHorseJumpBar(ScaledResolution scaledRes, int x);

    @Shadow
    public abstract void renderExpBar(ScaledResolution scaledRes, int x);

    @Shadow
    public abstract void renderSelectedItem(ScaledResolution scaledRes);

    @Shadow
    protected abstract void renderVignette(float lightLevel, ScaledResolution scaledRes);

    @Shadow
    protected abstract void renderAttackIndicator(float partialTicks, ScaledResolution p_184045_2_);

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderGameOverlay(float partialTicks) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaledWidth();
        int j = scaledresolution.getScaledHeight();
        FontRenderer fontrenderer = this.getFontRenderer();
        GlStateManager.enableBlend();

        if (Minecraft.isFancyGraphicsEnabled()) {
            this.renderVignette(((iMinecraft) this.mc).MikuPlayer().getBrightness(), scaledresolution);
        } else {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        ItemStack itemstack = ((iMinecraft) this.mc).MikuPlayer().inventory.armorItemInSlot(3);

        if (!EntityUtil.isProtected(this.mc))
            if (this.mc.gameSettings.thirdPersonView == 0 && itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
                this.renderPumpkinOverlay(scaledresolution);
            }

        if (!((iMinecraft) this.mc).MikuPlayer().isPotionActive(MobEffects.NAUSEA)) {
            float f = ((iMinecraft) this.mc).MikuPlayer().prevTimeInPortal + (((iMinecraft) this.mc).MikuPlayer().timeInPortal - ((iMinecraft) this.mc).MikuPlayer().prevTimeInPortal) * partialTicks;

            if (f > 0.0F) {
                this.renderPortal(f, scaledresolution);
            }
        }

        if (this.mc.playerController.isSpectator()) {
            this.spectatorGui.renderTooltip(scaledresolution, partialTicks);
        } else {
            this.renderHotbar(scaledresolution, partialTicks);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ICONS);
        GlStateManager.enableBlend();
        this.renderAttackIndicator(partialTicks, scaledresolution);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        ((iMinecraft) this.mc).MikuProfiler().startSection("bossHealth");
        this.overlayBoss.renderBossHealth();
        ((iMinecraft) this.mc).MikuProfiler().endSection();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ICONS);

        if (this.mc.playerController.shouldDrawHUD()) {
            this.renderPlayerStats(scaledresolution);
        }

        this.renderMountHealth(scaledresolution);
        GlStateManager.disableBlend();

        if (((iMinecraft) this.mc).MikuPlayer().getSleepTimer() > 0) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("sleep");
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            int j1 = ((iMinecraft) this.mc).MikuPlayer().getSleepTimer();
            float f1 = (float) j1 / 100.0F;

            if (f1 > 1.0F) {
                f1 = 1.0F - (float) (j1 - 100) / 10.0F;
            }

            int k = (int) (220.0F * f1) << 24 | 1052704;
            drawRect(0, 0, i, j, k);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int k1 = i / 2 - 91;

        if (((iMinecraft) this.mc).MikuPlayer().isRidingHorse()) {
            this.renderHorseJumpBar(scaledresolution, k1);
        } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
            this.renderExpBar(scaledresolution, k1);
        }

        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator()) {
            this.renderSelectedItem(scaledresolution);
        } else if (((iMinecraft) this.mc).MikuPlayer().isSpectator()) {
            this.spectatorGui.renderSelectedItem(scaledresolution);
        }

        this.renderPotionEffects(scaledresolution);

        if (this.mc.gameSettings.showDebugInfo) {
            this.overlayDebug.renderDebugInfo(scaledresolution);
        }

        if (this.overlayMessageTime > 0) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("overlayMessage");
            float f2 = (float) this.overlayMessageTime - partialTicks;
            int l1 = (int) (f2 * 255.0F / 20.0F);

            if (l1 > 255) {
                l1 = 255;
            }

            if (l1 > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (i / 2), (float) (j - 68), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int l = 16777215;

                if (this.animateOverlayMessageColor) {
                    l = MathHelper.hsvToRGB(f2 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                fontrenderer.drawString(this.overlayMessage, -fontrenderer.getStringWidth(this.overlayMessage) / 2, -4, l + (l1 << 24 & -16777216));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }

        this.overlaySubtitle.renderSubtitles(scaledresolution);

        if (this.titlesTimer > 0) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("titleAndSubtitle");
            float f3 = (float) this.titlesTimer - partialTicks;
            int i2 = 255;

            if (this.titlesTimer > this.titleFadeOut + this.titleDisplayTime) {
                float f4 = (float) (this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut) - f3;
                i2 = (int) (f4 * 255.0F / (float) this.titleFadeIn);
            }

            if (this.titlesTimer <= this.titleFadeOut) {
                i2 = (int) (f3 * 255.0F / (float) this.titleFadeOut);
            }

            i2 = MathHelper.clamp(i2, 0, 255);

            if (i2 > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                int j2 = i2 << 24 & -16777216;
                fontrenderer.drawString(this.displayedTitle, (float) (-fontrenderer.getStringWidth(this.displayedTitle) / 2), -10.0F, 16777215 | j2, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                fontrenderer.drawString(this.displayedSubTitle, (float) (-fontrenderer.getStringWidth(this.displayedSubTitle) / 2), 5.0F, 16777215 | j2, true);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }

        Scoreboard scoreboard = ((iMinecraft) (this.mc)).MikuWorld().getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(((iMinecraft) this.mc).MikuPlayer().getName());

        if (scoreplayerteam != null) {
            int i1 = scoreplayerteam.getColor().getColorIndex();

            if (i1 >= 0) {
                scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
            }
        }

        ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null) {
            this.renderScoreboard(scoreobjective1, scaledresolution);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, (float) (j - 48), 0.0F);
        ((iMinecraft) this.mc).MikuProfiler().startSection("chat");
        this.persistantChatGUI.drawChat(this.updateCounter);
        ((iMinecraft) this.mc).MikuProfiler().endSection();
        GlStateManager.popMatrix();
        scoreobjective1 = scoreboard.getObjectiveInDisplaySlot(0);

        if (!this.mc.gameSettings.keyBindPlayerList.isKeyDown() || this.mc.isIntegratedServerRunning() && ((iMinecraft) this.mc).MikuPlayer().connection.getPlayerInfoMap().size() <= 1 && scoreobjective1 == null) {
            this.overlayPlayerList.updatePlayerList(false);
        } else {
            this.overlayPlayerList.updatePlayerList(true);
            this.overlayPlayerList.renderPlayerlist(i, scoreboard, scoreobjective1);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
    }

    /**
     * @author mcst12345
     * @reason sh
     */
    @Overwrite
    public void updateTick() {
        if (TimeStopUtil.isTimeStop() || ((iMinecraft) this.mc).isTimeStop()) return;
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }

        if (this.titlesTimer > 0) {
            --this.titlesTimer;

            if (this.titlesTimer <= 0) {
                this.displayedTitle = "";
                this.displayedSubTitle = "";
            }
        }

        ++this.updateCounter;

        if (((iMinecraft) this.mc).MikuPlayer() != null) {
            ItemStack itemstack = ((iMinecraft) this.mc).MikuPlayer().inventory.getCurrentItem();

            if (itemstack.isEmpty()) {
                this.remainingHighlightTicks = 0;
            } else if (!this.highlightingItemStack.isEmpty() && itemstack.getItem() == this.highlightingItemStack.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.highlightingItemStack) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.highlightingItemStack.getMetadata())) {
                if (this.remainingHighlightTicks > 0) {
                    --this.remainingHighlightTicks;
                }
            } else {
                this.remainingHighlightTicks = 40;
            }

            this.highlightingItemStack = itemstack;
        }
    }
}
