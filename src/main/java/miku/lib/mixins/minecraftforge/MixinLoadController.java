package miku.lib.mixins.minecraftforge;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.EventBus;
import miku.lib.common.core.MikuCore;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = LoadController.class, remap = false)
public class MixinLoadController {
    @Shadow
    private List<ModContainer> activeModList;

    @Shadow
    private ListMultimap<String, ModContainer> packageOwners;

    @Shadow
    private LoaderState state;

    @Shadow
    private EventBus masterChannel;

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public ImmutableBiMap<ModContainer, Object> buildModObjectList() {
        ImmutableBiMap.Builder<ModContainer, Object> builder = ImmutableBiMap.builder();
        for (ModContainer mc : activeModList) {
            if (MikuCore.isModInvalid(mc.getModId()) || mc.getMod() == null && !mc.isImmutable() && state != LoaderState.CONSTRUCTING) {
                System.out.println("MikuWarn:Ignore mod:" + mc.getModId());
                continue;
            }
            if (!mc.isImmutable() && mc.getMod() != null) {
                builder.put(mc, mc.getMod());
                List<String> packages = mc.getOwnedPackages();
                for (String pkg : packages) {
                    packageOwners.put(pkg, mc);
                }
            }

        }
        return builder.build();
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void distributeStateMessage(LoaderState state, Object... eventData) {
        if (state.hasEvent()) {
            try {
                masterChannel.post(state.getEvent(eventData));
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception:");
                t.printStackTrace();
            }
        }
    }
}
