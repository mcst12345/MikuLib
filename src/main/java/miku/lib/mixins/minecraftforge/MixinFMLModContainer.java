package miku.lib.mixins.minecraftforge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.FormattedMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = FMLModContainer.class, remap = false)
public abstract class MixinFMLModContainer implements ModContainer {
    @Shadow
    private ListMultimap<Class<? extends FMLEvent>, Method> eventMethods;

    @Shadow
    private Object modInstance;


    @Shadow
    private File source;

    @Shadow
    public abstract String getModId();

    @Shadow
    private ModCandidate candidate;

    @Shadow
    private String className;

    @Shadow
    private Set<String> sourceFingerprints;

    @Shadow
    private Map<String, Object> descriptor;

    @Shadow
    private boolean fingerprintNotPresent;

    @Shadow
    private Certificate certificate;

    @Shadow
    private Map<String, String> customModProperties;

    @Shadow
    private Disableable disableability;

    @Shadow
    @Nullable
    protected abstract Method gatherAnnotations(Class<?> clazz);

    @Shadow
    protected abstract ILanguageAdapter getLanguageAdapter();

    @Shadow
    private EventBus eventBus;

    @Shadow
    protected abstract void processFieldAnnotations(ASMDataTable asmDataTable) throws IllegalAccessException;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Subscribe
    public void handleModStateEvent(FMLEvent event) {
        if (!eventMethods.containsKey(event.getClass())) {
            return;
        }
        try {
            for (Method m : eventMethods.get(event.getClass())) {
                try {
                    m.invoke(modInstance, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable t) {
            if (t instanceof ClassCastException || t instanceof NoSuchFieldError || t instanceof NoSuchMethodError)
                return;
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    @Subscribe
    public void constructMod(FMLConstructionEvent event) {
        ModClassLoader modClassLoader = event.getModClassLoader();
        try {
            modClassLoader.addFile(source);
        } catch (MalformedURLException e) {
            FormattedMessage message = new FormattedMessage("{} Failed to add file to classloader: {}", getModId(), source);
            throw new LoaderException(message.getFormattedMessage(), e);
        }
        modClassLoader.clearNegativeCacheFor(candidate.getClassList());

        //Only place I could think to add this...
        MinecraftForge.preloadCrashClasses(event.getASMHarvestedData(), getModId(), candidate.getClassList());

        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, modClassLoader);
        } catch (Throwable t) {
            System.out.println("MikuFATAL:Failed to load modClass:" + className + " it will be ignored.Report this.");
            try {
                long tmp;
                Field mods = Loader.class.getDeclaredField("mods");
                tmp = Launch.UNSAFE.objectFieldOffset(mods);
                List<ModContainer> Mods = (List<ModContainer>) Launch.UNSAFE.getObjectVolatile(Loader.instance(), tmp);
                List<ModContainer> fucked = new ArrayList<>();
                for (ModContainer mc : Mods) {
                    if (!mc.getModId().equals(getModId())) fucked.add(mc);
                }
                Launch.UNSAFE.putObjectVolatile(Loader.instance(), tmp, fucked);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            t.printStackTrace();
            return;
        }

        Certificate[] certificates = clazz.getProtectionDomain().getCodeSource().getCertificates();
        ImmutableList<String> certList = CertificateHelper.getFingerprints(certificates);
        sourceFingerprints = ImmutableSet.copyOf(certList);

        String expectedFingerprint = (String) descriptor.get("certificateFingerprint");

        fingerprintNotPresent = true;

        if (expectedFingerprint != null && !expectedFingerprint.isEmpty()) {
            if (!sourceFingerprints.contains(expectedFingerprint)) {
                Level warnLevel = source.isDirectory() ? Level.TRACE : Level.ERROR;
                FMLLog.log.log(warnLevel, "The mod {} is expecting signature {} for source {}, however there is no signature matching that description", getModId(), expectedFingerprint, source.getName());
            } else {
                certificate = certificates[certList.indexOf(expectedFingerprint)];
                fingerprintNotPresent = false;
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> props = (List<Map<String, String>>) descriptor.get("customProperties");
        if (props != null) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            for (Map<String, String> p : props) {
                builder.put(p.get("k"), p.get("v"));
            }
            customModProperties = builder.build();
        } else {
            customModProperties = EMPTY_PROPERTIES;
        }

        Boolean hasDisableableFlag = (Boolean) descriptor.get("canBeDeactivated");
        boolean hasReverseDepends = !event.getReverseDependencies().get(getModId()).isEmpty();
        if (hasDisableableFlag != null && hasDisableableFlag) {
            disableability = hasReverseDepends ? ModContainer.Disableable.DEPENDENCIES : ModContainer.Disableable.YES;
        } else {
            disableability = hasReverseDepends ? ModContainer.Disableable.DEPENDENCIES : ModContainer.Disableable.RESTART;
        }
        Method factoryMethod = gatherAnnotations(clazz);
        ILanguageAdapter languageAdapter = getLanguageAdapter();
        try {
            modInstance = languageAdapter.getNewInstance((FMLModContainer) (Object) this, clazz, modClassLoader, factoryMethod);
        } catch (Throwable t) {
            System.out.println("MikuWarn:Failed to load new mod instance of " + getModId() + ",it will be ignored.Report this.");
            try {
                long tmp;
                Field mods = Loader.class.getDeclaredField("mods");
                tmp = Launch.UNSAFE.objectFieldOffset(mods);
                List<ModContainer> Mods = (List<ModContainer>) Launch.UNSAFE.getObjectVolatile(Loader.instance(), tmp);
                List<ModContainer> fucked = new ArrayList<>();
                for (ModContainer mc : Mods) {
                    if (!mc.getModId().equals(getModId())) fucked.add(mc);
                }
                Launch.UNSAFE.putObjectVolatile(Loader.instance(), tmp, fucked);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            t.printStackTrace();
        }
        NetworkRegistry.INSTANCE.register(this, clazz, (String) (descriptor.getOrDefault("acceptableRemoteVersions", null)), event.getASMHarvestedData());
        if (fingerprintNotPresent) {
            eventBus.post(new FMLFingerprintViolationEvent(source.isDirectory(), source, ImmutableSet.copyOf(this.sourceFingerprints), expectedFingerprint));
        }
        ProxyInjector.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide(), languageAdapter);
        AutomaticEventSubscriber.inject(this, event.getASMHarvestedData(), FMLCommonHandler.instance().getSide());
        ConfigManager.sync(this.getModId(), Config.Type.INSTANCE);

        try {
            processFieldAnnotations(event.getASMHarvestedData());
        } catch (IllegalAccessException e) {
            FormattedMessage message = new FormattedMessage("{} Failed to process field annotations.", getModId());
            throw new LoaderException(message.getFormattedMessage(), e);
        }
    }
}
