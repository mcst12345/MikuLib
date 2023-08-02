package miku.lib.mixins.minecraft;

import miku.lib.client.gui.TheGui;
import miku.lib.common.api.iMinecraft;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.crashReportUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;

import static miku.lib.common.sqlite.Sqlite.DEBUG;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft implements iMinecraft {
    protected String LastPrint;
    public void Stop(){
        running=false;
    }
    public MixinMinecraft(){}
    protected boolean protect = false;

    public void SET_INGAME_FOCUS(){
        if (Display.isActive())
        {
            if (!this.inGameHasFocus)
            {
                if (!IS_RUNNING_ON_MAC)
                {
                    KeyBinding.updateKeyBindState();
                }

                this.inGameHasFocus = true;
                this.mouseHelper.grabMouseCursor();
                this.displayGuiScreen(null);
                this.leftClickCounter = 10000;
            }
        }
    }

    public void SET_INGAME_NOT_FOCUS(){
        if (this.inGameHasFocus)
        {
            this.inGameHasFocus = false;
            this.mouseHelper.ungrabMouseCursor();
        }
    }


    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void setIngameFocus(){}

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void setIngameNotInFocus(){}

    @Override
    public boolean protect(){
        return protect;
    }

    @Override
    public void SetProtected(){
        protect=true;
    }

    @Override
    public void SetTimeStop(){
        TimeStop=!TimeStop;
    }

    @Override
    public boolean isTimeStop(){
        return TimeStop;
    }

    protected boolean TimeStop=false;

    @Shadow public EntityPlayerSP player;

    @Shadow private ModelManager modelManager;

    @Shadow private int rightClickDelayTimer;

    @Shadow @Final public Profiler profiler;

    @Shadow private boolean isGamePaused;

    @Shadow public GuiIngame ingameGUI;

    @Shadow public EntityRenderer entityRenderer;

    @Shadow @Final private Tutorial tutorial;

    @Shadow public WorldClient world;

    @Shadow public RayTraceResult objectMouseOver;

    @Shadow public PlayerControllerMP playerController;

    @Shadow public TextureManager renderEngine;

    @Shadow @Nullable public GuiScreen currentScreen;


    @Shadow private int leftClickCounter;

    @Shadow protected abstract void runTickKeyboard();

    @Shadow private int joinPlayerCounter;

    @Shadow public RenderGlobal renderGlobal;

    @Shadow private MusicTicker musicTicker;

    @Shadow private SoundHandler soundHandler;

    @Shadow public ParticleManager effectRenderer;

    @Shadow
    @Final
    public static boolean IS_RUNNING_ON_MAC;

    @Shadow
    long systemTime;

    @Shadow
    public static long getSystemTime() {
        return 0;
    }
    @Shadow
    private static Minecraft instance;
    @Shadow
    public GameSettings gameSettings;

    @Shadow
    public boolean skipRenderWorld;


    @Shadow
    volatile boolean running;

    @Shadow public boolean inGameHasFocus;

    @Shadow public MouseHelper mouseHelper;
    @Shadow
    @Nullable
    private NetworkManager networkManager;

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static Minecraft getMinecraft() {
        //if(MikuMinecraft.Miku)return MikuMinecraft.getMinecraft();
        return instance;
    }

    @Shadow
    protected abstract void init();

    @Shadow
    protected abstract void runGameLoop();

    @Shadow
    public abstract void freeMemory();

    @Shadow
    public abstract void shutdownMinecraftApplet();

    /**
     * @author mcst12345
     * @reason F**k
     */
    @Overwrite
    public void displayGuiScreen(@Nullable GuiScreen guiScreenIn) {
        if (Sqlite.IS_GUI_BANNED(guiScreenIn)) {
            if (DEBUG()) System.out.println(guiScreenIn.getClass().toString() + " is banned");
            guiScreenIn.onGuiClosed();
            return;
        }
        if(EntityUtil.isProtected(player)){
            if (guiScreenIn instanceof GuiGameOver) {
                guiScreenIn.onGuiClosed();
                return;
            }
            if (guiScreenIn != null) {
                if (guiScreenIn.toString() != null) {
                    if (guiScreenIn.toString().toLowerCase().contains("dead") || guiScreenIn.toString().toLowerCase().contains("over")) {
                        return;
                    }
                }
            }
        }

        if(currentScreen instanceof TheGui){
            if(!(guiScreenIn instanceof GuiMainMenu))return;
        }

        if(DEBUG() && guiScreenIn!=null) {
            if(!guiScreenIn.getClass().toString().equals(LastPrint)){
                System.out.println(guiScreenIn.getClass());
                LastPrint = guiScreenIn.getClass().toString();
            }
        }
        if(EntityUtil.isDEAD(player))this.gameSettings.hideGUI = false;

        if (guiScreenIn == null && this.world == null)
        {
            guiScreenIn = new GuiMainMenu();
        }
        else if (guiScreenIn == null && this.player.getHealth() <= 0.0F)
        {
            guiScreenIn = new GuiGameOver(null);
        }

        GuiScreen old = this.currentScreen;
        net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);

        if (!(guiScreenIn instanceof GuiGameOver) && !(guiScreenIn instanceof GuiMainMenu) && net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            return;

        if (!(guiScreenIn instanceof GuiGameOver) && !(guiScreenIn instanceof GuiMainMenu))
            guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old) {
            old.onGuiClosed();
        }

        if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer)
        {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages(true);
        }

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null)
        {
            SET_INGAME_NOT_FOCUS();
            KeyBinding.unPressAllKeys();

            while (Mouse.next())
            {
            }

            while (Keyboard.next())
            {
            }

            ScaledResolution scaledresolution = new ScaledResolution((Minecraft)(Object)this);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution((Minecraft)(Object)this, i, j);
            this.skipRenderWorld = false;
        }
        else
        {
            this.soundHandler.resumeSounds();
            SET_INGAME_FOCUS();
        }
        if(EntityUtil.isDEAD(player))this.gameSettings.hideGUI = false;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void runTick() throws IOException
    {
        if (this.rightClickDelayTimer > 0)
        {
            --this.rightClickDelayTimer;
        }

        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreClientTick();

        if(EntityUtil.isProtected(player)){
            player.isDead=false;
            if (!player.world.playerEntities.contains(player)) {
                player.world.playerEntities.add(player);
                player.world.onEntityAdded(player);
            }
        }

        this.profiler.startSection("gui");

        if (!this.isGamePaused)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player))){
                this.ingameGUI.updateTick();
            }
        }

        this.profiler.endSection();
        if(!(TimeStop && !EntityUtil.isProtected(player)))this.entityRenderer.getMouseOver(1.0F);
        if(!(TimeStop && !EntityUtil.isProtected(player)))this.tutorial.onMouseHover(this.world, this.objectMouseOver);
        this.profiler.startSection("gameMode");

        if (!this.isGamePaused && this.world != null)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player)))this.playerController.updateController();
        }

        this.profiler.endStartSection("textures");

        if (this.world != null)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player)))this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.player != null)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player))){
                if (!EntityUtil.isProtected(player) && this.player.getHealth() <= 0.0F && !(this.currentScreen instanceof GuiGameOver)) {
                    this.displayGuiScreen(null);
                } else if (this.player.isPlayerSleeping() && this.world != null && !EntityUtil.isProtected(player)) {
                    this.displayGuiScreen(new GuiSleepMP());
                }
            }
        }
        else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.player.isPlayerSleeping())
        {
            this.displayGuiScreen(null);
        }

        if (this.currentScreen != null && !EntityUtil.isProtected(player))
        {
            this.leftClickCounter = 10000;
        }

        if (this.currentScreen != null && !(TimeStop && !EntityUtil.isProtected(player)))
        {
            try
            {
                this.currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashReportUtil.addDetail(crashreportcategory,"Screen name",getMinecraft());
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null)
            {
                try
                {
                    this.currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashReportUtil.addDetail(crashreportcategory1,"Screen name",getMinecraft());
                    throw new ReportedException(crashreport1);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput || EntityUtil.isProtected(player))
        {
            this.profiler.endStartSection("mouse");
            this.runTickMouse();

            if (this.leftClickCounter > 0)
            {
                --this.leftClickCounter;
            }

            this.profiler.endStartSection("keyboard");
            this.runTickKeyboard();
        }

        if (this.world != null)
        {
            if (this.player != null)
            {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30)
                {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.profiler.endStartSection("gameRenderer");

            if (!this.isGamePaused && !TimeStop)
            {
                this.entityRenderer.updateRenderer();
                //TODO
            }

            this.profiler.endStartSection("levelRenderer");

            if (!this.isGamePaused && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.renderGlobal.updateClouds();
            }

            this.profiler.endStartSection("level");

            if (!this.isGamePaused)
            {
                if (this.world.getLastLightningBolt() > 0 && !TimeStop && !SpecialItem.isTimeStop())
                {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.updateEntities();
            }
        }
        else if (this.entityRenderer.isShaderActive() && !TimeStop && !SpecialItem.isTimeStop())
        {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused && !(TimeStop && !EntityUtil.isProtected(player)))
        {
            this.musicTicker.update();
            this.soundHandler.update();
        }

        if (this.world != null)
        {
            if (!this.isGamePaused && !SpecialItem.isTimeStop() && !TimeStop)
            {
                this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                this.tutorial.update();

                try
                {
                    this.world.tick();
                }
                catch (Throwable throwable2)
                {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.world == null)
                    {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    }
                    else
                    {
                        this.world.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            this.profiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.profiler.endStartSection("particles");

            if (!this.isGamePaused && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.effectRenderer.updateEffects();
            }
        }
        else if (this.networkManager != null)
        {
            this.profiler.endStartSection("pendingConnection");
            this.networkManager.processReceivedPackets();
        }

        this.profiler.endSection();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostClientTick();
        this.systemTime = getSystemTime();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void runTickMouse() throws IOException {
        while (Mouse.next()) {
            if (net.minecraftforge.client.ForgeHooksClient.postMouseEvent()) continue;

            int i = Mouse.getEventButton();
            KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

            if (Mouse.getEventButtonState()) {
                if (this.player.isSpectator() && i == 2)
                {
                    this.ingameGUI.getSpectatorGui().onMiddleClick();
                }
                else
                {
                    KeyBinding.onTick(i - 100);
                }
            }

            long j = getSystemTime() - this.systemTime;

            if (j <= 200L)
            {
                int k = Mouse.getEventDWheel();

                if (k != 0)
                {
                    if (this.player.isSpectator())
                    {
                        k = k < 0 ? -1 : 1;

                        if (this.ingameGUI.getSpectatorGui().isMenuActive())
                        {
                            this.ingameGUI.getSpectatorGui().onMouseScroll(-k);
                        }
                        else
                        {
                            float f = MathHelper.clamp(this.player.capabilities.getFlySpeed() + (float)k * 0.005F, 0.0F, 0.2F);
                            this.player.capabilities.setFlySpeed(f);
                        }
                    }
                    else
                    {
                        this.player.inventory.changeCurrentItem(k);
                    }
                }

                if (this.currentScreen == null)
                {
                    if (!this.inGameHasFocus && Mouse.getEventButtonState())
                    {
                        SET_INGAME_FOCUS();
                    }
                }
                else if (this.currentScreen != null)
                {
                    this.currentScreen.handleMouseInput();
                }
            }
            net.minecraftforge.fml.common.FMLCommonHandler.instance().fireMouseInput();
        }
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public void run()
    {
        System.out.println("Successfully fucked MikuMinecraft.");

        this.running = true;

        try
        {
            this.init();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            try
            {
                while (this.running)
                {
                    try
                    {
                        this.runGameLoop();
                    }
                    catch (OutOfMemoryError var10)
                    {
                        this.freeMemory();
                        this.displayGuiScreen(new GuiMemoryErrorScreen());
                        System.gc();
                    }
                    catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
            catch (Throwable throwable1)
            {
                throwable1.printStackTrace();
            }
            finally
            {
                this.shutdownMinecraftApplet();
            }

            return;
        }
    }
}
