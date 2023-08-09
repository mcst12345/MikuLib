package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import miku.lib.common.core.MikuLib;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.item.Item;
import net.minecraftforge.client.ForgeClientHandler;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.network.ForgeNetworkHandler;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.json.JsonAnnotationLoader;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Mixin(value = ForgeModContainer.class)
public abstract class MixinForgeModContainer extends DummyModContainer implements WorldAccessContainer {
    @Shadow
    @Final
    static Logger log;

    @Shadow
    public UniversalBucket universalBucket;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Subscribe
    public void modConstruction(FMLConstructionEvent evt) {
        InputStream is = ForgeModContainer.class.getResourceAsStream("/META-INF/vanilla_annotations.json");
        try {
            if (is != null)
                JsonAnnotationLoader.loadJson(is, null, evt.getASMHarvestedData());
            log.debug("Loading Vanilla annotations: " + is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        List<String> all = Lists.newArrayList();
        for (ASMDataTable.ASMData asm : evt.getASMHarvestedData().getAll(ICrashReportDetail.class.getName().replace('.', '/')))
            all.add(asm.getClassName());
        for (ASMDataTable.ASMData asm : evt.getASMHarvestedData().getAll(ICrashCallable.class.getName().replace('.', '/')))
            all.add(asm.getClassName());
        // Add table classes for mod list tabulation
        all.add("net/minecraftforge/common/util/TextTable");
        all.add("net/minecraftforge/common/util/TextTable$Column");
        all.add("net/minecraftforge/common/util/TextTable$Row");
        all.add("net/minecraftforge/common/util/TextTable$Alignment");

        all.removeIf(cls -> !cls.startsWith("net/minecraft/") && !cls.startsWith("net/minecraftforge/"));

        log.debug("Preloading CrashReport Classes");
        Collections.sort(all); //Sort it because I like pretty output ;)
        for (String name : all) {
            log.debug("\t{}", name);
            try {
                Class.forName(name.replace('/', '.'), false, MinecraftForge.class.getClassLoader());
            } catch (Exception e) {
                log.error("Could not find class for name '{}'.", name, e);
            }
        }

        NetworkRegistry.INSTANCE.register(this, this.getClass(), "*", evt.getASMHarvestedData());
        ForgeNetworkHandler.registerChannel((ForgeModContainer) (Object) this, evt.getSide());
        ConfigManager.sync(this.getModId(), Config.Type.INSTANCE);
        MikuLib.MikuEventBus().register(this);
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    @Subscribe
    public void preInit(FMLPreInitializationEvent evt) {
        CapabilityItemHandler.register();
        CapabilityFluidHandler.register();
        CapabilityAnimation.register();
        CapabilityEnergy.register();
        MikuLib.MikuEventBus().register(MinecraftForge.INTERNAL_HANDLER);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            MikuLib.MikuEventBus().register(ForgeClientHandler.class);
        }
        ForgeChunkManager.captureConfig(evt.getModConfigurationDirectory());
        MikuLib.MikuEventBus().register(this);

        if (!ForgeModContainer.disableVersionCheck) {
            ForgeVersion.startVersionCheck();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    @SubscribeEvent
    public void registrItems(RegistryEvent.Register<Item> event) {
        // Add and register the forge universal bucket, if it's enabled
        if (FluidRegistry.isUniversalBucketEnabled()) {
            universalBucket = new UniversalBucket();
            universalBucket.setTranslationKey("forge.bucketFilled");
            event.getRegistry().register(universalBucket.setRegistryName(ForgeVersion.MOD_ID, "bucketFilled"));
            MikuLib.MikuEventBus().register(universalBucket);
        }
    }
}
