package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.client.gui.TheGui;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.crashReportUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import static miku.lib.common.sqlite.Sqlite.DEBUG;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft implements iMinecraft {
    public Profiler MikuProfiler() {
        return MikuProfiler;
    }

    protected String LastPrint;

    public void Stop() {
        running = false;
    }

    public MixinMinecraft() {
    }

    protected boolean protect = false;

    public void SET_INGAME_FOCUS() {
        if (Display.isActive()) {
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

    protected Profiler MikuProfiler = new Profiler();

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
    public abstract void freeMemory();

    @Shadow
    public abstract void shutdownMinecraftApplet();

    @Shadow
    public abstract void shutdown();

    @Shadow
    @Final
    private Timer timer;

    @Shadow
    @Final
    private Queue<FutureTask<?>> scheduledTasks;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void checkGLError(String message);

    @Shadow
    @Nullable
    public abstract Entity getRenderViewEntity();

    @Shadow
    private Framebuffer framebuffer;

    @Shadow
    private float renderPartialTicksPaused;

    @Shadow
    @Final
    private GuiToast toastGui;
    @Shadow
    private long prevFrameTime;

    @Shadow
    public int displayWidth;

    @Shadow
    public int displayHeight;

    @Shadow
    private int fpsCounter;

    @Shadow
    public abstract boolean isSingleplayer();

    @Shadow
    @Nullable
    private IntegratedServer integratedServer;

    @Shadow
    @Final
    public FrameTimer frameTimer;

    @Shadow
    private long startNanoTime;

    @Shadow
    private long debugUpdateTime;

    @Shadow
    private static int debugFPS;

    @Shadow
    public String debug;

    @Shadow
    @Final
    private Snooper usageSnooper;

    @Shadow
    public abstract boolean isFramerateLimitBelowMax();

    @Shadow
    public abstract int getLimitFramerate();

    @Shadow
    protected abstract void checkWindowResize();

    @Shadow
    private String debugProfilerName;

    @Shadow
    public FontRenderer fontRenderer;

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
        if (this.rightClickDelayTimer > 0) {
            --this.rightClickDelayTimer;
        }

        try {
            FMLCommonHandler.instance().onPreClientTick();
        } catch (Throwable e) {
            System.out.println("MikuWarn:Catch exception at onPreClientTick");
            e.printStackTrace();
        }

        if (EntityUtil.isProtected(player)) {
            player.isDead = false;
            if (!player.world.playerEntities.contains(player)) {
                player.world.playerEntities.add(player);
                player.world.onEntityAdded(player);
            }
        }

        this.MikuProfiler.startSection("gui");

        if (!this.isGamePaused)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player))){
                this.ingameGUI.updateTick();
            }
        }

        this.MikuProfiler.endSection();
        if(!(TimeStop && !EntityUtil.isProtected(player)))this.entityRenderer.getMouseOver(1.0F);
        if(!(TimeStop && !EntityUtil.isProtected(player)))this.tutorial.onMouseHover(this.world, this.objectMouseOver);
        this.MikuProfiler.startSection("gameMode");

        if (!this.isGamePaused && this.world != null)
        {
            if(!(TimeStop && !EntityUtil.isProtected(player)))this.playerController.updateController();
        }

        this.MikuProfiler.endStartSection("textures");

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
            this.MikuProfiler.endStartSection("mouse");
            this.runTickMouse();

            if (this.leftClickCounter > 0)
            {
                --this.leftClickCounter;
            }

            this.MikuProfiler.endStartSection("keyboard");
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

            this.MikuProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused && !TimeStop)
            {
                this.entityRenderer.updateRenderer();
                //TODO
            }

            this.MikuProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.renderGlobal.updateClouds();
            }

            this.MikuProfiler.endStartSection("level");

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

            this.MikuProfiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.MikuProfiler.endStartSection("particles");

            if (!this.isGamePaused && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.effectRenderer.updateEffects();
            }
        } else if (this.networkManager != null) {
            this.MikuProfiler.endStartSection("pendingConnection");
            this.networkManager.processReceivedPackets();
        }

        this.MikuProfiler.endSection();
        try {
            FMLCommonHandler.instance().onPostClientTick();
        } catch (Throwable e) {
            System.out.println("MikuWarn:catch exception at onPostClientTick");
            e.printStackTrace();
        }
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
        System.out.println("Successfully fucked Minecraft.");

        this.running = true;

        try
        {
            this.init();
        }
        catch (Throwable e)
        {
            System.out.println("MikuWarn:Catch exception when init Minecraft.");
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
                        System.out.println("MikuWarn:catch exception when running game loop.");
                        e.printStackTrace();
                    }
                }
            } finally {
                this.shutdownMinecraftApplet();
            }

            return;
        }
    }

    /**
     * @author mcst12345
     * @reason Holy Fuck
     */
    @Overwrite
    private void runGameLoop() throws IOException {
        long i = System.nanoTime();
        this.MikuProfiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested()) {
            this.shutdown();
        }

        this.timer.updateTimer();
        this.MikuProfiler.startSection("scheduledExecutables");

        synchronized (this.scheduledTasks) {
            while (!this.scheduledTasks.isEmpty()) {
                try {
                    Util.runTask(this.scheduledTasks.poll(), LOGGER);
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Catch exception when running scheduledTasks.");
                    t.printStackTrace();
                }
            }
        }

        this.MikuProfiler.endSection();
        long l = System.nanoTime();
        this.MikuProfiler.startSection("tick");

        for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j) {
            this.runTick();
        }

        this.MikuProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        this.checkGLError("Pre render");
        this.MikuProfiler.endStartSection("sound");
        this.soundHandler.setListener(this.getRenderViewEntity(), this.timer.renderPartialTicks); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
        this.MikuProfiler.endSection();
        this.MikuProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        this.framebuffer.bindFramebuffer(true);
        this.MikuProfiler.startSection("display");
        GlStateManager.enableTexture2D();
        this.MikuProfiler.endSection();

        if (!this.skipRenderWorld) {
            try {
                FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at onRenderTickStart");
                t.printStackTrace();
            }
            this.MikuProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks, i);
            this.MikuProfiler.endStartSection("toasts");
            this.toastGui.drawToast(new ScaledResolution((Minecraft) (Object) this));
            this.MikuProfiler.endSection();
            try {
                FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at onRenderTickEnd");
                t.printStackTrace();
            }
        }

        this.MikuProfiler.endSection();

        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI) {
            if (!this.MikuProfiler.profilingEnabled) {
                this.MikuProfiler.clearProfiling();
            }

            this.MikuProfiler.profilingEnabled = true;
            this.displayDebugInfo(i1);
        } else {
            this.MikuProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        this.framebuffer.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.framebuffer.framebufferRender(this.displayWidth, this.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.entityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
        GlStateManager.popMatrix();
        this.MikuProfiler.startSection("root");
        this.updateDisplay();
        Thread.yield();
        this.checkGLError("Post render");
        ++this.fpsCounter;
        boolean flag = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.integratedServer.getPublic();

        if (this.isGamePaused != flag) {
            if (this.isGamePaused) {
                this.renderPartialTicksPaused = this.timer.renderPartialTicks;
            } else {
                this.timer.renderPartialTicks = this.renderPartialTicksPaused;
            }

            this.isGamePaused = flag;
        }

        long k = System.nanoTime();
        this.frameTimer.addFrame(k - this.startNanoTime);
        this.startNanoTime = k;

        while (getSystemTime() >= this.debugUpdateTime + 1000L) {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float) this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : this.gameSettings.limitFramerate, this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.usageSnooper.addMemoryStatsToSnooper();

            if (!this.usageSnooper.isSnooperRunning()) {
                this.usageSnooper.startSnooper();
            }
        }

        if (this.isFramerateLimitBelowMax()) {
            this.MikuProfiler.startSection("fpslimit_wait");
            Display.sync(this.getLimitFramerate());
            this.MikuProfiler.endSection();
        }

        this.MikuProfiler.endSection();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void updateDisplay() {
        this.MikuProfiler.startSection("display_update");
        Display.update();
        this.MikuProfiler.endSection();
        this.checkWindowResize();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    private void updateDebugProfilerName(int keyCount) {
        List<Profiler.Result> list = this.MikuProfiler.getProfilingData(this.debugProfilerName);

        if (!list.isEmpty()) {
            Profiler.Result profiler$result = list.remove(0);

            if (keyCount == 0) {
                if (!profiler$result.profilerName.isEmpty()) {
                    int i = this.debugProfilerName.lastIndexOf(46);

                    if (i >= 0) {
                        this.debugProfilerName = this.debugProfilerName.substring(0, i);
                    }
                }
            } else {
                --keyCount;

                if (keyCount < list.size() && !"unspecified".equals((list.get(keyCount)).profilerName)) {
                    if (!this.debugProfilerName.isEmpty()) {
                        this.debugProfilerName = this.debugProfilerName + ".";
                    }

                    this.debugProfilerName = this.debugProfilerName + (list.get(keyCount)).profilerName;
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    private void displayDebugInfo(long elapsedTicksTime) {
        if (this.MikuProfiler.profilingEnabled) {
            List<Profiler.Result> list = this.MikuProfiler.getProfilingData(this.debugProfilerName);
            Profiler.Result profiler$result = list.remove(0);
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.enableColorMaterial();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int i = 160;
            int j = this.displayWidth - 160 - 10;
            int k = this.displayHeight - 320;
            GlStateManager.enableBlend();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos((float) j - 176.0F, (float) k - 96.0F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            bufferbuilder.pos((float) j - 176.0F, k + 320, 0.0D).color(200, 0, 0, 0).endVertex();
            bufferbuilder.pos((float) j + 176.0F, k + 320, 0.0D).color(200, 0, 0, 0).endVertex();
            bufferbuilder.pos((float) j + 176.0F, (float) k - 96.0F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
            tessellator.draw();
            GlStateManager.disableBlend();
            double d0 = 0.0D;

            for (int l = 0; l < list.size(); ++l) {
                Profiler.Result profiler$result1 = list.get(l);
                int i1 = MathHelper.floor(profiler$result1.usePercentage / 4.0D) + 1;
                bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
                int j1 = profiler$result1.getColor();
                int k1 = j1 >> 16 & 255;
                int l1 = j1 >> 8 & 255;
                int i2 = j1 & 255;
                bufferbuilder.pos(j, k, 0.0D).color(k1, l1, i2, 255).endVertex();

                for (int j2 = i1; j2 >= 0; --j2) {
                    float f = (float) ((d0 + profiler$result1.usePercentage * (double) j2 / (double) i1) * (Math.PI * 2D) / 100.0D);
                    float f1 = MathHelper.sin(f) * 160.0F;
                    float f2 = MathHelper.cos(f) * 160.0F * 0.5F;
                    bufferbuilder.pos((float) j + f1, (float) k - f2, 0.0D).color(k1, l1, i2, 255).endVertex();
                }

                tessellator.draw();
                bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

                for (int i3 = i1; i3 >= 0; --i3) {
                    float f3 = (float) ((d0 + profiler$result1.usePercentage * (double) i3 / (double) i1) * (Math.PI * 2D) / 100.0D);
                    float f4 = MathHelper.sin(f3) * 160.0F;
                    float f5 = MathHelper.cos(f3) * 160.0F * 0.5F;
                    bufferbuilder.pos((float) j + f4, (float) k - f5, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                    bufferbuilder.pos((float) j + f4, (float) k - f5 + 10.0F, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
                }

                tessellator.draw();
                d0 += profiler$result1.usePercentage;
            }

            DecimalFormat decimalformat = new DecimalFormat("##0.00");
            GlStateManager.enableTexture2D();
            String s = "";

            if (!"unspecified".equals(profiler$result.profilerName)) {
                s = s + "[0] ";
            }

            if (profiler$result.profilerName.isEmpty()) {
                s = s + "ROOT ";
            } else {
                s = s + profiler$result.profilerName + ' ';
            }

            int l2 = 16777215;
            this.fontRenderer.drawStringWithShadow(s, (float) (j - 160), (float) (k - 80 - 16), 16777215);
            s = decimalformat.format(profiler$result.totalUsePercentage) + "%";
            this.fontRenderer.drawStringWithShadow(s, (float) (j + 160 - this.fontRenderer.getStringWidth(s)), (float) (k - 80 - 16), 16777215);

            for (int k2 = 0; k2 < list.size(); ++k2) {
                Profiler.Result profiler$result2 = list.get(k2);
                StringBuilder stringbuilder = new StringBuilder();

                if ("unspecified".equals(profiler$result2.profilerName)) {
                    stringbuilder.append("[?] ");
                } else {
                    stringbuilder.append("[").append(k2 + 1).append("] ");
                }

                String s1 = stringbuilder.append(profiler$result2.profilerName).toString();
                this.fontRenderer.drawStringWithShadow(s1, (float) (j - 160), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
                s1 = decimalformat.format(profiler$result2.usePercentage) + "%";
                this.fontRenderer.drawStringWithShadow(s1, (float) (j + 160 - 50 - this.fontRenderer.getStringWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
                s1 = decimalformat.format(profiler$result2.totalUsePercentage) + "%";
                this.fontRenderer.drawStringWithShadow(s1, (float) (j + 160 - this.fontRenderer.getStringWidth(s1)), (float) (k + 80 + k2 * 8 + 20), profiler$result2.getColor());
            }
        }
    }

}
