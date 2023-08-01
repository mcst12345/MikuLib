package miku.lib.common.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class ClassUtil {

    protected static final Map<String, Boolean> GoodClassCache = new ConcurrentSkipListMap<>();
    protected static final Map<String, Boolean> MinecraftClassCache = new ConcurrentSkipListMap<>();
    protected static final Map<String, Boolean> LibraryClassCache = new ConcurrentSkipListMap<>();

    //Holy Fuck. Well,at least this is better than that package-based whitelist.
    protected static final String[] mod_id_white_list = {"jei", "zollerngalaxy", "xaerominimap", "variedcommodities", "universaltweaks", "twilightforest", "tragicmc", "torcherino", "vm", "tickratechanger",
            "tickdynamic", "sweetmagic", "stevekung's_lib", "srparasites", "spaceambient", "smoothfont", "flammpfeil.slashblade", "shutupmodelloader", "scp", "redstoneflux", "randompatches", "projecteintegration",
            "projecte", "placebo", "phosphor-lighting", "performant", "patchouli", "particleculling", "openterraingenerator", "oldjava", "neid", "non_update", "moreplanets", "testdummy", "mikulib", "mikulib_sqlite",
            "miku", "memorycleaner", "maze", "matteroverdrive", "manaita_plus", "lovely_robot", "lostcities", "letmedespawn", "jeid", "rejoymod", "ic2", "ilib", "hammercore", "getittogetherdrops", "galaxyspace",
            "galacticraftcore", "fpsreducer", "foamfix", "fastbench", "fastfurnace", "expequiv", "entityculling", "ageofminecraft", "ageofabyssalcraft", "ageofchaos", "ageofmutants", "enderio", "enderiobase",
            "enderioconduits", "enderiopowertools", "enderioconduitsappliedenergistics", "enderioconduitsopencomputers", "enderioconduitsrefinedstorage", "enderiointegrationforestry", "enderiointegrationtic",
            "enderiointegrationticlate", "enderioinvpanel", "enderiomachines", "endercore", "draconicevolution", "customnpcsfix", "customnpcs", "ctm", "codechickenlib", "clumps", "clearwater", "loliasm", "brandonscore",
            "betterbiomeblend", "bedbreakbegone", "avaritia", "asmodeuscore", "appliedenergistics2", "aiimprovements", "abyssalcraft", "magic_maid", "iceandfire", "llibrary", "carianstyle", "avp", "mdxlib", "endermail",
            "thebetweenlands", "forestry", "skillful", "skillsvanilla", "musicplayer", "projectex", "extendedexchange", "tamedmonster", "hormone", "brownmooshrooms", "pubgmc", "configuration", "vanillafix", "surge", "gregtech",
            "mwc", "inputmethodblocker", "mts", "mtsofficalpack", "overlast", "farlanders", "srmonstress", "scapeandrunvaccine", "wyrmsofnyrus", "roughmobsrevamped", "sereneseasons", "immersive-intelligence", "immersiveintelligence",
            "immersiveengineering", "techguns", "synlib", "geckolib3", "thermalfoundation", "cofhworld", "thermaldynamics", "thermalinnovation", "thermalcultivation", "thermalexpansion", "evilcraft", "bloodmagic", "moarsigns",
            "opencomputers", "immersivepetroleum", "immersiveposts", "warpdrive", "tconstruct", "securitycraft", "gamestages", "serenetweaks", "vampirism_integrations", "ichunutil", "morphspellpack", "baubles", "thaumcraft", "botania",
            "vampiresneedumbrellas", "consecration", "xreliquary", "bewitchment", "tammodized", "voidcraft", "aov", "classicbar", "toughasnails", "toroquest", "acintegration", "morph", "biomesoplenty", "nei", "mca", "tofucraft",
            "harvestcraft", "darknesslib", "grue"};

    protected static final String[] coremod_white_list = {"llibrary", "MixinBooter", "non_update", "OpenEyePlugin", "RandomPatches"};

    protected static final String[] coremod_class_white_list = {"com/enderio/core/common/transform/EnderCorePlugin"};//Holy Fuck.Why don't you write a @Name() annotation?


    protected static final List<String> TransformerExclusions = new ArrayList<>();
    protected static final List<String> MinecraftClasses = new ArrayList<>();
    protected static final List<String> LibraryClasses = new ArrayList<>();
    public static Map<String, Class<?>> cachedClasses = null;

    public static void AddJarToTransformerExclusions(File file, List<String> list, Map<String, Boolean> map) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory()) {
                    if (jarEntry.getName().matches("(.*).class")) {
                        String clazz = jarEntry.getName().replace("/", ".").replace(".class", "");
                        if (clazz.equals("module-info")) continue;
                        list.add(clazz);
                        System.out.println("Adding class "+clazz+"to transformer exclusions");
                        map.put(clazz, true);
                    }
                }
            }
        } catch (ZipException e) {
            System.out.println("Ignore file:" + file.getName());
        }
    }

    protected static boolean DisablejarFucker = false;

    protected static boolean isGoodCoremodClass(String s) {
        for (String c : coremod_class_white_list) {
            if (c.equals(s)) return true;
        }
        return false;
    }

    protected static boolean isGoodCoremod(String s) {
        for (String c : coremod_white_list) {
            if (c.equals(s)) return true;
        }
        return false;
    }

    protected static boolean LOADED = false;

    public synchronized static void ScanModJarFile(File file) throws IOException {
        if (file.getName().endsWith(".jar")) {
            boolean fucked = false;
            try (JarFile jar = new JarFile(file)) {
                System.out.println("Reading jar file:" + jar.getName());

                List<String> classes = new ArrayList<>();
                boolean good = false;
                Enumeration<JarEntry> entries = jar.entries();
                loop:
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (!jarEntry.isDirectory()) {
                        if (jarEntry.getName().endsWith(".class")) {
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

                                    } else {
                                        if (an.desc.equals("Lnet/minecraftforge/fml/relauncher/IFMLLoadingPlugin$Name;")) {
                                            for (Object o : an.values) {
                                                String s = o.toString();
                                                if (isGoodCoremod(s)) {
                                                    good = true;
                                                    break loop;
                                                }
                                            }
                                        }
                                    }
                                }

                                if (cn.interfaces != null) {
                                    for (String s : cn.interfaces) {
                                        if (s.equals("net/minecraftforge/fml/relauncher/IFMLLoadingPlugin")) {
                                            if (isGoodCoremodClass(cn.name)) {
                                                good = true;
                                                break;
                                            }
                                        }
                                    }
                                }

                            } catch (Throwable e) {
                                System.out.println("Ignore class file:" + clazz);
                            }
                        } else if (jarEntry.getName().matches("(.*)mcmod.info")) {
                            //TODO ?
                        } else if (jarEntry.getName().equals("META-INF/MANIFEST.MF")) {
                            try (InputStream is = jar.getInputStream(jarEntry)) {
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader br = new BufferedReader(isr);
                                String str;
                                while ((str = br.readLine()) != null) {
                                    str = str + "\n";
                                    if (str.contains("Fucked: true")) {
                                        fucked = true;
                                        good = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (good) {
                    System.out.println("Adding mod " + jar.getName() + " to TransformerExclusions");
                    TransformerExclusions.addAll(classes);
                    for (String str : classes) {
                        GoodClassCache.put(str, true);
                    }
                } else if (!fucked && !DisablejarFucker) {
                    JarFucker.FuckModJar(jar);
                } else {
                    System.out.println(jar.getName() + " has already being fucked.");
                }
            }
        }
    }

    public static synchronized boolean Init() throws IOException {
        if (LOADED) return false;
        if (System.getProperty("DisableJarFucker").equals("true")) {
            DisablejarFucker = true;
        }

        File minecraft = new File(System.getProperty("user.dir").replace(".minecraft", "") + System.getProperty("minecraft.client.jar").substring(System.getProperty("minecraft.client.jar").indexOf(".minecraft")));
        minecraft.setReadable(true);
        minecraft.setWritable(true);
        AddJarToTransformerExclusions(minecraft, MinecraftClasses, MinecraftClassCache);

        File libraires = new File("libraries");
        libraires.setWritable(true);
        libraires.setWritable(true);
        CreateDirectory(libraires);

        ScanLibraries(libraires);

        File mods = new File("mods");
        mods.setReadable(true);
        mods.setWritable(true);
        CreateDirectory(mods);

        ScanMods(mods);
        LOADED = true;
        return true;
    }

    private synchronized static void CreateDirectory(File d) {
        if (!d.exists()) {
            if (d.mkdir()) return;
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        } else if (!d.isDirectory()) {
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        }
    }

    public synchronized static void ScanLibraries(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            file.setReadable(true);
            file.setWritable(true);
            if (file.isDirectory()) {
                System.out.println("Scanning directory:" + file.getName());
                ScanLibraries(file);
            } else {
                if (file.getName().endsWith(".jar")) {
                    System.out.println(file.getAbsolutePath());
                    if(file.getName().contains("net/minecraft/client")){
                        continue;
                    }
                    AddJarToTransformerExclusions(file, LibraryClasses, LibraryClassCache);
                }
            }
        }
    }

    public synchronized static void ScanMods(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            file.setReadable(true);
            file.setWritable(true);
            if (file.isDirectory()) {
                System.out.println("Scanning directory:" + file.getName());
                ScanMods(file);
            } else ScanModJarFile(file);
        }
    }

    public static boolean isGoodClass(String s) {
        if (s == null) return false;
        if (GoodClassCache.containsKey(s)) return GoodClassCache.get(s);
        for (String c : TransformerExclusions) {
            if (s.contains(c)) {
                GoodClassCache.put(s, true);
                return true;
            }
        }
        GoodClassCache.put(s, false);
        return false;
    }

    public static boolean isMinecraftClass(String s) {
        if (s == null) return false;
        if (MinecraftClassCache.containsKey(s)) return MinecraftClassCache.get(s);
        for (String c : MinecraftClasses) {
            if (s.equals(c)) {
                MinecraftClassCache.put(s, true);
                return true;
            }
        }
        MinecraftClassCache.put(s, false);
        return false;
    }

    public static boolean isLibraryClass(String s) {
        if (s == null) return false;
        if (LibraryClassCache.containsKey(s)) return LibraryClassCache.get(s);
        for (String c : LibraryClasses) {
            if (s.contains(c)) {
                System.out.println(s);
                System.out.println(c);
                LibraryClassCache.put(s, true);
                return true;
            }
        }
        LibraryClassCache.put(s, false);
        return false;
    }
}
