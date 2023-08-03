package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.util.EntityUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.FoodStats;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
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
    protected abstract void renderVignette(float lightLevel, ScaledResolution scaledRes);

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
    protected abstract void renderAttackIndicator(float partialTicks, ScaledResolution p_184045_2_);

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
    protected long healthUpdateCounter;

    @Shadow
    protected int playerHealth;

    @Shadow
    protected long lastSystemTime;

    @Shadow
    protected int lastPlayerHealth;

    @Shadow
    @Final
    protected Random rand;

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
            this.renderVignette(this.mc.player.getBrightness(), scaledresolution);
        } else {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        ItemStack itemstack = this.mc.player.inventory.armorItemInSlot(3);

        if (!EntityUtil.isProtected(this.mc.player))
            if (this.mc.gameSettings.thirdPersonView == 0 && itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
                this.renderPumpkinOverlay(scaledresolution);
            }

        if (!this.mc.player.isPotionActive(MobEffects.NAUSEA)) {
            float f = this.mc.player.prevTimeInPortal + (this.mc.player.timeInPortal - this.mc.player.prevTimeInPortal) * partialTicks;

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

        if (this.mc.player.getSleepTimer() > 0) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("sleep");
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            int j1 = this.mc.player.getSleepTimer();
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

        if (this.mc.player.isRidingHorse()) {
            this.renderHorseJumpBar(scaledresolution, k1);
        } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
            this.renderExpBar(scaledresolution, k1);
        }

        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator()) {
            this.renderSelectedItem(scaledresolution);
        } else if (this.mc.player.isSpectator()) {
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

        Scoreboard scoreboard = this.mc.world.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.player.getName());

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

        if (!this.mc.gameSettings.keyBindPlayerList.isKeyDown() || this.mc.isIntegratedServerRunning() && this.mc.player.connection.getPlayerInfoMap().size() <= 1 && scoreobjective1 == null) {
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
     * @reason FUCK
     */
    @Overwrite
    public void renderHorseJumpBar(ScaledResolution scaledRes, int x) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("jumpBar");
        this.mc.getTextureManager().bindTexture(Gui.ICONS);
        float f = this.mc.player.getHorseJumpPower();
        int j = (int) (f * 183.0F);
        int k = scaledRes.getScaledHeight() - 32 + 3;
        this.drawTexturedModalRect(x, k, 0, 84, 182, 5);

        if (j > 0) {
            this.drawTexturedModalRect(x, k, 0, 89, j, 5);
        }

        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderExpBar(ScaledResolution scaledRes, int x) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("expBar");
        this.mc.getTextureManager().bindTexture(Gui.ICONS);
        int i = this.mc.player.xpBarCap();

        if (i > 0) {
            int k = (int) (this.mc.player.experience * 183.0F);
            int l = scaledRes.getScaledHeight() - 32 + 3;
            this.drawTexturedModalRect(x, l, 0, 64, 182, 5);

            if (k > 0) {
                this.drawTexturedModalRect(x, l, 0, 69, k, 5);
            }
        }

        ((iMinecraft) this.mc).MikuProfiler().endSection();

        if (this.mc.player.experienceLevel > 0) {
            ((iMinecraft) this.mc).MikuProfiler().startSection("expLevel");
            String s = String.valueOf(this.mc.player.experienceLevel);
            int i1 = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int j1 = scaledRes.getScaledHeight() - 31 - 4;
            this.getFontRenderer().drawString(s, i1 + 1, j1, 0);
            this.getFontRenderer().drawString(s, i1 - 1, j1, 0);
            this.getFontRenderer().drawString(s, i1, j1 + 1, 0);
            this.getFontRenderer().drawString(s, i1, j1 - 1, 0);
            this.getFontRenderer().drawString(s, i1, j1, 8453920);
            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderSelectedItem(ScaledResolution scaledRes) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("selectedItemName");

        if (this.remainingHighlightTicks > 0 && !this.highlightingItemStack.isEmpty()) {
            String s = this.highlightingItemStack.getDisplayName();

            if (this.highlightingItemStack.hasDisplayName()) {
                s = TextFormatting.ITALIC + s;
            }

            int i = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int j = scaledRes.getScaledHeight() - 59;

            if (!this.mc.playerController.shouldDrawHUD()) {
                j += 14;
            }

            int k = (int) ((float) this.remainingHighlightTicks * 256.0F / 10.0F);

            if (k > 255) {
                k = 255;
            }

            if (k > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                this.getFontRenderer().drawStringWithShadow(s, (float) i, (float) j, 16777215 + (k << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason SHIT
     */
    @Overwrite
    public void renderDemo(ScaledResolution scaledRes) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("demo");
        String s;

        if (this.mc.world.getTotalWorldTime() >= 120500L) {
            s = I18n.format("demo.demoExpired");
        } else {
            s = I18n.format("demo.remainingTime", StringUtils.ticksToElapsedTime((int) (120500L - this.mc.world.getTotalWorldTime())));
        }

        int i = this.getFontRenderer().getStringWidth(s);
        this.getFontRenderer().drawStringWithShadow(s, (float) (scaledRes.getScaledWidth() - i - 10), 5.0F, 16777215);
        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderPlayerStats(ScaledResolution scaledRes) {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            int i = MathHelper.ceil(entityplayer.getHealth());
            boolean flag = this.healthUpdateCounter > (long) this.updateCounter && (this.healthUpdateCounter - (long) this.updateCounter) / 3L % 2L == 1L;

            if (i < this.playerHealth && entityplayer.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = this.updateCounter + 20;
            } else if (i > this.playerHealth && entityplayer.hurtResistantTime > 0) {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = this.updateCounter + 10;
            }

            if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
                this.playerHealth = i;
                this.lastPlayerHealth = i;
                this.lastSystemTime = Minecraft.getSystemTime();
            }

            this.playerHealth = i;
            int j = this.lastPlayerHealth;
            this.rand.setSeed(this.updateCounter * 312871L);
            FoodStats foodstats = entityplayer.getFoodStats();
            int k = foodstats.getFoodLevel();
            IAttributeInstance iattributeinstance = entityplayer.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            int l = scaledRes.getScaledWidth() / 2 - 91;
            int i1 = scaledRes.getScaledWidth() / 2 + 91;
            int j1 = scaledRes.getScaledHeight() - 39;
            float f = (float) iattributeinstance.getAttributeValue();
            int k1 = MathHelper.ceil(entityplayer.getAbsorptionAmount());
            int l1 = MathHelper.ceil((f + (float) k1) / 2.0F / 10.0F);
            int i2 = Math.max(10 - (l1 - 2), 3);
            int j2 = j1 - (l1 - 1) * i2 - 10;
            int k2 = j1 - 10;
            int l2 = k1;
            int i3 = entityplayer.getTotalArmorValue();
            int j3 = -1;

            if (entityplayer.isPotionActive(MobEffects.REGENERATION)) {
                j3 = this.updateCounter % MathHelper.ceil(f + 5.0F);
            }

            ((iMinecraft) this.mc).MikuProfiler().startSection("armor");

            for (int k3 = 0; k3 < 10; ++k3) {
                if (i3 > 0) {
                    int l3 = l + k3 * 8;

                    if (k3 * 2 + 1 < i3) {
                        this.drawTexturedModalRect(l3, j2, 34, 9, 9, 9);
                    }

                    if (k3 * 2 + 1 == i3) {
                        this.drawTexturedModalRect(l3, j2, 25, 9, 9, 9);
                    }

                    if (k3 * 2 + 1 > i3) {
                        this.drawTexturedModalRect(l3, j2, 16, 9, 9, 9);
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endStartSection("health");

            for (int j5 = MathHelper.ceil((f + (float) k1) / 2.0F) - 1; j5 >= 0; --j5) {
                int k5 = 16;

                if (entityplayer.isPotionActive(MobEffects.POISON)) {
                    k5 += 36;
                } else if (entityplayer.isPotionActive(MobEffects.WITHER)) {
                    k5 += 72;
                }

                int i4 = 0;

                if (flag) {
                    i4 = 1;
                }

                int j4 = MathHelper.ceil((float) (j5 + 1) / 10.0F) - 1;
                int k4 = l + j5 % 10 * 8;
                int l4 = j1 - j4 * i2;

                if (i <= 4) {
                    l4 += this.rand.nextInt(2);
                }

                if (l2 <= 0 && j5 == j3) {
                    l4 -= 2;
                }

                int i5 = 0;

                if (entityplayer.world.getWorldInfo().isHardcoreModeEnabled()) {
                    i5 = 5;
                }

                this.drawTexturedModalRect(k4, l4, 16 + i4 * 9, 9 * i5, 9, 9);

                if (flag) {
                    if (j5 * 2 + 1 < j) {
                        this.drawTexturedModalRect(k4, l4, k5 + 54, 9 * i5, 9, 9);
                    }

                    if (j5 * 2 + 1 == j) {
                        this.drawTexturedModalRect(k4, l4, k5 + 63, 9 * i5, 9, 9);
                    }
                }

                if (l2 > 0) {
                    if (l2 == k1 && k1 % 2 == 1) {
                        this.drawTexturedModalRect(k4, l4, k5 + 153, 9 * i5, 9, 9);
                        --l2;
                    } else {
                        this.drawTexturedModalRect(k4, l4, k5 + 144, 9 * i5, 9, 9);
                        l2 -= 2;
                    }
                } else {
                    if (j5 * 2 + 1 < i) {
                        this.drawTexturedModalRect(k4, l4, k5 + 36, 9 * i5, 9, 9);
                    }

                    if (j5 * 2 + 1 == i) {
                        this.drawTexturedModalRect(k4, l4, k5 + 45, 9 * i5, 9, 9);
                    }
                }
            }

            Entity entity = entityplayer.getRidingEntity();

            if (!(entity instanceof EntityLivingBase)) {
                ((iMinecraft) this.mc).MikuProfiler().endStartSection("food");

                for (int l5 = 0; l5 < 10; ++l5) {
                    int j6 = j1;
                    int l6 = 16;
                    int j7 = 0;

                    if (entityplayer.isPotionActive(MobEffects.HUNGER)) {
                        l6 += 36;
                        j7 = 13;
                    }

                    if (entityplayer.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (k * 3 + 1) == 0) {
                        j6 = j1 + (this.rand.nextInt(3) - 1);
                    }

                    int l7 = i1 - l5 * 8 - 9;
                    this.drawTexturedModalRect(l7, j6, 16 + j7 * 9, 27, 9, 9);

                    if (l5 * 2 + 1 < k) {
                        this.drawTexturedModalRect(l7, j6, l6 + 36, 27, 9, 9);
                    }

                    if (l5 * 2 + 1 == k) {
                        this.drawTexturedModalRect(l7, j6, l6 + 45, 27, 9, 9);
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endStartSection("air");

            if (entityplayer.isInsideOfMaterial(Material.WATER)) {
                int i6 = this.mc.player.getAir();
                int k6 = MathHelper.ceil((double) (i6 - 2) * 10.0D / 300.0D);
                int i7 = MathHelper.ceil((double) i6 * 10.0D / 300.0D) - k6;

                for (int k7 = 0; k7 < k6 + i7; ++k7) {
                    if (k7 < k6) {
                        this.drawTexturedModalRect(i1 - k7 * 8 - 9, k2, 16, 18, 9, 9);
                    } else {
                        this.drawTexturedModalRect(i1 - k7 * 8 - 9, k2, 25, 18, 9, 9);
                    }
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderMountHealth(ScaledResolution p_184047_1_) {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            Entity entity = entityplayer.getRidingEntity();

            if (entity instanceof EntityLivingBase) {
                ((iMinecraft) this.mc).MikuProfiler().endStartSection("mountHealth");
                EntityLivingBase entitylivingbase = (EntityLivingBase) entity;
                int i = (int) Math.ceil(entitylivingbase.getHealth());
                float f = entitylivingbase.getMaxHealth();
                int j = (int) (f + 0.5F) / 2;

                if (j > 30) {
                    j = 30;
                }

                int k = p_184047_1_.getScaledHeight() - 39;
                int l = p_184047_1_.getScaledWidth() / 2 + 91;
                int i1 = k;
                int j1 = 0;

                for (; j > 0; j1 += 20) {
                    int k1 = Math.min(j, 10);
                    j -= k1;

                    for (int l1 = 0; l1 < k1; ++l1) {
                        int k2 = l - l1 * 8 - 9;
                        this.drawTexturedModalRect(k2, i1, 52, 9, 9, 9);

                        if (l1 * 2 + 1 + j1 < i) {
                            this.drawTexturedModalRect(k2, i1, 88, 9, 9, 9);
                        }

                        if (l1 * 2 + 1 + j1 == i) {
                            this.drawTexturedModalRect(k2, i1, 97, 9, 9, 9);
                        }
                    }

                    i1 -= 10;
                }
            }
        }
    }
}