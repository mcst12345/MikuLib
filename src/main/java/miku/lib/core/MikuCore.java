package miku.lib.core;

import miku.lib.sqlite.Sqlite;
import miku.lib.util.ClassUtil;
import miku.lib.util.HashUtil;
import miku.lib.util.JarFucker;
import miku.lib.util.MikuArrayListForTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class MikuCore implements IFMLLoadingPlugin {
    public MikuCore() throws IOException, NoSuchFieldException, IllegalAccessException {
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
        ClassUtil.cachedClasses = (Map<String, Class<?>>) cachedClasses.get(Launch.classLoader);

        ClassUtil.Init();

        InitLib();
        try {
            Launch.classLoader.addURL((new File("sqlite-jdbc-3.42.0.0.jar")).toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Sqlite.CoreInit();
        System.out.println("Add MikuTransformer");

        //Add our transformer
        Launch.classLoader.registerTransformer("miku.lib.core.MikuTransformer");

        System.out.println("Init mixins");

        MixinBootstrap.init();
        //Add Mixin configs.
        Mixins.addConfiguration("mixins.minecraft.json");
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.forge.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        if (JarFucker.shouldRestart()) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            Runtime.getRuntime().exit(-39);
        }
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        if (JarFucker.shouldRestart()) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        if (JarFucker.shouldRestart()) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if (JarFucker.shouldRestart()) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            Runtime.getRuntime().exit(-39);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        if (JarFucker.shouldRestart()) {
            System.out.println("MikuLib has completed its file injection.Please restart the game.");
            Runtime.getRuntime().exit(-39);
        }
        return null;
    }

    private void InitLib() {
        File sql = new File("sqlite-jdbc-3.42.0.0.jar");
        boolean flag = false;
        if (sql.exists()) {
            String sha256;
            try {
                sha256 = HashUtil.getHash(sql, "SHA-256");
                System.out.println(sha256);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!(sha256.equals("53174d7687bb73cc29db9c02766fb921fd7fc652f7952f3609e018e3dd5ded"))) {
                System.out.println("Film damaged,re-downloading.");
                if (!sql.delete()) {
                    throw new RuntimeException("Failed to delete damaged file:sqlite-jdbc-3.42.0.0.jar");
                }
                flag = true;

            }
        } else {
            flag = true;
            System.out.println("Downloading file:sqlite-jdbc-3.42.0.0.jar.");
            System.out.println("If you are in China and can't download this file because of GFW,download it from this url:");
            System.out.println("https://ghproxy.com/github.com/xerial/sqlite-jdbc/releases/download/3.42.0.0/sqlite-jdbc-3.42.0.0.jar");
            System.out.println("and put it into your .minecraft dir.");
        }
        if (flag) {
            try (FileOutputStream fs = new FileOutputStream("sqlite-jdbc-3.42.0.0.jar")) {
                URL url = new URL("https://github.com/xerial/sqlite-jdbc/releases/download/3.42.0.0/sqlite-jdbc-3.42.0.0.jar");
                URLConnection conn = url.openConnection();
                InputStream inStream = conn.getInputStream();

                byte[] buffer = new byte[40000000];
                int byteread;
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
            } catch (IOException ignored) {

            }
        }
    }
}
