package net.minecraft.launchwrapper;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.apache.logging.log4j.Level;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;

public class Launch {
    public static Unsafe UNSAFE;
    protected static final boolean Miku = true;
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static Map<String, Object> blackboard;
    public static LaunchClassLoader classLoader;
    public static Field Transformers;
    public static final String version = "1.0";
    private Launch() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(System.getProperty("java.home"));

        URLClassLoader ucl = (URLClassLoader) this.getClass().getClassLoader();
        classLoader = new LaunchClassLoader(ucl.getURLs());
        blackboard = new HashMap<>();
        Thread.currentThread().setContextClassLoader(classLoader);


    }

    public static void NoReflection(Class<?> clazz){
        try {
            Method method = Class.class.getDeclaredMethod("reflectionData");
            method.setAccessible(true);
            Object DATA = method.invoke(clazz);
            Class<?> ReflectionData = Class.forName("java.lang.Class$ReflectionData");
            Field declaredFields = ReflectionData.getDeclaredField("declaredFields");
            Field publicFields = ReflectionData.getDeclaredField("publicFields");
            Field declaredPublicFields = ReflectionData.getDeclaredField("declaredPublicFields");
            Field declaredMethods = ReflectionData.getDeclaredField("declaredMethods");
            Field publicMethods = ReflectionData.getDeclaredField("publicMethods");
            Field declaredPublicMethods = ReflectionData.getDeclaredField("declaredPublicMethods");
            long tmp;
            tmp = UNSAFE.objectFieldOffset(declaredFields);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
            tmp = UNSAFE.objectFieldOffset(publicFields);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
            tmp = UNSAFE.objectFieldOffset(declaredPublicFields);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
            tmp = UNSAFE.objectFieldOffset(declaredMethods);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
            tmp = UNSAFE.objectFieldOffset(publicMethods);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
            tmp = UNSAFE.objectFieldOffset(declaredPublicMethods);
            UNSAFE.putObjectVolatile(DATA,tmp,new Field[0]);
        } catch (NoSuchMethodException | ClassNotFoundException | NoSuchFieldException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void FuckNative() {
        try {
            Field nativeContext = ClassLoader.class.getDeclaredField("nativeLibraryContext");
            nativeContext.setAccessible(true);
            Stack stack = (Stack) nativeContext.get(null);
            for (Object item : stack) {
                System.out.println(item.toString());
            }
            Field nativeLibraries = ClassLoader.class.getDeclaredField("nativeLibraries");
            nativeLibraries.setAccessible(true);
            Vector nativeLibrary = (Vector) nativeLibraries.get(Launch.class.getClassLoader());
            for (Object item : nativeLibrary) {
                System.out.println(item.toString());
            }
            Field systemNativeLibrary = ClassLoader.class.getDeclaredField("systemNativeLibraries");
            systemNativeLibrary.setAccessible(true);
            Vector systemNativeLibraries = (Vector) systemNativeLibrary.get(null);
            for (Object item : systemNativeLibraries) {
                System.out.println(item.toString());
            }
            Field loadedLibraryName = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            loadedLibraryName.setAccessible(true);
            Vector loadedLibraryNames = (Vector) loadedLibraryName.get(null);
            for (Object item : loadedLibraryNames) {
                System.out.println(item.toString());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Launch().launch(args);
    }

    public static boolean MikuLibInstalled() {
        try {
            Class<?> MikuLib = Class.forName("miku.lib.common.core.MikuLib", false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected static void LoadMixin(){
        try {
            System.out.println("Loading mixin in javaagent mode.");
            VirtualMachine vm = VirtualMachine.attach(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            vm.loadAgent(System.getProperty("user.dir")+"/libraries/mixin.jar");
            System.out.println("Success!");
        } catch (AgentLoadException | IOException | AgentInitializationException | AttachNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    private void launch(String[] args) {
        LoadMixin();
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        OptionSpec<File> assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg().ofType(File.class);
        OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg().defaultsTo(DEFAULT_TWEAK);
        OptionSpec<String> nonOption = parser.nonOptions();

        OptionSet options = parser.parse(args);
        minecraftHome = options.valueOf(gameDirOption);
        assetsDir = options.valueOf(assetsDirOption);
        String profileName = options.valueOf(profileOption);
        List<String> tweakClassNames = new ArrayList<>(options.valuesOf(tweakClassOption));
        tweakClassNames.add("net.minecraft.launchwrapper.MikuTweaker");
        List<String> argumentList = new ArrayList<>();
        blackboard.put("TweakClasses", tweakClassNames);
        blackboard.put("ArgumentList", argumentList);
        Set<String> allTweakerNames = new HashSet<>();
        List<ITweaker> allTweakers = new ArrayList<>();
        FuckNative();
        try {
            Transformers = classLoader.getClass().getDeclaredField("transformers");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        NoReflection(LaunchClassLoader.class);
        NoReflection(CoreModManager.class);
        try {
            List<ITweaker> tweakers = new ArrayList<>(tweakClassNames.size() + 1);
            blackboard.put("Tweaks", tweakers);
            ITweaker primaryTweaker = null;
            do {
                for (Iterator<String> it = tweakClassNames.iterator(); it.hasNext(); ) {
                    String tweakName = it.next();
                    if (allTweakerNames.contains(tweakName)) {
                        LogWrapper.log(Level.WARN, "Tweak class name %s has already been visited -- skipping", tweakName);
                        it.remove();
                        continue;
                    } else {
                        allTweakerNames.add(tweakName);
                    }
                    LogWrapper.log(Level.INFO, "Loading tweak class name %s", tweakName);
                    classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf('.')));
                    ITweaker tweaker = (ITweaker) Class.forName(tweakName, true, classLoader).getConstructor().newInstance();
                    tweakers.add(tweaker);
                    it.remove();
                    if (primaryTweaker == null) {
                        LogWrapper.log(Level.INFO, "Using primary tweak class name %s", tweakName);
                        primaryTweaker = tweaker;
                    }
                }

                for (Iterator<ITweaker> it = tweakers.iterator(); it.hasNext(); ) {
                    ITweaker tweaker = it.next();
                    LogWrapper.log(Level.INFO, "Calling tweak class %s", tweaker.getClass().getName());
                    tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                    tweaker.injectIntoClassLoader(classLoader);
                    allTweakers.add(tweaker);
                    it.remove();
                }
            } while (!tweakClassNames.isEmpty());

            for (ITweaker tweaker : allTweakers) {
                argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments()));
            }

            String launchTarget;
            if (MikuLibInstalled()) {
                launchTarget = "miku.lib.client.minecraft.Main";
                Class<?> Sqlite = Class.forName("miku.lib.common.sqlite.Sqlite", false, classLoader);
                Method init = Sqlite.getMethod("CoreInit");
                init.invoke(null);
            } else {
                assert primaryTweaker != null;
                launchTarget = primaryTweaker.getLaunchTarget();
            }
            Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Method mainMethod = clazz.getMethod("main", String[].class);

            LogWrapper.info("Launching wrapped minecraft {%s}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[0]));
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "Unable to launch");
            System.exit(1);
        }
    }
}
