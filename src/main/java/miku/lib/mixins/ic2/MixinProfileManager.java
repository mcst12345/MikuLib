package miku.lib.mixins.ic2;

import ic2.core.profile.Profile;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.TextureStyle;
import miku.lib.client.api.iFMLClientHandler;
import miku.lib.client.api.iFallbackResourceManager;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.api.iSimpleReloadableResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(value = ProfileManager.class)
public class MixinProfileManager {
    @Shadow
    private static List<IResourcePack> textureChanges;

    @Shadow
    public static Profile selected;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public static void doTextureChanges() {
        if (textureChanges == null) {
            textureChanges = Collections.emptyList();
        }

        List<IResourcePack> packs = new ArrayList<>();

        IReloadableResourceManager rm = ((iMinecraft) Minecraft.getMinecraft()).GetResourceManager();

        Map<String, FallbackResourceManager> domainManagers = ((iSimpleReloadableResourceManager) rm).getDomainResourceManagers();

        for (TextureStyle texture : selected.textures) {
            FallbackResourceManager manager = domainManagers.get(texture.mod);
            if (manager != null) {
                ((iFallbackResourceManager) manager).getResourcePacks().removeAll(textureChanges);
                IResourcePack pack = texture.applyChanges();
                if (pack != null) {
                    manager.addResourcePack(pack);
                    packs.add(pack);
                }
            }
        }

        List<IResourcePack> defaultPacks = ((iFMLClientHandler) FMLClientHandler.instance()).getResourcePackList();
        defaultPacks.removeAll(textureChanges);

        assert defaultPacks.stream().noneMatch((packx) -> packx.getPackName().startsWith("IC2 Profile Pack for "));

        defaultPacks.addAll(packs);
        textureChanges = packs;
    }
}
