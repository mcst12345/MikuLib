package miku.lib.mixins.minecraftforge;

import com.google.common.base.Strings;
import com.google.common.collect.SetMultimap;
import miku.lib.common.core.MikuLib;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Mixin(value = AutomaticEventSubscriber.class, remap = false)
public abstract class MixinAutomaticEventSubscriber {
    @Shadow
    @Final
    private static EnumSet<Side> DEFAULT;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void inject(ModContainer mod, ASMDataTable data, Side side) {
        FMLLog.log.debug("Attempting to inject @EventBusSubscriber classes into the eventbus for {}", mod.getModId());
        SetMultimap<String, ASMDataTable.ASMData> modData = data.getAnnotationsFor(mod);
        Set<ASMDataTable.ASMData> mods = modData.get(Mod.class.getName());
        Set<ASMDataTable.ASMData> targets = modData.get(Mod.EventBusSubscriber.class.getName());
        ClassLoader mcl = Loader.instance().getModClassLoader();

        for (ASMDataTable.ASMData targ : targets) {
            try {
                //noinspection unchecked
                List<ModAnnotation.EnumHolder> sidesEnum = (List<ModAnnotation.EnumHolder>) targ.getAnnotationInfo().get("value");
                EnumSet<Side> sides = DEFAULT;
                if (sidesEnum != null) {
                    sides = EnumSet.noneOf(Side.class);
                    for (ModAnnotation.EnumHolder h : sidesEnum) {
                        sides.add(Side.valueOf(h.getValue()));
                    }
                }
                if (sides == DEFAULT || sides.contains(side)) {
                    //FMLLog.log.debug("Found @EventBusSubscriber class {}", targ.getClassName());
                    String amodid = (String) targ.getAnnotationInfo().get("modid");
                    if (Strings.isNullOrEmpty(amodid)) {
                        amodid = ASMDataTable.getOwnerModID(mods, targ);
                        if (Strings.isNullOrEmpty(amodid)) {
                            FMLLog.bigWarning("Could not determine owning mod for @EventBusSubscriber on {} for mod {}", targ.getClassName(), mod.getModId());
                            continue;
                        }
                    }
                    if (!mod.getModId().equals(amodid)) {
                        FMLLog.log.debug("Skipping @EventBusSubscriber injection for {} since it is not for mod {}", targ.getClassName(), mod.getModId());
                        continue; //We're not injecting this guy
                    }
                    FMLLog.log.debug("Registering @EventBusSubscriber for {} for mod {}", targ.getClassName(), mod.getModId());
                    Class<?> subscriptionTarget = Class.forName(targ.getClassName(), false, mcl);
                    MikuLib.MikuEventBus().register(subscriptionTarget);
                    FMLLog.log.debug("Injected @EventBusSubscriber class {}", targ.getClassName());
                }
            } catch (Throwable e) {
                FMLLog.log.error("An error occurred trying to load an EventBusSubscriber {} for modid {}", targ.getClassName(), mod.getModId(), e);
                throw new LoaderException(e);
            }
        }
    }
}
