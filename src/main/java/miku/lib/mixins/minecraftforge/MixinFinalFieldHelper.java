package miku.lib.mixins.minecraftforge;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.registries.ObjectHolderRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mixin(value = ObjectHolderRef.FinalFieldHelper.class, remap = false)
public class MixinFinalFieldHelper {


    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    static Field makeWritable(Field f) throws ReflectiveOperationException {
        f.setAccessible(true);
        Launch.getModifiersField().setInt(f, f.getModifiers() & ~Modifier.FINAL);
        return f;
    }

    /**
     * @author mct12345
     * @reason Fuck!
     */
    @Overwrite
    static void setField(Field field, @Nullable Object instance, Object thing) throws ReflectiveOperationException {
        Object fieldAccessor = Launch.getNewFieldAccessor().invoke(Launch.getReflectionFactory(), field, false);
        Launch.getFieldAccessorSet().invoke(fieldAccessor, instance, thing);
    }
}
