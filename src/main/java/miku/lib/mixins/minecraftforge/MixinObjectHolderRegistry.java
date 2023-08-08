package miku.lib.mixins.minecraftforge;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.registries.ObjectHolderRef;
import net.minecraftforge.registries.ObjectHolderRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

@Mixin(value = ObjectHolderRegistry.class)
public abstract class MixinObjectHolderRegistry {
    @Shadow
    protected abstract void scanClassForFields(Map<String, String> classModIds, String className, String value, Class<?> clazz, boolean extractFromExistingValues);

    @Shadow
    protected abstract void addHolderReference(ObjectHolderRef ref);

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    private void scanTarget(Map<String, String> classModIds, Map<String, Class<?>> classCache, String className, @Nullable String annotationTarget, String value, boolean isClass, boolean extractFromValue) {
        Class<?> clazz;
        if (classCache.containsKey(className)) {
            clazz = classCache.get(className);
        } else {
            try {
                clazz = Class.forName(className, extractFromValue, getClass().getClassLoader());
                classCache.put(className, clazz);
            } catch (ClassNotFoundException ex) {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
        if (isClass) {
            scanClassForFields(classModIds, className, value, clazz, extractFromValue);
        } else {
            if (value.indexOf(':') == -1) {
                String prefix = classModIds.get(className);
                if (prefix == null) {
                    FMLLog.log.warn("Found an unqualified ObjectHolder annotation ({}) without a modid context at {}.{}, ignoring", value, className, annotationTarget);
                    throw new IllegalStateException("Unqualified reference to ObjectHolder");
                }
                value = prefix + ":" + value;
            }
            try {
                Field f = clazz.getDeclaredField(annotationTarget);
                addHolderReference(new ObjectHolderRef(f, new ResourceLocation(value), extractFromValue));
            } catch (NoSuchFieldException ex) {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
    }
}
