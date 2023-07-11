package miku.lib.mixins.minecraft;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import miku.lib.api.iMinecraft;
import miku.lib.item.SpecialItem;
import miku.lib.sqlite.Sqlite;
import miku.lib.util.EntityUtil;
import miku.lib.util.ExceptionUtil;
import miku.lib.util.crashReportUtil;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.CreativeSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.OpenGLException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Mixin(value = Minecraft.class)
public abstract class MixinMinecraft implements iMinecraft {
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

    @Shadow protected abstract void runTickKeyboard() throws IOException;

    @Shadow private int joinPlayerCounter;

    @Shadow public RenderGlobal renderGlobal;

    @Shadow private MusicTicker musicTicker;

    @Shadow private SoundHandler soundHandler;

    @Shadow public ParticleManager effectRenderer;

    @Shadow @Nullable private NetworkManager networkManager;

    @Shadow
    long systemTime;

    @Shadow
    public static long getSystemTime() {
        return 0;
    }

    @Shadow
    public static Minecraft getMinecraft() {
        return null;
    }

    @Shadow public GameSettings gameSettings;

    @Shadow public boolean skipRenderWorld;


    @Shadow
    volatile boolean running;

    @Shadow public boolean inGameHasFocus;

    @Shadow public MouseHelper mouseHelper;

    @Shadow @Final public static boolean IS_RUNNING_ON_MAC;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    private void init() throws LWJGLException, IOException
    {
        this.gameSettings = new GameSettings((Minecraft)(Object)this, this.gameDir);
        this.creativeSettings = new CreativeSettings((Minecraft)(Object)this, this.gameDir);
        this.defaultResourcePacks.add(this.defaultResourcePack);
        this.startTimerHackThread();

        if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0)
        {
            this.displayWidth = this.gameSettings.overrideWidth;
            this.displayHeight = this.gameSettings.overrideHeight;
        }

