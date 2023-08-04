package miku.lib.mixins.minecraftforge;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import miku.lib.common.api.iEventBus;
import miku.lib.common.core.MikuTransformer;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.*;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.toposort.ModSortingException;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(value = FMLClientHandler.class,remap = false)
public abstract class MixinFMLClientHandler implements IFMLSidedHandler {
    @Shadow protected abstract void detectOptifine();

    @Shadow
    private Minecraft client;

    @Shadow
    private List<IResourcePack> resourcePackList;

    @Shadow
    private MetadataSerializer metaSerializer;

    @Shadow
    private Map<String, IResourcePack> resourcePackMap;

    @Shadow
    @Nullable
    private IDisplayableError errorToDisplay;
    @Shadow
    private BiMap<ModContainer, IModGuiFactory> guiFactories;
    @Shadow
    private boolean loading;

    @Shadow
    public abstract boolean hasError();

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void beginMinecraftLoading(Minecraft minecraft, List<IResourcePack> resourcePackList, IReloadableResourceManager resourceManager, MetadataSerializer metaSerializer) {
        Launch.NoReflection(FMLClientHandler.class);
        detectOptifine();
        SplashProgress.start();
        client = minecraft;
        this.resourcePackList = resourcePackList;
        this.metaSerializer = metaSerializer;
        this.resourcePackMap = Maps.newHashMap();
        if (minecraft.isDemo()) {
            FMLLog.log.fatal("DEMO MODE DETECTED, FML will not work. Finishing now.");
            haltGame("FML will not run in demo mode", new RuntimeException());
            return;
        }

        List<String> injectedModContainers = FMLCommonHandler.instance().beginLoading(this);
        try
        {
            Loader.instance().loadMods(injectedModContainers);
            Sqlite.Init();
            for(FieldNode field : MikuTransformer.BadFields){
                //To Do ?
            }
        }
        catch (WrongMinecraftVersionException | DuplicateModsFoundException | MissingModsException |
               ModSortingException | CustomModLoadingErrorDisplayException | MultipleModsErrored e)
        {
            FMLLog.log.error("An exception was thrown, the game will display an error screen and halt.", e);
            errorToDisplay = e;
            ((iEventBus) MinecraftForge.EVENT_BUS).Shutdown();
        }
        catch (LoaderException le)
        {
            haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
            return;
        }
        finally
        {
            client.refreshResources();
        }

        try
        {
            Loader.instance().preinitializeMods();
        }
        catch (LoaderException le)
        {
            if (le.getCause() instanceof CustomModLoadingErrorDisplayException)
            {
                CustomModLoadingErrorDisplayException custom = (CustomModLoadingErrorDisplayException) le.getCause();
                FMLLog.log.error("A custom exception was thrown by a mod, the game will display an error screen and halt.", custom);
                errorToDisplay = custom;
                ((iEventBus) MinecraftForge.EVENT_BUS).Shutdown();
            }
            else
            {
                haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
                return;
            }
        }

        Launch.NoReflection(Minecraft.class);
        @SuppressWarnings("unchecked")
        Map<String, Map<String,String>> sharedModList = (Map<String, Map<String, String>>) Launch.blackboard.get("modList");
        if (sharedModList == null)
        {
            sharedModList = Maps.newHashMap();
            Launch.blackboard.put("modList", sharedModList);
        }
        for (ModContainer mc : Loader.instance().getActiveModList()) {
            Map<String, String> sharedModDescriptor = mc.getSharedModDescriptor();
            if (sharedModDescriptor != null) {
                String sharedModId = "fml:" + mc.getModId();
                sharedModList.put(sharedModId, sharedModDescriptor);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Holy fuck
     */
    @Overwrite
    public void finishMinecraftLoading() {
        if (hasError()) {
            SplashProgress.finish();
            return;
        }
        try {
            Loader.instance().initializeMods();
        } catch (LoaderException le) {
            if (le.getCause() instanceof CustomModLoadingErrorDisplayException) {
                CustomModLoadingErrorDisplayException custom = (CustomModLoadingErrorDisplayException) le.getCause();
                FMLLog.log.error("A custom exception was thrown by a mod, the game will display an error screen and halt.", custom);
                errorToDisplay = custom;
                ((iEventBus) MinecraftForge.EVENT_BUS).Shutdown();
            } else {
                haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
                return;
            }
        }

        // This call is being phased out for performance reasons in 1.12,
        // but we are keeping an option here in case something needs it for a little longer.
        // See https://github.com/MinecraftForge/MinecraftForge/pull/4032
        // TODO remove in 1.13
        if (Boolean.parseBoolean(System.getProperty("fml.reloadResourcesOnStart", "false"))) {
            client.refreshResources();
        }

        RenderingRegistry.loadEntityRenderers(Minecraft.getMinecraft().getRenderManager().entityRenderMap);
        guiFactories = HashBiMap.create();
        for (ModContainer mc : Loader.instance().getActiveModList()) {
            String className = mc.getGuiClassName();
            if (Strings.isNullOrEmpty(className)) {
                if (ConfigManager.hasConfigForMod(mc.getModId())) {
                    guiFactories.put(mc, DefaultGuiFactory.forMod(mc));
                }
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className, true, Loader.instance().getModClassLoader());
                Class<? extends IModGuiFactory> guiClassFactory = clazz.asSubclass(IModGuiFactory.class);
                IModGuiFactory guiFactory = guiClassFactory.newInstance();
                guiFactory.initialize(client);
                guiFactories.put(mc, guiFactory);
            } catch (Exception e) {
                FMLLog.log.error("A critical error occurred instantiating the gui factory for mod {}", mc.getModId(), e);
            }
        }
        loading = false;
        client.gameSettings.loadOptions(); //Reload options to load any mod added keybindings.
        if (!hasError())
            Loader.instance().loadingComplete();
        SplashProgress.finish();
    }
}
