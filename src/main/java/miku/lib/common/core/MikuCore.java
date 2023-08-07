package miku.lib.common.core;

import com.sun.jna.Platform;
import miku.lib.common.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class MikuCore implements IFMLLoadingPlugin {
    public static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    protected static boolean restart = false;
    protected static final List<String> InvalidMods = new ArrayList<>();

    static final boolean win = Platform.isWindows();
    static final boolean Linux = Platform.isLinux();
    static final boolean MacOS = Platform.isMac();
    static final boolean BSD = Platform.isFreeBSD() || Platform.isNetBSD() || Platform.isOpenBSD() || Platform.iskFreeBSD();
    static final boolean Android = Platform.isAndroid();

    public MikuCore() {

        FuckLaunchWrapper();
        //FuckForge();

        if (win) {
            System.out.println("Holy fuck,MikuLib is running on Windows! This is not recommended! Use GNU/Linux instead if possible.");
        }
        if (Linux) {
            System.out.println("MikuLib is running on Linux. Weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee!");
        }
        if (MacOS) {
            System.out.println("MikuLib has never been tested on MacOS. Good luck.");
        }
        if (BSD) {
            System.out.println("MikuLib has never been tested on BSD. Good luck.");
        }
        if (Android) {
            System.out.println("The FUCK? You are running MikuLib on Android?");
        }

        if (restart) {
            try {
                StringBuilder LAUNCH = new StringBuilder();
                for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (s.contains("=")) {
                        if (!win) LAUNCH.append('"');
                        LAUNCH.append(s);
                        if (!win) LAUNCH.append('"');
                    } else LAUNCH.append(s);
                    LAUNCH.append(' ');
                }

                LAUNCH.append("-cp ");
                for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
                    if (!win) LAUNCH.append(path).append(":");
                    else LAUNCH.append(path).append(";");
                }
                LAUNCH = new StringBuilder(LAUNCH.substring(0, LAUNCH.length() - 1));

                String USERNAME = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
                String UUID = RandomStringUtils.randomAlphanumeric(32).toLowerCase();

                LAUNCH.append(" net.minecraft.launchwrapper.Launch --tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker --username ").append(USERNAME);
                LAUNCH.append(" --version 1.12.2");
                LAUNCH.append(" --gameDir ").append(System.getProperty("user.dir"));
                LAUNCH.append(" --assetsDir ").append(System.getProperty("user.dir")).append("/assets");
                LAUNCH.append(" --assetIndex 1.12");
                LAUNCH.append(" --uuid ").append(UUID);
                LAUNCH.append("  --accessToken HatsuneMiku");
                LAUNCH.append(" --userType msa --versionType Forge --width 854 --height 480");
                String JAVA = System.getProperty("java.home");
                System.out.println("java.home:" + JAVA);
                if (JAVA.endsWith("jre")) {
                    String JavaHome = JAVA.substring(0, JAVA.length() - 3);
                    File jdk = new File(JavaHome + "bin/java");
                    if (jdk.exists()) {
                        String tmp = JavaHome + "bin/java ";
                        if (win) {
                            tmp = tmp.trim();
                            tmp = tmp.replace("\\", "\\\\").replace("/", "\\\\");
                            tmp = "\"" + tmp;
                            tmp = tmp + ".exe\"";
                        }
                        LAUNCH.insert(0, tmp + " ");
                    } else {
                        String tmp = JAVA + "/bin/java ";
                        if (win) {
                            tmp = tmp.trim();
                            tmp = tmp.replace("\\", "\\\\").replace("/", "\\\\");
                            tmp = "\"" + tmp;
                            tmp = tmp + ".exe\"";
                        }
                        LAUNCH.insert(0, tmp + " ");
                    }
                }
                String command = LAUNCH.toString().replace(",", "");
                System.out.println("MikuLib has completed its file injection.Now restarting the game.");
                System.out.println("Command:\n" + command);
                if (win) {
                    ProcessBuilder process = new ProcessBuilder("cmd /c " + command);
                    process.redirectErrorStream(true);
                    Process mc = process.start();
                    BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(mc.getInputStream()));
                    String line;
                    while ((line = inStreamReader.readLine()) != null) {
                        System.out.println(line);
                    }

                } else {
                    Process mc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command}, null, null);
                    InputStream is = mc.getInputStream();
                    String line;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    mc.waitFor();
                    is.close();
                    reader.close();
                }

                while (true) {
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        Launch.Transformers.setAccessible(true);
        long tmp = Launch.UNSAFE.objectFieldOffset(Launch.Transformers);
        List<IClassTransformer> t = (List<IClassTransformer>) Launch.UNSAFE.getObject(Launch.classLoader, tmp);
        if (!(t instanceof MikuArrayListForTransformer)) {
            MikuArrayListForTransformer<IClassTransformer> fucked = new MikuArrayListForTransformer<>(2);
            for (IClassTransformer i : t) fucked.add(i);
            System.out.println("Fucking LaunchClassLoader.");
            Launch.UNSAFE.putObjectVolatile(Launch.classLoader, tmp, fucked);//Fuck other transformers.
        }

        Launch.NoReflection(MikuLib.class);
        Launch.NoReflection(MikuCore.class);
    }

    public static void InitMixin(){
        try {
            //Add Mixin configs.
            Mixins.addConfiguration("mixins.minecraft.json");
            Mixins.addConfiguration("mixins.forge.json");
        } catch (Throwable e) {
            if (isLaunchFucked()) {
                System.out.println("The fuck? MikuLib can't apply mixins.");
            }
        }
    }
    protected synchronized static boolean isLaunchFucked() {
        try {
            Field miku = Launch.class.getDeclaredField("Miku");
            miku.setAccessible(true);
            Class<?> version = Class.forName("net.minecraft.launchwrapper.Miku4");
            return true;
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            return false;
        }
    }

    public synchronized static void FuckLaunchWrapper() {
        if (isLaunchFucked()) return;
        try {
            FileUtils.copyFile(new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"), new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar.backup"));
            InputStream MikuLaunch;
            if (win) {
                MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked.win");
            } else MikuLaunch = MikuCore.class.getResourceAsStream("/launchwrapper-1.12.jar.fucked");
            InputStream MIXIN = MikuCore.class.getResourceAsStream("/mixin-0.8.5-SNAPSHOT.jar");
            assert MikuLaunch != null;
            FileUtils.copyInputStreamToFile(MikuLaunch, new File(System.getProperty("user.dir") + "/libraries/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar"));
            assert MIXIN != null;
            FileUtils.copyInputStreamToFile(MIXIN, new File(System.getProperty("user.dir") + "/libraries/mixin.jar"));
        } catch (IOException ignored) {
        }
        restart = true;
    }

    public synchronized static void FuckForge() {
        System.out.println("Fucking Forge.");
        if (ForgeFucked()) return;
        String forge = null;
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (path.contains("net/minecraftforge/forge")) forge = path;
        }
        if (forge == null) {
            throw new RuntimeException("The fuck?I can't find your forge file!");
        }
        try {
            JarFile jar = new JarFile(forge);
            JarOutputStream jos = new JarOutputStream(Files.newOutputStream(Paths.get(jar.getName() + ".fucked")));
            for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();
                try (InputStream is = jar.getInputStream(entry)) {
                    System.out.println(entry.getName());
                    if (entry.getName().equals("net/minecraftforge/fml/relauncher/CoreModManager$FMLPluginWrapper.class")) {
                        //TODO
                    }
                    if (entry.getName().equals("net/minecraftforge/fml/relauncher/CoreModManager.class")) {
                        //TODO
                    }
                }
            }
            jos.closeEntry();
            jos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //restart = true;
    }

    public synchronized static boolean ForgeFucked() {
        try {
            Field field = CoreModManager.class.getDeclaredField("Miku1");
            field.setAccessible(true);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static void AddInvalidMod(String mod) {
        if (mod.equals("mikulib") || mod.equals("miku")) return;
        InvalidMods.add(mod);
    }

    public static boolean isModInvalid(String mod) {
        if (mod.equals("mikulib") || mod.equals("miku")) return false;
        return InvalidMods.contains(mod);
    }
}
