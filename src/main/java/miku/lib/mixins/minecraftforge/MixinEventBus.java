package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import miku.lib.common.api.iEventBus;
import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.util.EntityUtil;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = EventBus.class, remap = false)
public class MixinEventBus implements iEventBus {

    private boolean Shutdown;

    @Shadow
    @Final
    private int busID;

    @Shadow
    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners;

    @Shadow
    private Map<Object, ModContainer> listenerOwners;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public boolean post(Event event) {
        if (EntityUtil.isKilling() || MikuInsaneMode.isMikuInsaneMode()) return false;
        if (event instanceof EntityJoinWorldEvent) {
            if (EntityUtil.isProtected(((EntityJoinWorldEvent) event).getEntity())) return false;
        }
        if (event instanceof PlayerInteractEvent) {
            if (EntityUtil.isProtected(((PlayerInteractEvent) event).getEntityPlayer()))
                return false;
        }
        if (Shutdown) return false;

        IEventListener[] listeners = event.getListenerList().getListeners(busID);
        int index = 0;
        try
        {
            for (; index < listeners.length; index++)
            {
                try {
                    listeners[index].invoke(event);
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Catch exception when posting event:" + event.getClass());
                    t.printStackTrace();
                }
            }
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception when posting event:" + event.getClass());
            t.printStackTrace();
        }
        return event.isCancelable() && event.isCanceled();
    }

    /**
     * @author mcst12345
     * @reason fuck you all
     */
    @Overwrite
    public void shutdown() {
    }

    @Override
    public void Shutdown() {
        FMLLog.log.warn("EventBus {} shutting down - future events will not be posted.", busID);
        Shutdown = true;
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void register(Object target) {
        if (listeners.containsKey(target)) {
            return;
        }

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null) {
            FMLLog.log.error("Unable to determine registrant mod for {}. This is a critical error and should be impossible", target, new Throwable());
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        listenerOwners.put(target, activeModContainer);
        boolean isStatic = target.getClass() == Class.class;
        @SuppressWarnings("unchecked")
        Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>) target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
        for (Method method : (isStatic ? (Class<?>) target : target.getClass()).getMethods()) {
            if (isStatic && !Modifier.isStatic(method.getModifiers()))
                continue;
            else if (!isStatic && Modifier.isStatic(method.getModifiers()))
                continue;

            for (Class<?> cls : supers) {
                try {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(SubscribeEvent.class)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1) {
                            throw new IllegalArgumentException(
                                    "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                                            " arguments.  Event handler methods must require a single argument."
                            );
                        }

                        Class<?> eventType = parameterTypes[0];

                        if (!Event.class.isAssignableFrom(eventType)) {
                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                        }

                        register(eventType, target, real, activeModContainer);
                        break;
                    }
                } catch (NoSuchMethodException e) {
                    // Eat the error, this is not unexpected
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void register(Class<?> eventType, Object target, Method method, final ModContainer owner) {
        try {
            Constructor<?> ctr = eventType.getConstructor();
            ctr.setAccessible(true);
            Event event = (Event) ctr.newInstance();
            final ASMEventHandler asm = new ASMEventHandler(target, method, owner, IGenericEvent.class.isAssignableFrom(eventType));

            IEventListener listener = asm;
            if (IContextSetter.class.isAssignableFrom(eventType)) {
                listener = event1 -> {
                    ModContainer old = Loader.instance().activeModContainer();
                    Loader.instance().setActiveModContainer(owner);
                    ((IContextSetter) event1).setModContainer(owner);
                    asm.invoke(event1);
                    Loader.instance().setActiveModContainer(old);
                };
            }

            event.getListenerList().register(busID, asm.getPriority(), listener);

            ArrayList<IEventListener> others = listeners.computeIfAbsent(target, k -> new ArrayList<>());
            others.add(listener);
        } catch (Exception e) {
            FMLLog.log.error("Error registering event handler: {} {} {}", owner, eventType, method, e);
        }
    }
}
