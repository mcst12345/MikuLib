package miku.lib.mixins.minecraftforge;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Mixin(value = EnumHelper.class)
public class MixinEnumHelper {
    @Shadow
    private static boolean isSetup;

    @Shadow
    private static Object reflectionFactory;

    @Shadow
    private static Method newConstructorAccessor;

    @Shadow
    private static Method newInstance;

    @Shadow
    private static Method newFieldAccessor;

    @Shadow
    private static Method fieldAccessorSet;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private static void setup() {
        if (isSetup) {
            return;
        }

        try {
            reflectionFactory = Launch.getReflectionFactory();
            newConstructorAccessor = Launch.getNewConstructorAccessor();
            newInstance = Launch.getNewInstance();
            newFieldAccessor = Launch.getNewFieldAccessor();
            fieldAccessorSet = Launch.getFieldAccessorSet();
        } catch (Exception e) {
            FMLLog.log.error("Error setting up EnumHelper.", e);
        }

        isSetup = true;
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public static void setFailsafeFieldValue(Field field, @Nullable Object target, @Nullable Object value) throws Exception {
        Launch.getModifiersField().setInt(field, field.getModifiers() & ~Modifier.FINAL);
        Object fieldAccessor = newFieldAccessor.invoke(reflectionFactory, field, false);
        fieldAccessorSet.invoke(fieldAccessor, target, value);
    }
}
