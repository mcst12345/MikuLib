package miku.lib.mixins.minecraft;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.gui.TheGui;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.crashReportUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.CreativeSettings;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import java.net.Proxy;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
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

    public EntityRenderer MikuEntityRenderer() {
        return MikuEntityRenderer;
    }

    public void SET_INGAME_FOCUS() {
        if (Display.isActive()) {
            if (!this.inGameHasFocus) {
                if (!IS_RUNNING_ON_MAC) {
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

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public TextureManager renderEngine;

    @Shadow
    @Nullable
    public GuiScreen currentScreen;


    @Shadow
    private int leftClickCounter;

    /**
     * @author mcst12345
     * @reason Shit
     */
    @Overwrite
    private void runTickKeyboard() throws IOException {
        while (Keyboard.next()) {
            int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

            if (this.debugCrashKeyPressTime > 0L) {
                if (getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
                    throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                }

                if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61)) {
                    this.debugCrashKeyPressTime = -1L;
                }
            } else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61)) {
                this.actionKeyF3 = true;
                this.debugCrashKeyPressTime = getSystemTime();
            }

            this.dispatchKeypresses();

            if (this.currentScreen != null) {
                this.currentScreen.handleKeyboardInput();
            }

            boolean flag = Keyboard.getEventKeyState();

            if (flag) {
                if (i == 62 && this.MikuEntityRenderer != null) {
                    this.MikuEntityRenderer.switchUseShader();
                }

                boolean flag1 = false;

                if (this.currentScreen == null) {
                    if (i == 1) {
                        this.displayInGameMenu();
                    }

                    flag1 = Keyboard.isKeyDown(61) && this.processKeyF3(i);
                    this.actionKeyF3 |= flag1;

                    if (i == 59) {
                        this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                    }
                }

                if (flag1) {
                    KeyBinding.setKeyBindState(i, false);
                } else {
                    KeyBinding.setKeyBindState(i, true);
                    KeyBinding.onTick(i);
                }

                if (this.gameSettings.showDebugProfilerChart) {
                    if (i == 11) {
                        this.updateDebugProfilerName(0);
                    }

                    for (int j = 0; j < 9; ++j) {
                        if (i == 2 + j) {
                            this.updateDebugProfilerName(j + 1);
                        }
                    }
                }
            } else {
                KeyBinding.setKeyBindState(i, false);

                if (i == 61) {
                    if (this.actionKeyF3) {
                        this.actionKeyF3 = false;
                    } else {
                        this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                        this.gameSettings.showDebugProfilerChart = this.gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
                        this.gameSettings.showLagometer = this.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
                    }
                }
            }
            net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();
        }

        this.processKeyBinds();
    }

    @Shadow
    private int joinPlayerCounter;

    @Shadow
    public RenderGlobal renderGlobal;

    @Shadow
    private MusicTicker musicTicker;

    @Shadow
    private SoundHandler soundHandler;

    @Shadow
    public ParticleManager effectRenderer;

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

    @Shadow
    public boolean inGameHasFocus;

    @Shadow
    public MouseHelper mouseHelper;
    @Shadow
    @Nullable
    private NetworkManager networkManager;

    protected EntityRenderer MikuEntityRenderer;

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static Minecraft getMinecraft() {
        //if(MikuMinecraft.Miku)return MikuMinecraft.getMinecraft();
        return instance;
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private void init() throws LWJGLException {
        this.gameSettings = new GameSettings((Minecraft) (Object) this, this.gameDir);
        this.creativeSettings = new CreativeSettings((Minecraft) (Object) this, this.gameDir);
        this.defaultResourcePacks.add(this.defaultResourcePack);
        this.startTimerHackThread();

        if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
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
        net.minecraftforge.fml.client.FMLClientHandler.instance().beginMinecraftLoading((Minecraft) (Object) this, this.defaultResourcePacks, this.resourceManager, this.metadataSerializer);
        this.renderEngine = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.renderEngine);
        net.minecraftforge.fml.client.SplashProgress.drawVanillaScreen(this.renderEngine);
        this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService);
        this.saveLoader = new AnvilSaveConverter(new File(this.gameDir, "saves"), this.dataFixer);
        this.soundHandler = new SoundHandler(this.resourceManager, this.gameSettings);
        this.resourceManager.registerReloadListener(this.soundHandler);
        this.musicTicker = new MusicTicker((Minecraft) (Object) this);
        this.fontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);

        if (this.gameSettings.language != null) {
            this.fontRenderer.setUnicodeFlag(this.isUnicode());
            this.fontRenderer.setBidiFlag(this.languageManager.isCurrentLanguageBidirectional());
        }

        this.standardGalacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
        this.resourceManager.registerReloadListener(this.fontRenderer);
        this.resourceManager.registerReloadListener(this.standardGalacticFontRenderer);
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.mouseHelper = new MouseHelper();
        net.minecraftforge.fml.common.ProgressManager.ProgressBar bar = net.minecraftforge.fml.common.ProgressManager.push("Rendering Setup", 5, true);
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
        this.itemRenderer = new ItemRenderer((Minecraft) (Object) this);
        this.resourceManager.registerReloadListener(this.renderItem);
        bar.step("Loading Entity Renderer");
        net.minecraftforge.fml.client.SplashProgress.pause();
        this.entityRenderer = new EntityRenderer((Minecraft) (Object) this, this.resourceManager);
        this.MikuEntityRenderer = new EntityRenderer((Minecraft) (Object) this, this.resourceManager);
        this.resourceManager.registerReloadListener(this.MikuEntityRenderer);
        this.blockRenderDispatcher = new BlockRendererDispatcher(this.modelManager.getBlockModelShapes(), this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderDispatcher);
        this.renderGlobal = new RenderGlobal((Minecraft) (Object) this);
        this.resourceManager.registerReloadListener(this.renderGlobal);
        this.populateSearchTreeManager();
        this.resourceManager.registerReloadListener(this.searchTreeManager);
        GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
        this.effectRenderer = new ParticleManager(this.world, this.renderEngine);
        net.minecraftforge.fml.client.SplashProgress.resume();
        net.minecraftforge.fml.common.ProgressManager.pop(bar);
        net.minecraftforge.fml.client.FMLClientHandler.instance().finishMinecraftLoading();
        this.checkGLError("Post startup");
        this.ingameGUI = new net.minecraftforge.client.GuiIngameForge((Minecraft) (Object) this);

        if (this.serverName != null) {
            net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServerAtStartup(this.serverName, this.serverPort);
        } else {
            this.displayGuiScreen(new GuiMainMenu());
        }

        net.minecraftforge.fml.client.SplashProgress.clearVanillaResources(renderEngine, mojangLogo);
        this.mojangLogo = null;
        this.loadingScreen = new LoadingScreenRenderer((Minecraft) (Object) this);
        this.debugRenderer = new DebugRenderer((Minecraft) (Object) this);

        net.minecraftforge.fml.client.FMLClientHandler.instance().onInitializationComplete();
        if (this.gameSettings.fullScreen && !this.fullscreen) {
            this.toggleFullscreen();
        }

        try {
            Display.setVSyncEnabled(this.gameSettings.enableVsync);
        } catch (OpenGLException var2) {
            this.gameSettings.enableVsync = false;
            this.gameSettings.saveOptions();
        }

        this.renderGlobal.makeEntityOutlineShader();
    }

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
    long prevFrameTime;

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
    long startNanoTime;

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

    @Shadow
    @Final
    public File gameDir;

    @Shadow
    public CreativeSettings creativeSettings;

    @Shadow
    @Final
    public DefaultResourcePack defaultResourcePack;

    @Shadow
    @Final
    private List<IResourcePack> defaultResourcePacks;

    @Shadow
    protected abstract void startTimerHackThread();

    @Shadow
    protected abstract void setWindowIcon();

    @Shadow
    protected abstract void setInitialDisplayMode() throws LWJGLException;

    @Shadow
    protected abstract void createDisplay() throws LWJGLException;

    @Shadow
    protected abstract void registerMetadataSerializers();

    @Shadow
    private ResourcePackRepository resourcePackRepository;

    @Shadow
    @Final
    private File fileResourcepacks;

    @Shadow
    private IReloadableResourceManager resourceManager;

    @Shadow
    @Final
    private MetadataSerializer metadataSerializer;

    @Shadow
    private LanguageManager languageManager;

    @Shadow
    private SkinManager skinManager;

    @Shadow
    @Final
    private File fileAssets;

    @Shadow
    @Final
    private MinecraftSessionService sessionService;

    @Shadow
    private ISaveFormat saveLoader;

    @Shadow
    @Final
    private DataFixer dataFixer;

    @Shadow
    public abstract boolean isUnicode();

    @Shadow
    public FontRenderer standardGalacticFontRenderer;

    @Shadow
    private TextureMap textureMapBlocks;

    @Shadow
    private BlockColors blockColors;

    @Shadow
    private ItemColors itemColors;

    @Shadow
    private RenderItem renderItem;

    @Shadow
    private RenderManager renderManager;

    @Shadow
    private ItemRenderer itemRenderer;

    @Shadow
    private BlockRendererDispatcher blockRenderDispatcher;

    @Shadow
    public abstract void populateSearchTreeManager();

    @Shadow
    private SearchTreeManager searchTreeManager;

    @Shadow
    private String serverName;

    @Shadow
    private int serverPort;

    @Shadow
    private ResourceLocation mojangLogo;

    @Shadow
    public LoadingScreenRenderer loadingScreen;

    @Shadow
    public DebugRenderer debugRenderer;

    @Shadow
    private boolean fullscreen;

    @Shadow
    public abstract void toggleFullscreen();

    @Shadow
    private long debugCrashKeyPressTime;

    @Shadow
    private boolean actionKeyF3;

    @Shadow
    public abstract void dispatchKeypresses();

    @Shadow
    public abstract void displayInGameMenu();

    @Shadow
    protected abstract boolean processKeyF3(int auxKey);

    @Shadow
    @Nullable
    public abstract NetHandlerPlayClient getConnection();

    @Shadow
    protected abstract void clickMouse();

    @Shadow
    protected abstract void middleClickMouse();

    @Shadow
    protected abstract void sendClickBlockToController(boolean leftClick);

    @Shadow
    @Nullable
    private Entity renderViewEntity;

    @Shadow
    public abstract void setServerData(ServerData serverDataIn);

    @Shadow
    private boolean integratedServerIsRunning;

    @Shadow
    @Final
    private Proxy proxy;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private void processKeyBinds() {
        for (; this.gameSettings.keyBindTogglePerspective.isPressed(); this.renderGlobal.setDisplayListEntitiesDirty()) {
            ++this.gameSettings.thirdPersonView;

            if (this.gameSettings.thirdPersonView > 2) {
                this.gameSettings.thirdPersonView = 0;
            }

            if (this.gameSettings.thirdPersonView == 0) {
                this.MikuEntityRenderer.loadEntityShader(this.getRenderViewEntity());
            } else if (this.gameSettings.thirdPersonView == 1) {
                this.MikuEntityRenderer.loadEntityShader(null);
            }
        }

        while (this.gameSettings.keyBindSmoothCamera.isPressed()) {
            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
        }

        for (int i = 0; i < 9; ++i) {
            boolean flag = this.gameSettings.keyBindSaveToolbar.isKeyDown();
            boolean flag1 = this.gameSettings.keyBindLoadToolbar.isKeyDown();

            if (this.gameSettings.keyBindsHotbar[i].isPressed()) {
                if (this.player.isSpectator()) {
                    this.ingameGUI.getSpectatorGui().onHotbarSelected(i);
                } else if (!this.player.isCreative() || this.currentScreen != null || !flag1 && !flag) {
                    this.player.inventory.currentItem = i;
                } else {
                    GuiContainerCreative.handleHotbarSnapshots((Minecraft) (Object) this, i, flag1, flag);
                }
            }
        }

        while (this.gameSettings.keyBindInventory.isPressed()) {
            if (this.playerController.isRidingHorse()) {
                this.player.sendHorseInventory();
            } else {
                this.tutorial.openInventory();
                this.displayGuiScreen(new GuiInventory(this.player));
            }
        }

        while (this.gameSettings.keyBindAdvancements.isPressed()) {
            this.displayGuiScreen(new GuiScreenAdvancements(this.player.connection.getAdvancementManager()));
        }

        while (this.gameSettings.keyBindSwapHands.isPressed()) {
            if (!this.player.isSpectator()) {
                this.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }

        while (this.gameSettings.keyBindDrop.isPressed()) {
            if (!this.player.isSpectator()) {
                this.player.dropItem(GuiScreen.isCtrlKeyDown());
            }
        }

        boolean flag2 = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

        if (flag2) {
            while (this.gameSettings.keyBindChat.isPressed()) {
                this.displayGuiScreen(new GuiChat());
            }

            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed()) {
                this.displayGuiScreen(new GuiChat("/"));
            }
        }

        if (this.player.isHandActive()) {
            if (!this.gameSettings.keyBindUseItem.isKeyDown()) {
                this.playerController.onStoppedUsingItem(this.player);
            }

            label109:

            while (true) {
                if (!this.gameSettings.keyBindAttack.isPressed()) {
                    while (this.gameSettings.keyBindUseItem.isPressed()) {
                    }

                    while (true) {
                        if (this.gameSettings.keyBindPickBlock.isPressed()) {
                            continue;
                        }

                        break label109;
                    }
                }
            }
        } else {
            while (this.gameSettings.keyBindAttack.isPressed()) {
                this.clickMouse();
            }

            while (this.gameSettings.keyBindUseItem.isPressed()) {
                this.rightClickMouse();
            }

            while (this.gameSettings.keyBindPickBlock.isPressed()) {
                this.middleClickMouse();
            }
        }

        if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.player.isHandActive()) {
            this.rightClickMouse();
        }

        this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
    }

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
        if (!(TimeStop && !EntityUtil.isProtected(player))) this.MikuEntityRenderer.getMouseOver(1.0F);
        if (!(TimeStop && !EntityUtil.isProtected(player)))
            this.tutorial.onMouseHover(this.world, this.objectMouseOver);
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
                this.MikuEntityRenderer.updateRenderer();
                //TODO
            }

            this.MikuProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused && !TimeStop && !SpecialItem.isTimeStop())
            {
                this.renderGlobal.updateClouds();
            }

            this.MikuProfiler.endStartSection("level");

            if (!this.isGamePaused) {
                if (this.world.getLastLightningBolt() > 0 && !TimeStop && !SpecialItem.isTimeStop()) {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.updateEntities();
            }
        } else if (this.MikuEntityRenderer.isShaderActive() && !TimeStop && !SpecialItem.isTimeStop()) {
            this.MikuEntityRenderer.stopUseShader();
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
     * @reason Fuck!
     */
    @Overwrite
    public void runGameLoop() throws IOException {
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
            this.MikuEntityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks, i);
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
        this.MikuEntityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
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
    public void displayDebugInfo(long elapsedTicksTime) {
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

            for (Profiler.Result profiler$result1 : list) {
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

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    @SuppressWarnings("incomplete-switch")
    private void rightClickMouse() {
        if (!this.playerController.getIsHittingBlock()) {
            this.rightClickDelayTimer = 4;

            if (!this.player.isRowingBoat()) {
                if (this.objectMouseOver == null) {
                    LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                }

                for (EnumHand enumhand : EnumHand.values()) {
                    ItemStack itemstack = this.player.getHeldItem(enumhand);

                    if (this.objectMouseOver != null) {
                        switch (this.objectMouseOver.typeOfHit) {
                            case ENTITY:

                                if (this.playerController.interactWithEntity(this.player, this.objectMouseOver.entityHit, this.objectMouseOver, enumhand) == EnumActionResult.SUCCESS) {
                                    return;
                                }

                                if (this.playerController.interactWithEntity(this.player, this.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS) {
                                    return;
                                }

                                break;
                            case BLOCK:
                                BlockPos blockpos = this.objectMouseOver.getBlockPos();

                                if (this.world.getBlockState(blockpos).getMaterial() != Material.AIR) {
                                    int i = itemstack.getCount();
                                    EnumActionResult enumactionresult = this.playerController.processRightClickBlock(this.player, this.world, blockpos, this.objectMouseOver.sideHit, this.objectMouseOver.hitVec, enumhand);

                                    if (enumactionresult == EnumActionResult.SUCCESS) {
                                        this.player.swingArm(enumhand);

                                        if (!itemstack.isEmpty() && (itemstack.getCount() != i || this.playerController.isInCreativeMode())) {
                                            this.MikuEntityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                                        }

                                        return;
                                    }
                                }
                        }
                    }

                    if (itemstack.isEmpty() && (this.objectMouseOver == null || this.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS))
                        net.minecraftforge.common.ForgeHooks.onEmptyClick(this.player, enumhand);
                    if (!itemstack.isEmpty() && this.playerController.processRightClick(this.player, this.world, enumhand) == EnumActionResult.SUCCESS) {
                        this.MikuEntityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                        return;
                    }
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private void updateFramebufferSize() {
        this.framebuffer.createBindFramebuffer(this.displayWidth, this.displayHeight);

        if (this.MikuEntityRenderer != null) {
            this.MikuEntityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void loadWorld(@Nullable WorldClient worldClientIn, String loadingMessage) {
        if (world != null) {
            try {
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Unload(world));
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at WorldEvent.Unload");
                t.printStackTrace();
            }
        }

        if (worldClientIn == null) {
            NetHandlerPlayClient nethandlerplayclient = this.getConnection();

            if (nethandlerplayclient != null) {
                nethandlerplayclient.cleanup();
            }

            if (this.integratedServer != null && this.integratedServer.isAnvilFileSet()) {
                this.integratedServer.initiateShutdown();
                if (loadingScreen != null && this.running) {
                    this.loadingScreen.displayLoadingString(I18n.format("forge.client.shutdown.internal"));
                }
                while (!integratedServer.isServerStopped()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            this.integratedServer = null;
            this.MikuEntityRenderer.resetData();
            this.playerController = null;
            NarratorChatListener.INSTANCE.clear();
        }

        this.renderViewEntity = null;
        this.networkManager = null;

        if (this.loadingScreen != null) {
            this.loadingScreen.resetProgressAndMessage(loadingMessage);
            this.loadingScreen.displayLoadingString("");
        }

        if (worldClientIn == null && this.world != null) {
            this.resourcePackRepository.clearResourcePack();
            this.ingameGUI.resetPlayersOverlayFooterHeader();
            this.setServerData(null);
            this.integratedServerIsRunning = false;
            net.minecraftforge.fml.client.FMLClientHandler.instance().handleClientWorldClosing(this.world);
        }

        this.soundHandler.stopSounds();
        this.world = worldClientIn;

        if (this.renderGlobal != null) {
            this.renderGlobal.setWorldAndLoadRenderers(worldClientIn);
        }

        if (this.effectRenderer != null) {
            this.effectRenderer.clearEffects(worldClientIn);
        }

        TileEntityRendererDispatcher.instance.setWorld(worldClientIn);
        net.minecraftforge.client.MinecraftForgeClient.clearRenderCache();

        if (worldClientIn != null) {
            if (!this.integratedServerIsRunning) {
                AuthenticationService authenticationservice = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
                MinecraftSessionService minecraftsessionservice = authenticationservice.createMinecraftSessionService();
                GameProfileRepository gameprofilerepository = authenticationservice.createProfileRepository();
                PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(this.gameDir, MinecraftServer.USER_CACHE_FILE.getName()));
                TileEntitySkull.setProfileCache(playerprofilecache);
                TileEntitySkull.setSessionService(minecraftsessionservice);
                PlayerProfileCache.setOnlineMode(false);
            }

            if (this.player == null) {
                this.player = this.playerController.createPlayer(worldClientIn, new StatisticsManager(), new RecipeBookClient());
                this.playerController.flipPlayer(this.player);
            }

            this.player.preparePlayerToSpawn();
            worldClientIn.spawnEntity(this.player);
            this.player.movementInput = new MovementInputFromOptions(this.gameSettings);
            this.playerController.setPlayerCapabilities(this.player);
            this.renderViewEntity = this.player;
        } else {
            this.saveLoader.flushCache();
            this.player = null;
        }

        System.gc();
        this.systemTime = 0L;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void setRenderViewEntity(Entity viewingEntity) {
        this.renderViewEntity = viewingEntity;
        this.MikuEntityRenderer.loadEntityShader(viewingEntity);
    }
}
