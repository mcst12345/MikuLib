package miku.lib.mixins.foamfix;

import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.ProxyCommon;
import pl.asie.foamfix.common.TileEntityFasterHopper;
import pl.asie.foamfix.shared.FoamFixShared;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

@Mixin(value = ProxyCommon.class, remap = false)
public class MixinProxyCommon {
    /**
     * @author mcst12345
     * @reason Fix.
     */
    @Overwrite
    public void init() {
        if (this.getClass().getClassLoader() instanceof LaunchClassLoader && FoamFixShared.config.lwWeakenResourceCache) {
            FoamFix.getLogger().info("Weakening LaunchWrapper resource cache...");

            try {
                LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();
                Map<String, byte[]> oldResourceCache = loader.resourceCache;
                Map newResourceCache = CacheBuilder.newBuilder().weakValues().build().asMap();
                newResourceCache.putAll(oldResourceCache);
                loader.resourceCache = newResourceCache;
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void preInit() {
        if (FoamFixShared.config.geFasterHopper) {
            TileEntity.register("hopper", TileEntityFasterHopper.class);
            FoamFix.TILE_OVERRIDES.put(TileEntityHopper.class, new ResourceLocation("hopper"));
        }

        if (this.getClass().getClassLoader() instanceof LaunchClassLoader && FoamFixShared.config.lwRemovePackageManifestMap) {
            FoamFix.getLogger().info("Removing LaunchWrapper package manifest map...");

            try {
                LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();
                loader.packageManifests = new Map<Package, Manifest>() {
                    public int size() {
                        return 0;
                    }

                    public boolean isEmpty() {
                        return true;
                    }

                    public boolean containsKey(Object o) {
                        return false;
                    }

                    public boolean containsValue(Object o) {
                        return false;
                    }

                    public Manifest get(Object o) {
                        return null;
                    }

                    public Manifest put(Package o, Manifest o2) {
                        return o2;
                    }

                    public Manifest remove(Object o) {
                        return null;
                    }

                    public void putAll(Map map) {
                    }

                    public void clear() {
                    }

                    public Set keySet() {
                        return Collections.emptySet();
                    }

                    public Collection values() {
                        return Collections.emptySet();
                    }

                    public Set<Map.Entry<Package, Manifest>> entrySet() {
                        return Collections.emptySet();
                    }
                };
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

    }
}
