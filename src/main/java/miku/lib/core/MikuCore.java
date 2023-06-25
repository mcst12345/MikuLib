package miku.lib.core;

import miku.lib.config.MikuConfig;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("MikuLib")
public class MikuCore implements IFMLLoadingPlugin {
    public static boolean RescueMode=false;

    public MikuCore(){
        MikuConfig.init();
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.mikulib.json");
        Mixins.addConfiguration("mixins.chaos.json");
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
