package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IDisplayableError;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.toposort.ModSortingException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
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
        } catch (LoaderException le) {
            haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
            return;
        } finally {
            client.refreshResources();
        }

        try {
            Loader.instance().preinitializeMods();
        }
        catch (Throwable e){
            e.printStackTrace();
        }
    }
}