        LOGGER.info("LWJGL Version: {}", Sys.getVersion());
        this.setWindowIcon();
        this.setInitialDisplayMode();
        this.createDisplay();
        OpenGlHelper.initializeTextures();
        this.framebuffer = new Framebuffer(this.displayWidth, this.displayHeight, true);
        this.framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.registerMetadataSerializers();
        this.resourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.gameDir, "server-resource-packs"), this.defaultResourcePack, this.metadataSerializer, this.gameSettings);
        this.resourceManager = new SimpleReloadableResourceManager(this.metadataSerializer);
        this.languageManager = new LanguageManager(this.metadataSerializer, this.gameSettings.language);
        this.resourceManager.registerReloadListener(this.languageManager);
        try {
            net.minecraftforge.fml.client.FMLClientHandler.instance().beginMinecraftLoading((Minecraft)(Object) this, this.defaultResourcePacks, this.resourceManager, this.metadataSerializer);
        } catch (Throwable e) {
            System.out.println("WARN:catch exception:"+e);
        }
        this.renderEngine = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.renderEngine);
        net.minecraftforge.fml.client.SplashProgress.drawVanillaScreen(this.renderEngine);
        this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService);
        this.saveLoader = new AnvilSaveConverter(new File(this.gameDir, "saves"), this.dataFixer);
        this.soundHandler = new SoundHandler(this.resourceManager, this.gameSettings);
        this.resourceManager.registerReloadListener(this.soundHandler);
        this.musicTicker = new MusicTicker((Minecraft)(Object)this);
        this.fontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);

        if (this.gameSettings.language != null)
        {
            this.fontRenderer.setUnicodeFlag(this.isUnicode());
            this.fontRenderer.setBidiFlag(this.languageManager.isCurrentLanguageBidirectional());
        }

        this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
        this.resourceManager.registerReloadListener(this.fontRenderer);
        this.resourceManager.registerReloadListener(this.standardGalacticFontRenderer);
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.mouseHelper = new MouseHelper();
        net.minecraftforge.fml.common.ProgressManager.ProgressBar bar= net.minecraftforge.fml.common.ProgressManager.push("Rendering Setup", 5, true);
        bar.step("GL Setup");
        this.checkGLError("Pre startup");
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        this.checkGLError("Startup");
        bar.step("Loading Texture Map");
        this.textureMapBlocks = new TextureMap("textures");
        this.textureMapBlocks.setMipmapLevels(this.gameSettings.mipmapLevels);
        this.renderEngine.loadTickableTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, this.textureMapBlocks);
        this.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        this.textureMapBlocks.setBlurMipmapDirect(false, this.gameSettings.mipmapLevels > 0);
        bar.step("Loading Model Manager");
        this.modelManager = new ModelManager(this.textureMapBlocks);
        this.resourceManager.registerReloadListener(this.modelManager);
        this.blockColors = BlockColors.init();
        this.itemColors = ItemColors.init(this.blockColors);
        bar.step("Loading Item Renderer");
        this.renderItem = new RenderItem(this.renderEngine, this.modelManager, this.itemColors);
        this.renderManager = new RenderManager(this.renderEngine, this.renderItem);
        this.itemRenderer = new ItemRenderer((Minecraft)(Object)this);
        this.resourceManager.registerReloadListener(this.renderItem);
        bar.step("Loading Entity Renderer");
        net.minecraftforge.fml.client.SplashProgress.pause();
        this.entityRenderer = new EntityRenderer((Minecraft)(Object)this, this.resourceManager);
        this.resourceManager.registerReloadListener(this.entityRenderer);
        this.blockRenderDispatcher = new BlockRendererDispatcher(this.modelManager.getBlockModelShapes(), this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderDispatcher);
        this.renderGlobal = new RenderGlobal((Minecraft)(Object)this);
        this.resourceManager.registerReloadListener(this.renderGlobal);
        this.populateSearchTreeManager();
        this.resourceManager.registerReloadListener(this.searchTreeManager);
        GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
        this.effectRenderer = new ParticleManager(this.world, this.renderEngine);
        net.minecraftforge.fml.client.SplashProgress.resume();
        net.minecraftforge.fml.common.ProgressManager.pop(bar);
        try {
            net.minecraftforge.fml.client.FMLClientHandler.instance().finishMinecraftLoading();
        } catch (Throwable e) {
            System.out.println("WARN:catch exception:"+e);
        }
        this.checkGLError("Post startup");
        this.ingameGUI = new net.minecraftforge.client.GuiIngameForge((Minecraft)(Object)this);

        if (this.serverName != null)
        {
            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServerAtStartup(this.serverName, this.serverPort);
        }
        else
        {
            this.displayGuiScreen(new GuiMainMenu());
        }

        net.minecraftforge.fml.client.SplashProgress.clearVanillaResources(renderEngine, mojangLogo);
        this.mojangLogo = null;
        this.loadingScreen = new LoadingScreenRenderer((Minecraft)(Object)this);
        this.debugRenderer = new DebugRenderer((Minecraft)(Object)this);

        try {
            net.minecraftforge.fml.client.FMLClientHandler.instance().onInitializationComplete();
        } catch (Throwable e) {
            System.out.println("WARN:catch exception:"+e);
        }
        if (this.gameSettings.fullScreen && !this.fullscreen)
        {
            this.toggleFullscreen();
        }

        try
        {
            Display.setVSyncEnabled(this.gameSettings.enableVsync);
        }
        catch (OpenGLException var2)
        {
            this.gameSettings.enableVsync = false;
            this.gameSettings.saveOptions();
        }

        this.renderGlobal.makeEntityOutlineShader();
    }

    @Shadow public abstract void displayCrashReport(CrashReport crashReportIn);

    @Shadow private boolean hasCrashed;

    @Shadow private CrashReport crashReporter;

    @Shadow protected abstract void runGameLoop() throws IOException;

    @Shadow public abstract void freeMemory();

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract void shutdownMinecraftApplet();

    @Shadow private static Minecraft instance;

    @Shadow @Final public File gameDir;

    @Shadow public CreativeSettings creativeSettings;

    @Shadow @Final private List<IResourcePack> defaultResourcePacks;

    @Shadow protected abstract void startTimerHackThread();

    @Shadow public int displayWidth;

    @Shadow public int displayHeight;

    @Shadow protected abstract void setWindowIcon();

    @Shadow protected abstract void setInitialDisplayMode() throws LWJGLException;

    @Shadow protected abstract void createDisplay() throws LWJGLException;

    @Shadow private Framebuffer framebuffer;

    @Shadow protected abstract void registerMetadataSerializers();

    @Shadow private ResourcePackRepository resourcePackRepository;

    @Shadow @Final private File fileResourcepacks;

    @Shadow @Final public DefaultResourcePack defaultResourcePack;

    @Shadow @Final private MetadataSerializer metadataSerializer;

    @Shadow private IReloadableResourceManager resourceManager;

    @Shadow private LanguageManager languageManager;

    @Shadow private SkinManager skinManager;

    @Shadow private ISaveFormat saveLoader;

    @Shadow @Final private DataFixer dataFixer;

    @Shadow @Final private File fileAssets;

    @Shadow @Final private MinecraftSessionService sessionService;

    @Shadow public FontRenderer fontRenderer;

    @Shadow public abstract boolean isUnicode();

    @Shadow public FontRenderer standardGalacticFontRenderer;

    @Shadow protected abstract void checkGLError(String message);

    @Shadow private TextureMap textureMapBlocks;

    @Shadow private BlockColors blockColors;

    @Shadow private ItemColors itemColors;

    @Shadow private RenderItem renderItem;

    @Shadow private RenderManager renderManager;

    @Shadow private ItemRenderer itemRenderer;

    @Shadow private BlockRendererDispatcher blockRenderDispatcher;

    @Shadow private String serverName;

    @Shadow private int serverPort;

    @Shadow public abstract void populateSearchTreeManager();

    @Shadow private SearchTreeManager searchTreeManager;

    @Shadow private ResourceLocation mojangLogo;

    @Shadow public LoadingScreenRenderer loadingScreen;

    @Shadow public DebugRenderer debugRenderer;

    @Shadow private boolean fullscreen;

    @Shadow public abstract void toggleFullscreen();

    /**
     * @author mcst12345
     * @reason F**k
     */
    @Overwrite
    public void displayGuiScreen(@Nullable GuiScreen guiScreenIn)
    {
        if (Sqlite.IS_GUI_BANNED(guiScreenIn)) {
            if((boolean)Sqlite.GetValueFromTable("debug","CONFIG",0))System.out.println(guiScreenIn.getClass().toString()+" is banned");
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
                    if (guiScreenIn.toString().toLowerCase().matches("(.*)dead(.*)") || guiScreenIn.toString().toLowerCase().matches("(.*)gameover(.*)")) {
                        return;
                    }
                }
            }
        }
        if((boolean)Sqlite.GetValueFromTable("debug","CONFIG",0) && guiScreenIn!=null)System.out.println(guiScreenIn.getClass().toString());

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

        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return;

        guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old)
        {
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
    }

    @Override
    public ModelManager GetModelManager() {
        return modelManager;
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
     * @reason F
     */
    @Overwrite
    private void runTickMouse() throws IOException
    {
        while (Mouse.next())
        {
            if (net.minecraftforge.client.ForgeHooksClient.postMouseEvent()) continue;

            int i = Mouse.getEventButton();
            KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

            if (Mouse.getEventButtonState())
            {
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
    public void run() {
        boolean finished = false;
        this.running = true;

        try {
            this.init();
        } catch (Throwable e) {
            System.out.println("WARN:catch exception:"+e);
        }
        if (!finished) {
            while (true) {
                try {
                    while (this.running) {
                        if (!this.hasCrashed || this.crashReporter == null) {
                            try {
                                this.runGameLoop();
                            } catch (OutOfMemoryError var10) {
                                this.freeMemory();
                                this.displayGuiScreen(new GuiMemoryErrorScreen());
                                System.gc();
                            } catch (Throwable e){
                                if(!ExceptionUtil.isIgnored(e))throw new RuntimeException(e);
                                else {
                                    System.out.println("WARN:catch exception:"+e);
                                }
                            }
                        } else {
                            this.displayCrashReport(this.crashReporter);
                        }
                    }
                } catch (Throwable e) {
                    System.out.println("WARN:catch exception:"+e);
                }
            }
        }

    }

}
