package miku.lib.mixins.minecraftforge;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.registries.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.BitSet;
import java.util.Set;

@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class MixinForgeRegistry<V extends IForgeRegistryEntry<V>> implements IForgeRegistryInternal<V>, IForgeRegistryModifiable<V> {
    @Shadow
    @Final
    private BitSet availabilityMap;

    @Shadow
    @Final
    private int min;

    @Shadow
    @Final
    private int max;

    @Shadow
    protected abstract V getRaw(ResourceLocation key);

    @Shadow
    @Final
    private Class<V> superType;

    @Shadow
    public abstract int getID(V value);

    @Shadow
    @Final
    private boolean allowOverrides;

    @Shadow
    @Final
    public static boolean DEBUG;

    @Shadow
    @Final
    private BiMap<Integer, V> ids;

    @Shadow
    @Final
    private ResourceLocation defaultKey;

    @Shadow
    private V defaultValue;

    @Shadow
    @Final
    private BiMap<ResourceLocation, V> names;

    @Shadow
    @Final
    private BiMap<ForgeRegistry.OverrideOwner, V> owners;

    @Shadow
    @Final
    private boolean isDelegated;

    @Shadow
    protected abstract RegistryDelegate<V> getDelegate(V thing);

    @Shadow
    @Final
    private Multimap<ResourceLocation, V> overrides;

    @Shadow
    @Final
    private RegistryManager stage;

    @Shadow
    @Final
    private AddCallback<V> add;

    @Shadow
    @Final
    private Set<ResourceLocation> dummies;

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    int add(int id, V value, String owner) {
        if (add instanceof GameData.BlockCallbacks) {
            if (!(value instanceof Block)) return -1;
        }
        if (add instanceof GameData.ItemCallbacks) {
            if (!(value instanceof Item)) return -1;
        }

        ResourceLocation key = value == null ? null : value.getRegistryName();
        Preconditions.checkNotNull(key, "Can't use a null-name for the registry, object %s.", value);
        Preconditions.checkNotNull(value, "Can't add null-object to the registry, name %s.", key);

        int idToUse = id;
        if (idToUse < 0 || availabilityMap.get(idToUse))
            idToUse = availabilityMap.nextClearBit(min);

        if (idToUse > max)
            throw new RuntimeException(String.format("Invalid id %d - maximum id range exceeded.", idToUse));

        V oldEntry = getRaw(key);
        if (oldEntry == value) // already registered, return prev registration's id
        {
            FMLLog.bigWarning("Registry {}: The object {} has been registered twice for the same name {}.", this.superType.getSimpleName(), value, key);
            return this.getID(value);
        }
        if (oldEntry != null) // duplicate name
        {
            if (!this.allowOverrides)
                throw new IllegalArgumentException(String.format("The name %s has been registered twice, for %s and %s.", key, getRaw(key), value));
            if (owner == null)
                throw new IllegalStateException(String.format("Could not determine owner for the override on %s. Value: %s", key, value));
            if (DEBUG)
                FMLLog.log.debug("Registry {} Override: {} {} -> {}", this.superType.getSimpleName(), key, oldEntry, value);
            idToUse = this.getID(oldEntry);
        }

        Integer foundId = this.ids.inverse().get(value); //Is this ever possible to trigger with otherThing being different?
        if (foundId != null) {
            V otherThing = this.ids.get(foundId);
            throw new IllegalArgumentException(String.format("The object %s{%x} has been registered twice, using the names %s and %s. (Other object at this id is %s{%x})", value, System.identityHashCode(value), getKey(value), key, otherThing, System.identityHashCode(otherThing)));
        }

        if (isLocked())
            throw new IllegalStateException(String.format("The object %s (name %s) is being added too late.", value, key));

        if (defaultKey != null && defaultKey.equals(key)) {
            if (this.defaultValue != null)
                throw new IllegalStateException(String.format("Attemped to override already set default value. This is not allowed: The object %s (name %s)", value, key));
            this.defaultValue = value;
        }

        this.names.put(key, value);
        this.ids.put(idToUse, value);
        this.availabilityMap.set(idToUse);
        this.owners.put(new ForgeRegistry.OverrideOwner(owner == null ? key.getNamespace() : owner, key), value);

        if (isDelegated) {
            getDelegate(value).setName(key);
            if (oldEntry != null) {
                if (!this.overrides.get(key).contains(oldEntry))
                    this.overrides.put(key, oldEntry);
                this.overrides.get(key).remove(value);
                if (this.stage == RegistryManager.ACTIVE)
                    getDelegate(value).changeReference(value);
            }
        }

        if (this.add != null)
            this.add.onAdd(this, this.stage, idToUse, value, oldEntry);

        if (this.dummies.remove(key) && DEBUG)
            FMLLog.log.debug("Registry {} Dummy Remove: {}", this.superType.getSimpleName(), key);

        if (DEBUG)
            FMLLog.log.trace("Registry {} add: {} {} {} (req. id {})", this.superType.getSimpleName(), key, idToUse, value, id);

        return idToUse;
    }
}
