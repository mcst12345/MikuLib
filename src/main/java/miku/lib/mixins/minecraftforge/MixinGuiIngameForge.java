package miku.lib.mixins.minecraftforge;

import miku.lib.client.api.iMinecraft;
import miku.lib.client.util.GuiOverlayDebugForge;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.*;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge extends GuiIngame {
    @Shadow
    private RenderGameOverlayEvent eventParent;

    public MixinGuiIngameForge(Minecraft mcIn) {
        super(mcIn);
    }

    @Shadow
    protected abstract void bind(ResourceLocation res);

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    private void post(RenderGameOverlayEvent.ElementType type) {
        try {
            MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at RenderGameOverlayEvent." + type.name());
            t.printStackTrace();
        }
    }

    @Shadow
    public static int left_height;

    @Shadow
    public static int right_height;

    @Shadow
    private FontRenderer fontrenderer;

    @Shadow
    @Final
    private static int WHITE;

    @Shadow
    private ScaledResolution res;
    @Shadow
    public static boolean renderHealthMount;
    @Shadow
    public static boolean renderFood;
    @Shadow
    public static boolean renderJumpBar;
    @Shadow
    public static boolean renderVignette;
    @Shadow
    public static boolean renderHelmet;

    @Shadow
    protected abstract void renderHelmet(ScaledResolution res, float partialTicks);

    @Shadow
    public static boolean renderPortal;

    @Shadow
    protected abstract void renderPortal(ScaledResolution res, float partialTicks);

    @Shadow
    public static boolean renderHotbar;
    @Shadow
    public static boolean renderCrosshairs;

    @Shadow
    protected abstract void renderCrosshairs(float partialTicks);

    @Shadow
    public static boolean renderBossHealth;
    @Shadow
    public static boolean renderHealth;
    @Shadow
    public static boolean renderArmor;
    @Shadow
    public static boolean renderAir;
    @Shadow
    public static boolean renderExperiance;

    @Shadow
    protected abstract void renderPotionIcons(ScaledResolution resolution);

    @Shadow
    protected abstract void renderSubtitles(ScaledResolution resolution);

    @Shadow
    public static boolean renderObjective;

    @Shadow
    protected abstract void renderPlayerList(int width, int height);

    private GuiOverlayDebugForge DebugOverlay;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Minecraft mc, CallbackInfo ci) {
        DebugOverlay = new GuiOverlayDebugForge(mc);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private boolean pre(RenderGameOverlayEvent.ElementType type) {
        boolean flag = false;
        try {
            flag = MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
        } catch (Throwable ignored) {
        }
        return flag;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderBossHealth() {
        if (pre(BOSSHEALTH)) return;
        bind(Gui.ICONS);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        ((iMinecraft) mc).MikuProfiler().startSection("bossHealth");
        GlStateManager.enableBlend();
        this.overlayBoss.renderBossHealth();
        GlStateManager.disableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        post(BOSSHEALTH);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderArmor(int width, int height) {
        if (pre(ARMOR)) return;
        ((iMinecraft) mc).MikuProfiler().startSection("armor");

        GlStateManager.enableBlend();
        int left = width / 2 - 91;
        int top = height - left_height;

        int level = ForgeHooks.getTotalArmorValue(mc.player);
        for (int i = 1; level > 0 && i < 20; i += 2) {
            if (i < level) {
                drawTexturedModalRect(left, top, 34, 9, 9, 9);
            } else if (i == level) {
                drawTexturedModalRect(left, top, 25, 9, 9, 9);
            } else if (i > level) {
                drawTexturedModalRect(left, top, 16, 9, 9, 9);
            }
            left += 8;
        }
        left_height += 10;

        GlStateManager.disableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        post(ARMOR);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderAir(int width, int height) {
        if (pre(AIR)) return;
        ((iMinecraft) mc).MikuProfiler().startSection("air");
        EntityPlayer player = (EntityPlayer) this.mc.getRenderViewEntity();
        GlStateManager.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;

        assert player != null;
        if (player.isInsideOfMaterial(Material.WATER)) {
            int air = player.getAir();
            int full = MathHelper.ceil((double) (air - 2) * 10.0D / 300.0D);
            int partial = MathHelper.ceil((double) air * 10.0D / 300.0D) - full;

            for (int i = 0; i < full + partial; ++i) {
                drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
            }
            right_height += 10;
        }

        GlStateManager.disableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        post(AIR);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void renderHealth(int width, int height) {
        bind(ICONS);
        if (pre(HEALTH)) return;
        ((iMinecraft) mc).MikuProfiler().startSection("health");
        GlStateManager.enableBlend();

        EntityPlayer player = (EntityPlayer) this.mc.getRenderViewEntity();
        assert player != null;
        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

        if (health < this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = this.updateCounter + 20;
        } else if (health > this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = this.updateCounter + 10;
        }

        if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
            this.playerHealth = health;
            this.lastPlayerHealth = health;
            this.lastSystemTime = Minecraft.getSystemTime();
        }

        this.playerHealth = health;
        int healthLast = this.lastPlayerHealth;

        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float) attrMaxHealth.getAttributeValue();
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed(updateCounter * 312871L);

        int left = width / 2 - 91;
        int top = height - left_height;
        left_height += (healthRows * rowHeight);
        if (rowHeight != 10) left_height += 10 - rowHeight;

        int regen = -1;
        if (player.isPotionActive(MobEffects.REGENERATION)) {
            regen = updateCounter % 25;
        }

        final int TOP = 9 * (mc.world.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
        final int BACKGROUND = (highlight ? 25 : 16);
        int MARGIN = 16;
        if (player.isPotionActive(MobEffects.POISON)) MARGIN += 36;
        else if (player.isPotionActive(MobEffects.WITHER)) MARGIN += 72;
        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += rand.nextInt(2);
            if (i == regen) y -= 2;

            drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
                else if (i * 2 + 1 == healthLast)
                    drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                } else {
                    drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            } else {
                if (i * 2 + 1 < health)
                    drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
                else if (i * 2 + 1 == health)
                    drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
            }
        }

        GlStateManager.disableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        post(HEALTH);
    }

    /**
     * @author mcst12345
     * @reason Shit
     */
    @Overwrite
    public void renderFood(int width, int height) {
        if (pre(FOOD)) return;
        ((iMinecraft) mc).MikuProfiler().startSection("food");

        EntityPlayer player = (EntityPlayer) this.mc.getRenderViewEntity();
        GlStateManager.enableBlend();
        int left = width / 2 + 91;
        int top = height - right_height;
        right_height += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        FoodStats stats = mc.player.getFoodStats();
        int level = stats.getFoodLevel();

        for (int i = 0; i < 10; ++i) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            int icon = 16;
            byte background = 0;

            if (mc.player.isPotionActive(MobEffects.HUNGER)) {
                icon += 36;
                background = 13;
            }
            if (unused) background = 1; //Probably should be a += 1 but vanilla never uses this

            assert player != null;
            if (player.getFoodStats().getSaturationLevel() <= 0.0F && updateCounter % (level * 3 + 1) == 0) {
                y = top + (rand.nextInt(3) - 1);
            }

            drawTexturedModalRect(x, y, 16 + background * 9, 27, 9, 9);

            if (idx < level)
                drawTexturedModalRect(x, y, icon + 36, 27, 9, 9);
            else if (idx == level)
                drawTexturedModalRect(x, y, icon + 45, 27, 9, 9);
        }
        GlStateManager.disableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        post(FOOD);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderSleepFade(int width, int height) {
        if (mc.player.getSleepTimer() > 0) {
            ((iMinecraft) mc).MikuProfiler().startSection("sleep");
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            int sleepTime = mc.player.getSleepTimer();
            float opacity = (float) sleepTime / 100.0F;

            if (opacity > 1.0F) {
                opacity = 1.0F - (float) (sleepTime - 100) / 10.0F;
            }

            int color = (int) (220.0F * opacity) << 24 | 1052704;
            drawRect(0, 0, width, height, color);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            ((iMinecraft) mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    protected void renderExperience(int width, int height) {
        bind(ICONS);
        if (pre(EXPERIENCE)) return;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();

        if (mc.playerController.gameIsSurvivalOrAdventure()) {
            ((iMinecraft) mc).MikuProfiler().startSection("expBar");
            int cap = this.mc.player.xpBarCap();
            int left = width / 2 - 91;

            if (cap > 0) {
                short barWidth = 182;
                int filled = (int) (mc.player.experience * (float) (barWidth + 1));
                int top = height - 32 + 3;
                drawTexturedModalRect(left, top, 0, 64, barWidth, 5);

                if (filled > 0) {
                    drawTexturedModalRect(left, top, 0, 69, filled, 5);
                }
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();


            if (mc.playerController.gameIsSurvivalOrAdventure() && mc.player.experienceLevel > 0) {
                ((iMinecraft) mc).MikuProfiler().startSection("expLevel");
                int color = 8453920;
                String text = String.valueOf(mc.player.experienceLevel);
                int x = (width - fontrenderer.getStringWidth(text)) / 2;
                int y = height - 31 - 4;
                fontrenderer.drawString(text, x + 1, y, 0);
                fontrenderer.drawString(text, x - 1, y, 0);
                fontrenderer.drawString(text, x, y + 1, 0);
                fontrenderer.drawString(text, x, y - 1, 0);
                fontrenderer.drawString(text, x, y, color);
                ((iMinecraft) mc).MikuProfiler().endSection();
            }
        }
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        post(EXPERIENCE);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderJumpBar(int width, int height) {
        bind(ICONS);
        if (pre(JUMPBAR)) return;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();

        ((iMinecraft) mc).MikuProfiler().startSection("jumpBar");
        float charge = mc.player.getHorseJumpPower();
        final int barWidth = 182;
        int x = (width / 2) - (barWidth / 2);
        int filled = (int) (charge * (float) (barWidth + 1));
        int top = height - 32 + 3;

        drawTexturedModalRect(x, top, 0, 84, barWidth, 5);

        if (filled > 0) {
            this.drawTexturedModalRect(x, top, 0, 89, filled, 5);
        }

        GlStateManager.enableBlend();
        ((iMinecraft) mc).MikuProfiler().endSection();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        post(JUMPBAR);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderToolHighlight(ScaledResolution res) {
        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator()) {
            ((iMinecraft) mc).MikuProfiler().startSection("toolHighlight");

            if (this.remainingHighlightTicks > 0 && !this.highlightingItemStack.isEmpty()) {
                String name = this.highlightingItemStack.getDisplayName();
                if (this.highlightingItemStack.hasDisplayName())
                    name = TextFormatting.ITALIC + name;

                name = this.highlightingItemStack.getItem().getHighlightTip(this.highlightingItemStack, name);

                int opacity = (int) ((float) this.remainingHighlightTicks * 256.0F / 10.0F);
                if (opacity > 255) opacity = 255;

                if (opacity > 0) {
                    int y = res.getScaledHeight() - 59;
                    if (!mc.playerController.shouldDrawHUD()) y += 14;

                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    FontRenderer font = highlightingItemStack.getItem().getFontRenderer(highlightingItemStack);
                    if (font != null) {
                        int x = (res.getScaledWidth() - font.getStringWidth(name)) / 2;
                        font.drawStringWithShadow(name, x, y, WHITE | (opacity << 24));
                    } else {
                        int x = (res.getScaledWidth() - fontrenderer.getStringWidth(name)) / 2;
                        fontrenderer.drawStringWithShadow(name, x, y, WHITE | (opacity << 24));
                    }
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }

            ((iMinecraft) mc).MikuProfiler().endSection();
        } else if (this.mc.player.isSpectator()) {
            this.spectatorGui.renderSelectedItem(res);
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderHUDText(int width, int height) {
        ((iMinecraft) mc).MikuProfiler().startSection("forgeHudText");
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        ArrayList<String> listL = new ArrayList<>();
        ArrayList<String> listR = new ArrayList<>();

        if (mc.isDemo()) {
            long time = mc.world.getTotalWorldTime();
            if (time >= 120500L) {
                listR.add(I18n.format("demo.demoExpired"));
            } else {
                listR.add(I18n.format("demo.remainingTime", StringUtils.ticksToElapsedTime((int) (120500L - time))));
            }
        }

        if (this.mc.gameSettings.showDebugInfo && !pre(DEBUG)) {
            listL.addAll(DebugOverlay.getLeft());
            listR.addAll(DebugOverlay.getRight());
            post(DEBUG);
        }

        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(eventParent, listL, listR);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            int top = 2;
            for (String msg : listL) {
                if (msg == null) continue;
                drawRect(1, top - 1, 2 + fontrenderer.getStringWidth(msg) + 1, top + fontrenderer.FONT_HEIGHT - 1, -1873784752);
                fontrenderer.drawString(msg, 2, top, 14737632);
                top += fontrenderer.FONT_HEIGHT;
            }

            top = 2;
            for (String msg : listR) {
                if (msg == null) continue;
                int w = fontrenderer.getStringWidth(msg);
                int left = width - 2 - w;
                drawRect(left - 1, top - 1, left + w + 1, top + fontrenderer.FONT_HEIGHT - 1, -1873784752);
                fontrenderer.drawString(msg, left, top, 14737632);
                top += fontrenderer.FONT_HEIGHT;
            }
        }

        ((iMinecraft) mc).MikuProfiler().endSection();
        post(TEXT);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void renderFPSGraph() {
        if (this.mc.gameSettings.showDebugInfo && this.mc.gameSettings.showLagometer && !pre(FPS_GRAPH)) {
            this.DebugOverlay.renderLagometer();
            post(FPS_GRAPH);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    protected void renderRecordOverlay(int width, int height, float partialTicks) {
        if (overlayMessageTime > 0) {
            ((iMinecraft) mc).MikuProfiler().startSection("overlayMessage");
            float hue = (float) overlayMessageTime - partialTicks;
            int opacity = (int) (hue * 256.0F / 20.0F);
            if (opacity > 255) opacity = 255;

            if (opacity > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (width / 2), (float) (height - 68), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int color = (animateOverlayMessageColor ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE);
                fontrenderer.drawString(overlayMessage, -fontrenderer.getStringWidth(overlayMessage) / 2, -4, color | (opacity << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            ((iMinecraft) mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason Shit Fuck
     */
    @Overwrite
    protected void renderTitle(int width, int height, float partialTicks) {
        if (titlesTimer > 0) {
            ((iMinecraft) mc).MikuProfiler().startSection("titleAndSubtitle");
            float age = (float) this.titlesTimer - partialTicks;
            int opacity = 255;

            if (titlesTimer > titleFadeOut + titleDisplayTime) {
                float f3 = (float) (titleFadeIn + titleDisplayTime + titleFadeOut) - age;
                opacity = (int) (f3 * 255.0F / (float) titleFadeIn);
            }
            if (titlesTimer <= titleFadeOut) opacity = (int) (age * 255.0F / (float) this.titleFadeOut);

            opacity = MathHelper.clamp(opacity, 0, 255);

            if (opacity > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (width / 2), (float) (height / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                int l = opacity << 24 & -16777216;
                this.getFontRenderer().drawString(this.displayedTitle, (float) (-this.getFontRenderer().getStringWidth(this.displayedTitle) / 2), -10.0F, 16777215 | l, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                this.getFontRenderer().drawString(this.displayedSubTitle, (float) (-this.getFontRenderer().getStringWidth(this.displayedSubTitle) / 2), 5.0F, 16777215 | l, true);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            ((iMinecraft) this.mc).MikuProfiler().endSection();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    protected void renderChat(int width, int height) {
        ((iMinecraft) this.mc).MikuProfiler().startSection("chat");

        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(eventParent, 0, height - 48);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) event.getPosX(), (float) event.getPosY(), 0.0F);
        persistantChatGUI.drawChat(updateCounter);
        GlStateManager.popMatrix();

        post(CHAT);

        ((iMinecraft) this.mc).MikuProfiler().endSection();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    protected void renderHealthMount(int width, int height) {
        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        Entity tmp = player.getRidingEntity();
        if (!(tmp instanceof EntityLivingBase)) return;

        bind(ICONS);

        if (pre(HEALTHMOUNT)) return;

        boolean unused = false;
        int left_align = width / 2 + 91;

        ((iMinecraft) this.mc).MikuProfiler().endStartSection("mountHealth");
        GlStateManager.enableBlend();
        EntityLivingBase mount = (EntityLivingBase) tmp;
        int health = (int) Math.ceil(mount.getHealth());
        float healthMax = mount.getMaxHealth();
        int hearts = (int) (healthMax + 0.5F) / 2;

        if (hearts > 30) hearts = 30;

        final int MARGIN = 52;
        final int BACKGROUND = MARGIN + (unused ? 1 : 0);
        final int HALF = MARGIN + 45;
        final int FULL = MARGIN + 36;

        for (int heart = 0; hearts > 0; heart += 20) {
            int top = height - right_height;

            int rowCount = Math.min(hearts, 10);
            hearts -= rowCount;

            for (int i = 0; i < rowCount; ++i) {
                int x = left_align - i * 8 - 9;
                drawTexturedModalRect(x, top, BACKGROUND, 9, 9, 9);

                if (i * 2 + 1 + heart < health)
                    drawTexturedModalRect(x, top, FULL, 9, 9, 9);
                else if (i * 2 + 1 + heart == health)
                    drawTexturedModalRect(x, top, HALF, 9, 9, 9);
            }

            right_height += 10;
        }
        GlStateManager.disableBlend();
        post(HEALTHMOUNT);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Override
    public void renderGameOverlay(float partialTicks) {
        res = new ScaledResolution(mc);
        eventParent = new RenderGameOverlayEvent(partialTicks, res);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        renderHealthMount = mc.player.getRidingEntity() instanceof EntityLivingBase;
        renderFood = mc.player.getRidingEntity() == null;
        renderJumpBar = mc.player.isRidingHorse();

        right_height = 39;
        left_height = 39;

        if (pre(ALL)) return;

        fontrenderer = mc.fontRenderer;
        ((iMinecraft) mc).MikuEntityRenderer().setupOverlayRendering();
        GlStateManager.enableBlend();

        if (renderVignette && Minecraft.isFancyGraphicsEnabled()) {
            renderVignette(mc.player.getBrightness(), res);
        } else {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        if (renderHelmet) renderHelmet(res, partialTicks);

        if (renderPortal && !mc.player.isPotionActive(MobEffects.NAUSEA)) {
            renderPortal(res, partialTicks);
        }

        if (renderHotbar) renderHotbar(res, partialTicks);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        zLevel = -90.0F;
        rand.setSeed(updateCounter * 312871L);

        if (renderCrosshairs) renderCrosshairs(partialTicks);
        if (renderBossHealth) renderBossHealth();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.mc.playerController.shouldDrawHUD() && this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            if (renderHealth) renderHealth(width, height);
            if (renderArmor) renderArmor(width, height);
            if (renderFood) renderFood(width, height);
            if (renderHealthMount) renderHealthMount(width, height);
            if (renderAir) renderAir(width, height);
        }

        renderSleepFade(width, height);

        if (renderJumpBar) {
            renderJumpBar(width, height);
        } else if (renderExperiance) {
            renderExperience(width, height);
        }

        renderToolHighlight(res);
        renderHUDText(width, height);
        renderFPSGraph();
        renderPotionIcons(res);
        renderRecordOverlay(width, height, partialTicks);
        renderSubtitles(res);
        renderTitle(width, height, partialTicks);


        Scoreboard scoreboard = this.mc.world.getScoreboard();
        ScoreObjective objective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.player.getName());
        if (scoreplayerteam != null) {
            int slot = scoreplayerteam.getColor().getColorIndex();
            if (slot >= 0) objective = scoreboard.getObjectiveInDisplaySlot(3 + slot);
        }
        ScoreObjective scoreobjective1 = objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);
        if (renderObjective && scoreobjective1 != null) {
            this.renderScoreboard(scoreobjective1, res);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableAlpha();

        renderChat(width, height);

        renderPlayerList(width, height);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();

        post(ALL);
    }
}
