package miku.lib.core;

import miku.lib.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MikuTweaker implements ITweaker {
    //Holy Fuck. Well,at least this is better than that package-based whitelist.
    protected static final String[] mod_id_white_list = {"jei", "zollerngalaxy", "xaerominimap", "variedcommodities", "universaltweaks", "twilightforest", "tragicmc", "torcherino", "vm", "tickratechanger",
            "tickdynamic", "sweetmagic", "stevekung's_lib", "srparasites", "spaceambient", "smoothfont", "flammpfeil.slashblade", "shutupmodelloader", "scp", "redstoneflux", "randompatches", "projecteintegration",
            "projecte", "placebo", "phosphor-lighting", "performant", "patchouli", "particleculling", "openterraingenerator", "oldjava", "neid", "non_update", "moreplanets", "testdummy", "mikulib", "mikulib_sqlite",
            "miku", "memorycleaner", "maze", "matteroverdrive", "manaita_plus", "lovely_robot", "lostcities", "letmedespawn", "jeid", "rejoymod", "ic2", "ilib", "hammercore", "getittogetherdrops", "galaxyspace",
            "galacticraftcore", "fpsreducer", "foamfix", "fastbench", "fastfurnace", "expequiv", "entityculling", "ageofminecraft", "ageofabyssalcraft", "ageofchaos", "ageofmutants", "enderio", "enderiobase",
            "enderioconduits", "enderiopowertools", "enderioconduitsappliedenergistics", "enderioconduitsopencomputers", "enderioconduitsrefinedstorage", "enderiointegrationforestry", "enderiointegrationtic",
            "enderiointegrationticlate", "enderioinvpanel", "enderiomachines", "endercore", "draconicevolution", "customnpcsfix", "customnpcs", "ctm", "codechickenlib", "clumps", "clearwater", "loliasm", "brandonscore",
            "betterbiomeblend", "bedbreakbegone", "avaritia", "asmodeuscore", "appliedenergistics2", "aiimprovements", "abyssalcraft", "magic_maid"};
    protected static final List<String> TransformerExclusions = new ArrayList<>();
    protected static final List<String> MinecraftClasses = new ArrayList<>();
    public static Map<String, Class<?>> cachedClasses = null;

    public MikuTweaker() throws IOException, NoSuchFieldException, IllegalAccessException {
        InitSqlite();
        Field transformers = Launch.classLoader.getClass().getDeclaredField("transformers");
        transformers.setAccessible(true);
        List<IClassTransformer> t = (List<IClassTransformer>) transformers.get(Launch.classLoader);
        if (!(t instanceof MikuArrayListForTransformer)) {
            MikuArrayListForTransformer<IClassTransformer> fucked = new MikuArrayListForTransformer<IClassTransformer>(2);
            for (IClassTransformer i : t) fucked.add(i);
            System.out.println("Fucking LaunchClassLoader.");
            transformers.set(Launch.classLoader, fucked);//Fuck other transformers.
        }
        Field cachedClasses = Launch.classLoader.getClass().getDeclaredField("cachedClasses");
        cachedClasses.setAccessible(true);
        MikuTweaker.cachedClasses = (Map<String, Class<?>>) cachedClasses.get(Launch.classLoader);

        File minecraft = new File(System.getProperty("minecraft.client.jar"));
        AddMinecraftJarToTransformerExclusions(minecraft);


        File mods = new File("mods");
        if (!mods.exists()) {
            if (mods.mkdir()) return;
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        } else if (!mods.isDirectory()) {
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        }

        ScanMods(mods);

    }

    private static void AddMinecraftJarToTransformerExclusions(File file) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory()) {
                    if (jarEntry.getName().matches("(.*).class")) {
                        String clazz = jarEntry.getName().replace("/", ".").replace(".class", "");
                        if (clazz.equals("module-info")) continue;
                        MikuTweaker.MinecraftClasses.add(clazz);
                    }
                }
            }
        }
    }

    protected static void ScanJarFile(File file) throws IOException {
        if (file.getName().matches("(.*).jar")) {
            try (JarFile jar = new JarFile(file)) {
                System.out.println("Reading jar file:" + jar.getName());
                List<String> classes = new ArrayList<>();
                boolean good = false;
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (!jarEntry.isDirectory()) {
                        if (jarEntry.getName().matches("(.*).class")) {
                            String clazz = jarEntry.getName().replace("/", ".").replace(".class", "");
                            if (clazz.equals("module-info")) continue;
                            classes.add(clazz);
                            InputStream classStream = jar.getInputStream(jarEntry);
                            if (classStream == null) continue;
                            try {
                                ClassReader cr = new ClassReader(classStream);
                                ClassNode cn = new ClassNode();
                                cr.accept(cn, 0);
                                if (cn.visibleAnnotations != null) for (AnnotationNode an : cn.visibleAnnotations) {
                                    if (an.desc.equals("Lnet/minecraftforge/fml/common/Mod;")) {
                                        boolean flag = false;
                                        //System.out.println(an.values.toString());
                                        String modid = null;
                                        for (Object o : an.values) {
                                            String s = (String) o;
                                            if (flag) {
                                                modid = s;
                                                break;
                                            }
                                            if (s.equals("modid")) {
                                                flag = true;
                                            }
                                        }
                                        if (modid == null) {
                                            System.out.println("The fuck?");
                                            FMLCommonHandler.instance().exitJava(0, true);
                                        } else {
                                            System.out.println(modid);
                                            for (String s : mod_id_white_list) {
                                                if (s.equals(modid)) {
                                                    good = true;
                                                    break;
                                                }
                                            }
                                        }

                                    }
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                                System.out.println("Ignore class file:" + clazz);
                            }
                        } else if (jarEntry.getName().matches("(.*)mcmod.info")) {
                            //TODO ?
                        }
                    }
                }
                if (good) {
                    System.out.println("Adding mod " + jar.getName() + " to TransformerExclusions");
                    TransformerExclusions.addAll(classes);
                }
            }
        }
    }

    protected static void ScanMods(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                System.out.println("Scanning directory:" + file.getName());
                ScanMods(file);
            } else ScanJarFile(file);
        }
    }

    protected void InitSqlite() throws IOException {
        boolean flag = true;
        for (File file : Objects.requireNonNull(new File("mods").listFiles())) {
            if (file.getName().equals("MikuLib-SQlite-1.0.jar")) {//Check is the sqlite loader installed.
                flag = false;
                break;
            }
        }
        if (flag) {
            System.out.println("MikuLib's sqlite loader doesn't exists,extract it.");
            InputStream stream = MikuTweaker.class.getResourceAsStream("/MikuLib-SQlite-1.0.jar");
            assert stream != null;
            byte[] file = new byte[stream.available()];

            stream.read(file);
            stream.close();
            FileOutputStream outputStream = new FileOutputStream("mods/MikuLib-SQlite-1.0.jar");
            outputStream.write(file);//extracted the file.
            outputStream.close();
            System.out.println("MikuLib has just extracted the sqlite loader of it. Please restart the game.");
            FMLCommonHandler.instance().exitJava(0, true);
        }
    }

    private String[] args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        String[] additionArgs = {"--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDir.getAbsolutePath(), "--version", profile};
        List<String> fullArgs = new ArrayList<>();
        fullArgs.addAll(args);
        fullArgs.addAll(Arrays.asList(additionArgs));
        this.args = fullArgs.toArray(new String[fullArgs.size()]);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        System.out.println("Add MikuTransformer");

        //Add our transformer
        classLoader.registerTransformer("miku.lib.core.MikuTransformer");
        //classLoader.registerTransformer("miku.lib.core.AccessTransformer");
        try {
            CoreModManager.getIgnoredMods().remove(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getName());
            //remove us from the CoreMod ignored list.
        } catch (Throwable ignored) {}
        System.out.println("Init mixins");
        MixinBootstrap.init();
        //Add Mixin configs.
        Mixins.addConfiguration("mixins.minecraft.json");
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.forge.json");
    }

    @Override
    public String getLaunchTarget() {
        return "miku.lib.util.Main";//No usage.
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

    public static boolean isGoodClass(String s) {
        for (String c : TransformerExclusions) {
            if (s.matches("(.*)" + c + "(.*)")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMinecraftClass(String s) {
        for (String c : MinecraftClasses) {
            if (s.matches("(.*)" + c + "(.*)")) {
                return true;
            }
        }
        return false;
    }
}
