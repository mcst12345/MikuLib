package miku.lib.mixins.minecraftforge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import miku.lib.common.core.MikuLib;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.registries.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(value = GameData.class)
public abstract class MixinGameData {
    @Shadow
    private static <T extends IForgeRegistryEntry<T>> void loadPersistentDataToStagingRegistry(RegistryManager pool, RegistryManager to, Map<ResourceLocation, Integer[]> remaps, Map<ResourceLocation, Integer> missing, ResourceLocation name, ForgeRegistry.Snapshot snap, Class<T> regType) {
    }

    @Shadow
    private static <T extends IForgeRegistryEntry<T>> void processMissing(Class<T> clazz, ResourceLocation name, RegistryManager STAGING, RegistryEvent.MissingMappings<?> e, Map<ResourceLocation, Integer> missing, Map<ResourceLocation, Integer[]> remaps, Collection<ResourceLocation> defaulted, Collection<ResourceLocation> failed, boolean injectNetworkDummies) {
    }

    @Shadow
    private static <T extends IForgeRegistryEntry<T>> void loadFrozenDataToStagingRegistry(RegistryManager STAGING, ResourceLocation name, Map<ResourceLocation, Integer[]> remaps, Class<T> clazz) {
    }

    @Shadow
    private static <T extends IForgeRegistryEntry<T>> void loadRegistry(ResourceLocation registryName, RegistryManager from, RegistryManager to, Class<T> regType, boolean freeze) {
    }

    @Shadow
    @Final
    public static ResourceLocation BLOCKS;

