package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IDisplayableError;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.registry.ItemStackHolderInjector;
import net.minecraftforge.fml.common.toposort.ModSortingException;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Mixin(value = FMLClientHandler.class,remap = false)
public abstract class MixinFMLClientHandler {

    @Shadow protected abstract void detectOptifine();

    @Shadow private Minecraft client;

    @Shadow private List<IResourcePack> resourcePackList;

    @Shadow private MetadataSerializer metaSerializer;

    @Shadow private Map<String, IResourcePack> resourcePackMap;

    @Shadow @Nullable private IDisplayableError errorToDisplay;

    @Shadow public abstract void haltGame(String message, Throwable t);

    /**
     * @author mcst12345
     * @reason The fuck?
     */
    @Overwrite
    public void beginMinecraftLoading(Minecraft minecraft, List<IResourcePack> resourcePackList, IReloadableResourceManager resourceManager, MetadataSerializer metaSerializer) {
        System.out.println("Successfully fucked FMLClientHandler");

        detectOptifine();
        SplashProgress.start();
        client = minecraft;
        this.resourcePackList = resourcePackList;
        this.metaSerializer = metaSerializer;
        this.resourcePackMap = Maps.newHashMap();

        List<String> injectedModContainers = FMLCommonHandler.instance().beginLoading((FMLClientHandler) (Object) this);
        try {
            Loader.instance().loadMods(injectedModContainers);
        } catch (WrongMinecraftVersionException | DuplicateModsFoundException | MissingModsException |
                 ModSortingException | CustomModLoadingErrorDisplayException | MultipleModsErrored e) {
            FMLLog.log.error("An exception was thrown, the game will display an error screen and halt.", e);
            errorToDisplay = e;
            MinecraftForge.EVENT_BUS.shutdown();
        } finally {
            client.refreshResources();
        }

        try {
            Class<Loader> loader = (Class<Loader>) Loader.instance().getClass();
            Field field1 = loader.getField("modController");
            field1.setAccessible(true);
            LoadController modController = (LoadController) field1.get(Loader.instance());

            Field field2 = loader.getField("discoverer");
            field2.setAccessible(true);
            ModDiscoverer discoverer = (ModDiscoverer) field2.get(Loader.instance());

            Field field3 = loader.getField("canonicalConfigDir");
            field3.setAccessible(true);
            File canonicalConfigDir = (File) field3.get(Loader.instance());

            Field field4 = loader.getField("progressBar");
            field4.setAccessible(true);
            ProgressManager.ProgressBar progressBar = (ProgressManager.ProgressBar) field4.get(Loader.instance());


            if (!modController.isInState(LoaderState.PREINITIALIZATION))
            {
                FMLLog.log.warn("There were errors previously. Not beginning mod initialization phase");
                return;
            }
            GameData.fireCreateRegistryEvents();
            try {
                ObjectHolderRegistry.INSTANCE.findObjectHolders(discoverer.getASMTable());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            ItemStackHolderInjector.INSTANCE.findHolders(discoverer.getASMTable());
            CapabilityManager.INSTANCE.injectCapabilities(discoverer.getASMTable());
            modController.distributeStateMessage(LoaderState.PREINITIALIZATION, discoverer.getASMTable(), canonicalConfigDir);
            GameData.fireRegistryEvents(rl -> !rl.equals(GameData.RECIPES));
            FMLCommonHandler.instance().fireSidedRegistryEvents();
            ObjectHolderRegistry.INSTANCE.applyObjectHolders();
            ItemStackHolderInjector.INSTANCE.inject();
            modController.transition(LoaderState.INITIALIZATION, false);
            progressBar.step("Initializing Minecraft Engine");
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }
}
