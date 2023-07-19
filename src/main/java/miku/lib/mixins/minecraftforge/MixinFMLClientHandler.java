package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Maps;
import miku.lib.core.MikuTransformer;
import miku.lib.core.MikuTweaker;
import miku.lib.sqlite.Sqlite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IDisplayableError;
import net.minecraftforge.fml.client.SplashProgress;
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

    @Shadow private Minecraft client;

    @Shadow private List<IResourcePack> resourcePackList;

    @Shadow private MetadataSerializer metaSerializer;

    @Shadow private Map<String, IResourcePack> resourcePackMap;

    @Shadow @Nullable private IDisplayableError errorToDisplay;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void beginMinecraftLoading(Minecraft minecraft, List<IResourcePack> resourcePackList, IReloadableResourceManager resourceManager, MetadataSerializer metaSerializer)
    {
        //MikuTweaker.fucker.stop();
        MikuTweaker.cachedClasses.clear();
        detectOptifine();
        SplashProgress.start();
        client = minecraft;
        this.resourcePackList = resourcePackList;
        this.metaSerializer = metaSerializer;
        this.resourcePackMap = Maps.newHashMap();
        if (minecraft.isDemo())
        {
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
            MinecraftForge.EVENT_BUS.shutdown();
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
                MinecraftForge.EVENT_BUS.shutdown();
            }
            else
            {
                haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
                return;
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String,String>> sharedModList = (Map<String, Map<String, String>>) Launch.blackboard.get("modList");
        if (sharedModList == null)
        {
            sharedModList = Maps.newHashMap();
            Launch.blackboard.put("modList", sharedModList);
        }
        for (ModContainer mc : Loader.instance().getActiveModList())
        {
            Map<String,String> sharedModDescriptor = mc.getSharedModDescriptor();
            if (sharedModDescriptor != null)
            {
                String sharedModId = "fml:"+mc.getModId();
                sharedModList.put(sharedModId, sharedModDescriptor);
            }
        }
    }
}
