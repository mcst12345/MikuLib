package miku.lib.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public class ClassUtil {

    //Holy Fuck. Well,at least this is better than that package-based whitelist.
    public static final String[] mod_id_white_list = {"jei", "zollerngalaxy", "xaerominimap", "variedcommodities", "universaltweaks", "twilightforest", "tragicmc", "torcherino", "vm", "tickratechanger",
            "tickdynamic", "sweetmagic", "stevekung's_lib", "srparasites", "spaceambient", "smoothfont", "flammpfeil.slashblade", "shutupmodelloader", "scp", "redstoneflux", "randompatches", "projecteintegration",
            "projecte", "placebo", "phosphor-lighting", "performant", "patchouli", "particleculling", "openterraingenerator", "oldjava", "neid", "non_update", "moreplanets", "testdummy", "mikulib", "mikulib_sqlite",
            "miku", "memorycleaner", "maze", "matteroverdrive", "manaita_plus", "lovely_robot", "lostcities", "letmedespawn", "jeid", "rejoymod", "ic2", "ilib", "hammercore", "getittogetherdrops", "galaxyspace",
            "galacticraftcore", "fpsreducer", "foamfix", "fastbench", "fastfurnace", "expequiv", "entityculling", "ageofminecraft", "ageofabyssalcraft", "ageofchaos", "ageofmutants", "enderio", "enderiobase",
            "enderioconduits", "enderiopowertools", "enderioconduitsappliedenergistics", "enderioconduitsopencomputers", "enderioconduitsrefinedstorage", "enderiointegrationforestry", "enderiointegrationtic",
            "enderiointegrationticlate", "enderioinvpanel", "enderiomachines", "endercore", "draconicevolution", "customnpcsfix", "customnpcs", "ctm", "codechickenlib", "clumps", "clearwater", "loliasm", "brandonscore",
            "betterbiomeblend", "bedbreakbegone", "avaritia", "asmodeuscore", "appliedenergistics2", "aiimprovements", "abyssalcraft", "magic_maid", "iceandfire", "llibrary", "carianstyle", "avp", "mdxlib", "endermail",
            "thebetweenlands", "forestry", "skillful", "skillsvanilla", "musicplayer", "projectex", "extendedexchange", "tamedmonster", "hormone", "brownmooshrooms", "pubgmc", "configuration", "vanillafix", "surge", "gregtech",
            "mwc", "inputmethodblocker", "mts", "mtsofficalpack", "overlast", "farlanders", "srmonstress", "scapeandrunvaccine", "wyrmsofnyrus", "roughmobsrevamped", "sereneseasons", "immersive-intelligence", "immersiveintelligence",
            "immersiveengineering", "techguns", "synlib", "geckolib3", "thermalfoundation", "cofhworld", "thermaldynamics", "thermalinnovation", "thermalcultivation", "thermalexpansion", "evilcraft", "bloodmagic", "moarsigns", "botania",
            "opencomputers", "immersivepetroleum", "immersiveposts", "warpdrive", "tconstruct", "securitycraft"};
    public static final List<String> TransformerExclusions = new ArrayList<>();
    public static final List<String> MinecraftClasses = new ArrayList<>();
    public static final List<String> LibraryClasses = new ArrayList<>();
    public static Map<String, Class<?>> cachedClasses = null;

    public static void AddJarToTransformerExclusions(File file, List<String> list) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.isDirectory()) {
                    if (jarEntry.getName().matches("(.*).class")) {
                        String clazz = jarEntry.getName().replace("/", ".").replace(".class", "");
                        if (clazz.equals("module-info")) continue;
                        list.add(clazz);
                    }
                }
            }
        } catch (ZipException e) {
            System.out.println("Ignore file:" + file.getName());
        }
    }

    public static void ScanModJarFile(File file) throws IOException {
        if (file.getName().matches("(.*).jar")) {
            boolean fucked = false;
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
                                    if (str.contains("Fucked: true")) fucked = true;
                                }
                            }
                        }
                    }
                }
                if (good) {
                    System.out.println("Adding mod " + jar.getName() + " to TransformerExclusions");
                    TransformerExclusions.addAll(classes);
                } else if (!fucked) {
                    JarFucker.FuckJar(jar);
                } else {
                    System.out.println(jar.getName() + " has already be fucked.");
                }
            }
        }
    }

    protected static boolean LOADED = false;

    public static boolean Init() throws IOException {
        if (LOADED) return false;
        File minecraft = new File(System.getProperty("user.dir").replace(".minecraft", "") + System.getProperty("minecraft.client.jar").substring(System.getProperty("minecraft.client.jar").indexOf(".minecraft")));
        ClassUtil.AddJarToTransformerExclusions(minecraft, ClassUtil.MinecraftClasses);

        File libraires = new File("libraries");
        CreateDirectory(libraires);

        ClassUtil.ScanLibraries(libraires);

        File mods = new File("mods");
        CreateDirectory(mods);

        ClassUtil.ScanMods(mods);
        LOADED = true;
        return true;
    }

    private static void CreateDirectory(File d) {
        if (!d.exists()) {
            if (d.mkdir()) return;
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        } else if (!d.isDirectory()) {
            System.out.println("The fuck?");
            FMLCommonHandler.instance().exitJava(0, true);
        }
    }

    public static void ScanLibraries(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                System.out.println("Scanning directory:" + file.getName());
                ScanLibraries(file);
            } else {
                if (file.getName().matches("(.*).jar")) AddJarToTransformerExclusions(file, LibraryClasses);
            }
        }
    }

    public static void ScanMods(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                System.out.println("Scanning directory:" + file.getName());
                ScanMods(file);
            } else ScanModJarFile(file);
        }
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
            if (s.equals(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLibraryClass(String s) {
        for (String c : LibraryClasses) {
            if (s.equals(c)) {
                return true;
            }
        }
        return false;
    }
}
