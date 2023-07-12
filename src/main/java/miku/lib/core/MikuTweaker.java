package miku.lib.core;

import miku.lib.sqlite.Sqlite;
import miku.lib.util.HashUtil;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MikuTweaker implements ITweaker {
    private void InitLib(){
        File sql = new File("sqlite-jdbc-3.42.0.0.jar");
        boolean flag = false;
        if(sql.exists()){
            String sha256;
            try {
                sha256 = HashUtil.getHash(sql,"SHA-256");
                System.out.println(sha256);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if(!(sha256.equals("53174d7687bb73cc29db9c02766fb921fd7fc652f7952f3609e018e3dd5ded"))){
                System.out.println("Film damaged,re-downloading.");
                if(!sql.delete()){
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
    private String[] args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        InitLib();
        try {
            Class<URLClassLoader> clazz = (Class<URLClassLoader>) java.lang.ClassLoader.getSystemClassLoader().getClass();
            Method method = clazz.getMethod("addURL",URL.class);
            method.invoke(ClassLoader.getSystemClassLoader(),new File("sqlite-jdbc-3.42.0.0.jar").toURI().toURL());
            //Launch.classLoader.addURL(new File("sqlite-jdbc-3.42.0.0.jar").toURI().toURL());
        } catch (MalformedURLException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Sqlite.CoreInit();
        String[] additionArgs = {"--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDir.getAbsolutePath(), "--version", profile};
        List<String> fullArgs = new ArrayList<>();
        fullArgs.addAll(args);
        fullArgs.addAll(Arrays.asList(additionArgs));
        this.args = fullArgs.toArray(new String[fullArgs.size()]);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        System.out.println("Add MikuTransformer");
        classLoader.registerTransformer("miku.lib.core.MikuTransformer");
    }

    @Override
    public String getLaunchTarget() {
        return "miku.lib.util.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
