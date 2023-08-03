package miku.lib.mixins.minecraft;

import miku.lib.common.command.MikuInsaneMode;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.concurrent.FutureTask;

@Mixin(value = Util.class)
public class MixinUtil {
    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Nullable
    public static <V> V runTask(FutureTask<V> task, Logger logger) {
        if (task == null || MikuInsaneMode.isMikuInsaneMode()) return null;
        try {
            task.run();
            return task.get();
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception when running task:" + task.getClass());
            t.printStackTrace();
        }

        return null;
    }
}
