package miku.lib.common.util;

import com.sun.jna.Platform;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class ClassUtil {
    protected static final List<File> BadMods = new ArrayList<>();
    protected static final Map<String, Boolean> GoodClassCache = new ConcurrentSkipListMap<>();
    protected static final Map<String, Boolean> MinecraftClassCache = new ConcurrentSkipListMap<>();
    protected static final Map<String, Boolean> LibraryClassCache = new ConcurrentSkipListMap<>();

    //Holy Fuck. Well, at least this is better than that package-based whitelist.
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
            "vampiresneedumbrellas", "consecration", "xreliquary", "bewitchment", "tammodized", "voidcraft", "aov", "classicbar", "toughasnails", "toroquest", "acintegration", "morph", "biomesoplenty", "nei", "mca", "tofucraft", "advancedrocketry",
            "harvestcraft", "darknesslib", "grue", "aether", "aether_legacy", "cyclopscore", "academy", "uncraftingtable", "ghostsexplosives", "kubejs", "yukarilib", "aoa3", "atum", "thelegendofthebrave", "lycanitesmobs", "forgelin",
            "snowrealmagic", "mist", "erebus", "timemachin", "herobrine", "nikkorimod", "googlyeyes", "movingworld", "shui", "davincisvessels", "mjrlegendslib", "libvulpes", "kiwi", "uteamcore", "webdisplays", "gargoyles", "lelib", "legendera"};

    protected static final String[] coremod_white_list = {"llibrary", "MixinBooter", "non_update", "OpenEyePlugin", "RandomPatches"};

    protected static final String[] coremod_class_white_list = {"com/enderio/core/common/transform/EnderCorePlugin", "thebetweenlands/core/TheBetweenlandsLoadingPlugin"};//Holy Fuck.Why don't you write a @Name() annotation?
    protected static final List<String> TransformerExclusions = new ArrayList<>();
    protected static final List<String> MinecraftClasses = new ArrayList<>();
    protected static final List<String> LibraryClasses = new ArrayList<>();
    protected static final List<String> MikuClasses = new ArrayList<>();
    protected static final Map<String, Boolean> MikuClassCache = new ConcurrentSkipListMap<>();

    public static void AddJarToTransformerExclusions(File file, List<String> list, Map<String, Boolean> map) throws IOException {
        System.out.println("Adding " + file.getPath() + " to transformer exclusions");
        List<File> deps = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory()) {
                    if (jarEntry.getName().endsWith(".class")) {
                        String clazz = jarEntry.getName().replace("/", ".").replace(".class", "").trim();
                        if (clazz.equals("module-info")) continue;
                        list.add(clazz);
                        map.put(clazz, true);
                    } else if (jarEntry.getName().endsWith(".jar")) {
                        File tmp = new File(jarEntry.getName().substring(jarEntry.getName().lastIndexOf("/")));
                        try (InputStream is = jar.getInputStream(jarEntry)) {
                            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                                while (is.available() > 0) {
                                    fos.write(is.read());
                                }
                            }
                        }
                        deps.add(tmp);
                    }
                }
            }
        } catch (ZipException e) {
            System.out.println("Ignore file:" + file.getName());
        }

        for (File jarFile : deps) {
            AddJarToTransformerExclusions(jarFile, list, map);
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

    public static boolean Loaded() {
        return LOADED;
    }

    public synchronized static void ScanModJarFile(File file) throws IOException {
        if (file.getName().endsWith(".jar")) {
            String modid = null;
            boolean fucked = false;
            try (JarFile jar = new JarFile(file)) {
                System.out.println("Reading jar file:" + jar.getName());

                List<String> classes = new ArrayList<>();
                boolean good = false;
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (!jarEntry.isDirectory()) {
                        if (jarEntry.getName().endsWith(".class")) {
                            String clazz = jarEntry.getName().replace("/", ".").replace(".class", "").trim();
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
                                        for (Object o : an.values) {
                                            String s = (String) o;
                                            if (flag) {
                                                if (s.equals("bathappymod")) {
                                                    BadMods.add(file);
                                                    return;
                                                }
                                                modid = s;
                                                break;
                                            }
                                            if (s.equals("modid")) {
                                                flag = true;
                                            }
                                        }
                                        if (modid == null) {
                                            System.out.println("The fuck? A class has @Mod annotation but without a mod id?");
                                        } else {
                                            System.out.println(modid);
                                            for (String s : mod_id_white_list) {
                                                if (s.equals(modid)) {
                                                    if (modid.equals("mikulib") || modid.equals("miku") || modid.equals("maze"))
                                                        fucked = true;
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
                        } else if (jarEntry.getName().endsWith("mcmod.info")) {
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
                                        if (good) break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (good) {
                    if (!fucked) {
                        JarFucker.RemoveSignature(jar);
                    }
                    System.out.println("Adding mod " + jar.getName() + " to TransformerExclusions");
                    if (modid != null) {
                        if (modid.equals("mikulib") || modid.equals("miku") || modid.equals("maze")) {
                            MikuClasses.addAll(classes);
                            for (String str : classes) {
                                MikuClassCache.put(str, true);
                            }
                        }
                    }
                    TransformerExclusions.addAll(classes);
                    for (String str : classes) {
                        GoodClassCache.put(str, true);
                    }
                } else if (!fucked) {
                    JarFucker.FuckModJar(jar);
                } else {
                    System.out.println(jar.getName() + " has already being fucked.");
                }
            }
        }
    }

    public static synchronized boolean Init() throws IOException {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();//调用的类名

        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();//调用的方法名

        int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();//调用的行数

        System.out.println(className);
        System.out.println(methodName);
        System.out.println(lineNumber);

        if (LOADED) return false;
        if (System.getProperty("DisableJarFucker") != null) if (System.getProperty("DisableJarFucker").equals("true")) {
            DisablejarFucker = true;
        }

        boolean windows = Platform.isWindows();

        System.out.println(System.getenv("INST_DIR"));

        String jar;

        if (Launch.Client) {
            System.out.println(System.getProperty("user.dir"));
            System.out.println(System.getProperty("minecraft.client.jar"));

            if (windows) {
                jar = System.getProperty("minecraft.client.jar").substring(System.getProperty("minecraft.client.jar").lastIndexOf("\\"));
            } else {
                jar = System.getProperty("minecraft.client.jar").substring(System.getProperty("minecraft.client.jar").lastIndexOf("/"));
            }
            System.out.println(jar);


            File minecraft = new File(System.getenv("INST_DIR") + jar);
            minecraft.setReadable(true);
            minecraft.setWritable(true);
            AddJarToTransformerExclusions(minecraft, MinecraftClasses, MinecraftClassCache);
        } else {
            List<Class<?>> MAIN = Misc.deduceMainApplicationClasses();
            for (Class<?> clazz : MAIN) {

                try {
                    CodeSource cs = clazz.getProtectionDomain().getCodeSource();
                    String file;
                    file = cs.getLocation().toURI().getSchemeSpecificPart();
                    int lastIndex = file.lastIndexOf(".jar");
                    file = file.substring(5, lastIndex + 4);
                    AddJarToTransformerExclusions(new File(file), TransformerExclusions, GoodClassCache);
                    try (JarFile JAR = new JarFile(file)) {
                        Attributes MainAttributes = JAR.getManifest().getMainAttributes();
                        for (Map.Entry<Object, Object> entry : MainAttributes.entrySet()) {
                            String key = entry.getKey().toString();
                            if (key.equalsIgnoreCase("Class-Path")) {
                                System.out.println(entry.getValue());
                            }
                        }

                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }


            }
        }


        TransformerExclusions.add("net.minecraft.server.MinecraftServer");
        GoodClassCache.put("net.minecraft.server.MinecraftServer", true);
        TransformerExclusions.add("net.minecraft.server.MinecraftServer$1");
        GoodClassCache.put("net.minecraft.server.MinecraftServer$1", true);
        TransformerExclusions.add("net.minecraft.server.MinecraftServer$3");
        GoodClassCache.put("net.minecraft.server.MinecraftServer$3", true);
        TransformerExclusions.add("net.minecraft.server.MinecraftServer$4");
        GoodClassCache.put("net.minecraft.server.MinecraftServer$4", true);
        TransformerExclusions.add("net.minecraft.realms.DisconnectedRealmsScreen");
        GoodClassCache.put("net.minecraft.realms.DisconnectedRealmsScreen", true);
        TransformerExclusions.add("net.minecraft.realms.Realms");
        GoodClassCache.put("net.minecraft.realms.Realms", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsAnvilLevelStorageSource");
        GoodClassCache.put("net.minecraft.realms.RealmsAnvilLevelStorageSource", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsBridge");
        GoodClassCache.put("net.minecraft.realms.RealmsBridge", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsBufferBuilder");
        GoodClassCache.put("net.minecraft.realms.RealmsBufferBuilder", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsButton");
        GoodClassCache.put("net.minecraft.realms.RealmsButton", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsClickableScrolledSelectionList");
        GoodClassCache.put("net.minecraft.realms.RealmsClickableScrolledSelectionList", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsConnect");
        GoodClassCache.put("net.minecraft.realms.RealmsConnect", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsDefaultVertexFormat");
        GoodClassCache.put("net.minecraft.realms.RealmsDefaultVertexFormat", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsEditBox");
        GoodClassCache.put("net.minecraft.realms.RealmsEditBox", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsLevelSummary");
        GoodClassCache.put("net.minecraft.realms.RealmsLevelSummary", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsMth");
        GoodClassCache.put("net.minecraft.realms.RealmsMth", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsScreen");
        GoodClassCache.put("net.minecraft.realms.RealmsScreen", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsScrolledSelectionList");
        GoodClassCache.put("net.minecraft.realms.RealmsScrolledSelectionList", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsServerAddress");
        GoodClassCache.put("net.minecraft.realms.RealmsServerAddress", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsSharedConstants");
        GoodClassCache.put("net.minecraft.realms.RealmsSharedConstants", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsSimpleScrolledSelectionList");
        GoodClassCache.put("net.minecraft.realms.RealmsSimpleScrolledSelectionList", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsSliderButton");
        GoodClassCache.put("net.minecraft.realms.RealmsSliderButton", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsVertexFormat");
        GoodClassCache.put("net.minecraft.realms.RealmsVertexFormat", true);
        TransformerExclusions.add("net.minecraft.realms.RealmsVertexFormatElement");
        GoodClassCache.put("net.minecraft.realms.RealmsVertexFormatElement", true);
        TransformerExclusions.add("net.minecraft.realms.Tezzelator");
        GoodClassCache.put("net.minecraft.realms.Tezzelator", true);

        File libraires = new File("libraries");
        libraires.setWritable(true);
        libraires.setWritable(true);
        CreateDirectory(libraires);

        ScanLibraries();

        File mods = new File("mods");
        mods.setReadable(true);
        mods.setWritable(true);
        CreateDirectory(mods);

        ScanMods(mods);
        LOADED = true;

        if (!BadMods.isEmpty()) {
            for (File file : BadMods) {
                System.gc();
                Files.deleteIfExists(file.toPath());
                if (Files.exists(file.toPath())) Files.delete(file.toPath());
            }
        }

        if (System.getProperty("MikuDEBUG") != null) {
            if (System.getProperty("MikuDEBUG").equals("true")) {
                for (String s : TransformerExclusions) {
                    System.out.println(s);
                }
                for (String s : MinecraftClasses) {
                    System.out.println(s);
                }
                for (String s : LibraryClasses) {
                    System.out.println(s);
                }
            }
        }

        return true;
    }

    private synchronized static void CreateDirectory(File d) {
        if (!d.exists()) {
            if (d.mkdir()) return;
            System.out.println("The fuck?");
        } else if (!d.isDirectory()) {
            System.out.println("The fuck?");
        }
    }

    public synchronized static void ScanLibraries() throws IOException {
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)){
            if(path.endsWith("1.12.2.jar"))continue;
            File file = new File(path);
            AddJarToTransformerExclusions(file,LibraryClasses,LibraryClassCache);
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
            if (s.contains(c)) {
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
                LibraryClassCache.put(s, true);
                return true;
            }
        }
        LibraryClassCache.put(s, false);
        return false;
    }

    public static boolean isMiku(String s) {
        if (s == null) return false;
        if (MikuClassCache.containsKey(s)) return MikuClassCache.get(s);
        for (String c : MikuClasses) {
            if (s.contains(c)) {
                MikuClassCache.put(s, true);
                return true;
            }
        }
        MikuClassCache.put(s, false);
        return false;
    }
}