    @Shadow
    @Final
    public static ResourceLocation ITEMS;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public static Multimap<ResourceLocation, ResourceLocation> injectSnapshot(Map<ResourceLocation, ForgeRegistry.Snapshot> snapshot, boolean injectFrozenData, boolean isLocalWorld) {
        FMLLog.log.info("Injecting existing registry data into this {} instance", FMLCommonHandler.instance().getEffectiveSide().isServer() ? "server" : "client");
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.validateContent(name));
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.dump(name));
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.resetDelegates());

        if (isLocalWorld) {
            List<ResourceLocation> missingRegs = snapshot.keySet().stream().filter(name -> !RegistryManager.ACTIVE.registries.containsKey(name)).collect(Collectors.toList());
            if (missingRegs.size() > 0) {
                StringBuilder text = new StringBuilder("Forge Mod Loader detected missing/unknown registrie(s).\n\n" +
                        "There are " + missingRegs.size() + " missing registries in this save.\n" +
                        "If you continue the missing registries will get removed.\n" +
                        "This may cause issues, it is advised that you create a world backup before continuing.\n\n" +
                        "Missing Registries:\n");

                for (ResourceLocation s : missingRegs)
                    text.append(s.toString()).append("\n");

                if (!StartupQuery.confirm(text.toString()))
                    StartupQuery.abort();
            }
        }

        RegistryManager STAGING = new RegistryManager("STAGING");

        final Map<ResourceLocation, Map<ResourceLocation, Integer[]>> remaps = Maps.newHashMap();
        final LinkedHashMap<ResourceLocation, Map<ResourceLocation, Integer>> missing = Maps.newLinkedHashMap();
        // Load the snapshot into the "STAGING" registry
        snapshot.forEach((key, value) ->
        {
            final Class<? extends IForgeRegistryEntry> clazz = RegistryManager.ACTIVE.getSuperType(key);
            remaps.put(key, Maps.newLinkedHashMap());
            missing.put(key, Maps.newHashMap());
            loadPersistentDataToStagingRegistry(RegistryManager.ACTIVE, STAGING, remaps.get(key), missing.get(key), key, value, clazz);
        });

        snapshot.forEach((key, value) ->
                value.dummied.forEach(dummy ->
                {
                    Map<ResourceLocation, Integer> m = missing.get(key);
                    ForgeRegistry<?> reg = STAGING.getRegistry(key);

                    // Currently missing locally, we just inject and carry on
                    if (m.containsKey(dummy)) {
                        if (reg.markDummy(dummy, m.get(dummy)))
                            m.remove(dummy);
                    } else if (isLocalWorld) {
                        if (ForgeRegistry.DEBUG)
                            FMLLog.log.debug("Registry {}: Resuscitating dummy entry {}", key, dummy);
                    } else {
                        // The server believes this is a dummy block identity, but we seem to have one locally. This is likely a conflict
                        // in mod setup - Mark this entry as a dummy
                        int id = reg.getID(dummy);
                        FMLLog.log.warn("Registry {}: The ID {} @ {} is currently locally mapped - it will be replaced with a dummy for this session", dummy, key, id);
                        reg.markDummy(dummy, id);
                    }
                }));

        int count = missing.values().stream().mapToInt(Map::size).sum();
        if (count > 0) {
            FMLLog.log.debug("There are {} mappings missing - attempting a mod remap", count);
            Multimap<ResourceLocation, ResourceLocation> defaulted = ArrayListMultimap.create();
            Multimap<ResourceLocation, ResourceLocation> failed = ArrayListMultimap.create();

            missing.entrySet().stream().filter(e -> e.getValue().size() > 0).forEach(m ->
            {
                ResourceLocation name = m.getKey();
                ForgeRegistry<?> reg = STAGING.getRegistry(name);
                RegistryEvent.MissingMappings<?> event = reg.getMissingEvent(name, m.getValue());
                MikuLib.MikuEventBus().post(event);

                List<RegistryEvent.MissingMappings.Mapping<?>> lst = event.getAllMappings().stream().filter(e -> e.getAction() == RegistryEvent.MissingMappings.Action.DEFAULT).sorted(Comparator.comparing((Function<RegistryEvent.MissingMappings.Mapping<?>, String>) Object::toString)).collect(Collectors.toList());
                if (!lst.isEmpty()) {
                    FMLLog.log.error("Unidentified mapping from registry {}", name);
                    lst.forEach(map -> FMLLog.log.error("    {}: {}", map.key, map.id));
                }
                event.getAllMappings().stream().filter(e -> e.getAction() == RegistryEvent.MissingMappings.Action.FAIL).forEach(fail -> failed.put(name, fail.key));

                final Class<? extends IForgeRegistryEntry> clazz = RegistryManager.ACTIVE.getSuperType(name);
                processMissing(clazz, name, STAGING, event, m.getValue(), remaps.get(name), defaulted.get(name), failed.get(name), !isLocalWorld);
            });

            if (!defaulted.isEmpty() && !isLocalWorld)
                return defaulted;

            if (!defaulted.isEmpty()) {
                StringBuilder buf = new StringBuilder();
                buf.append("Forge Mod Loader detected missing registry entries.\n\n")
                        .append("There are ").append(defaulted.size()).append(" missing entries in this save.\n")
                        .append("If you continue the missing entries will get removed.\n")
                        .append("A world backup will be automatically created in your saves directory.\n\n");

                defaulted.asMap().forEach((name, entries) ->
                {
                    buf.append("Missing ").append(name).append(":\n");
                    entries.forEach(rl -> buf.append("    ").append(rl).append("\n"));
                });

                boolean confirmed = StartupQuery.confirm(buf.toString());
                if (!confirmed)
                    StartupQuery.abort();

                try {
                    String skip = System.getProperty("fml.doNotBackup");
                    if (!"true".equals(skip)) {
                        ZipperUtil.backupWorld();
                    } else {
                        for (int x = 0; x < 10; x++)
                            FMLLog.log.error("!!!!!!!!!! UPDATING WORLD WITHOUT DOING BACKUP !!!!!!!!!!!!!!!!");
                    }
                } catch (IOException e) {
                    StartupQuery.notify("The world backup couldn't be created.\n\n" + e);
                    StartupQuery.abort();
                }
            }

            if (!defaulted.isEmpty()) {
                if (isLocalWorld)
                    FMLLog.log.error("There are unidentified mappings in this world - we are going to attempt to process anyway");
            }

        }

        if (injectFrozenData) {
            // If we're loading from disk, we can actually substitute air in the block map for anything that is otherwise "missing". This keeps the reference in the map, in case
            // the block comes back later
            missing.forEach((name, m) ->
            {
                ForgeRegistry<?> reg = STAGING.getRegistry(name);
                m.forEach(reg::markDummy);
            });


            // If we're loading up the world from disk, we want to add in the new data that might have been provisioned by mods
            // So we load it from the frozen persistent registry
            RegistryManager.ACTIVE.registries.forEach((name, reg) ->
            {
                final Class<? extends IForgeRegistryEntry> clazz = RegistryManager.ACTIVE.getSuperType(name);
                loadFrozenDataToStagingRegistry(STAGING, name, remaps.get(name), clazz);
            });
        }

        // Validate that all the STAGING data is good
        STAGING.registries.forEach((name, reg) -> reg.validateContent(name));

        // Load the STAGING registry into the ACTIVE registry
        for (Map.Entry<ResourceLocation, ForgeRegistry<? extends IForgeRegistryEntry<?>>> r : RegistryManager.ACTIVE.registries.entrySet()) {
            final Class<? extends IForgeRegistryEntry> registrySuperType = RegistryManager.ACTIVE.getSuperType(r.getKey());
            loadRegistry(r.getKey(), STAGING, RegistryManager.ACTIVE, registrySuperType, true);
        }

        // Dump the active registry
        RegistryManager.ACTIVE.registries.forEach((name, reg) -> reg.dump(name));

        // Tell mods that the ids have changed
        Loader.instance().fireRemapEvent(remaps, false);

        // The id map changed, ensure we apply object holders
        ObjectHolderRegistry.INSTANCE.applyObjectHolders();

        // Return an empty list, because we're good
        return ArrayListMultimap.create();
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void fireCreateRegistryEvents() {
        MikuLib.MikuEventBus().post(new RegistryEvent.NewRegistry());
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void fireRegistryEvents(Predicate<ResourceLocation> filter) {
        List<ResourceLocation> keys = Lists.newArrayList(RegistryManager.ACTIVE.registries.keySet());
        keys.sort((o1, o2) -> o1.toString().compareToIgnoreCase(o2.toString()));

        if (filter.test(BLOCKS)) {
            MikuLib.MikuEventBus().post(RegistryManager.ACTIVE.getRegistry(BLOCKS).getRegisterEvent(BLOCKS));
            ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject any blocks
        }
        if (filter.test(ITEMS)) {
            MikuLib.MikuEventBus().post(RegistryManager.ACTIVE.getRegistry(ITEMS).getRegisterEvent(ITEMS));
            ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject any items
        }
        for (ResourceLocation rl : keys) {
            if (!filter.test(rl)) continue;
            if (rl == BLOCKS || rl == ITEMS) continue;
            MikuLib.MikuEventBus().post(RegistryManager.ACTIVE.getRegistry(rl).getRegisterEvent(rl));
        }
        ObjectHolderRegistry.INSTANCE.applyObjectHolders(); // inject everything else

    }
}
