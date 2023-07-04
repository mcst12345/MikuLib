package miku.lib.core;

import miku.lib.config.MikuConfig;
import miku.lib.sqlite.Sqlite;
import miku.lib.util.HashUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("MikuLib")
public class MikuCore implements IFMLLoadingPlugin {
    private void InitLib(){
        File sql = new File("sqlite-jdbc-3.42.0.0.jar");
        boolean flag = false;
        if(sql.exists()){
            String sha256;
            try {
                sha256 = HashUtil.getHash(sql,"SHA-256");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if(!sha256.equals("53174d76087bb73cc29db9c02766fb921fd7fc652f7952f3609e0018e3dd5ded")){
                if(!sql.delete()){
                    throw new RuntimeException("Failed to delete the invalid file:"+sql.getName());
                }
                flag = true;
                System.out.println(sha256);
            }
        } else flag = true;
        if(flag){
            try(FileOutputStream fs = new FileOutputStream("sqlite-jdbc-3.42.0.0.jar")) {
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
    public static boolean RescueMode=false;

    public MikuCore(){
        MikuConfig.init();
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.chaos.json");
        InitLib();
        try {
            Loader.instance().getModClassLoader().addFile(new File("sqlite-jdbc-3.42.0.0.jar"));
        } catch (MalformedURLException ignored) {
        }
        Sqlite.Init();
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
        try {
            ClassLoader appClassLoader = Launch.class.getClassLoader();
            MethodUtils.invokeMethod(appClassLoader, true, "addURL", this.getClass().getProtectionDomain().getCodeSource().getLocation());
            MethodUtils.invokeStaticMethod(appClassLoader.loadClass(this.getClass().getName()), "initMixin");
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
