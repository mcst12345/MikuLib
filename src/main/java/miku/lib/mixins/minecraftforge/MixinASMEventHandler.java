package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.util.EntityUtil;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.*;
import org.apache.logging.log4j.ThreadContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Type;

@Mixin(value = ASMEventHandler.class, remap = false)
public class MixinASMEventHandler {
    @Shadow
    @Final
    private static boolean GETCONTEXT;

    @Shadow
    private ModContainer owner;

    @Shadow
    @Final
    private IEventListener handler;

    @Shadow
    private Type filter;

    @Shadow
    @Final
    private SubscribeEvent subInfo;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public void invoke(Event event) {
        if (EntityUtil.isKilling() || MikuInsaneMode.isMikuInsaneMode()) return;
        if (GETCONTEXT)
            ThreadContext.put("mod", owner == null ? "" : owner.getName());
        if (handler != null) {
            if (!event.isCancelable() || !event.isCanceled() || subInfo.receiveCanceled()) {
                if (filter == null || filter == ((IGenericEvent<?>) event).getGenericType()) {
                    try {
                        handler.invoke(event);
                    } catch (Throwable t) {
                        System.out.println("MikuWarn:Catch exception when invoking event:" + event.getClass());
                    }
                }
            }
        }
        if (GETCONTEXT)
            ThreadContext.remove("mod");
    }
}
