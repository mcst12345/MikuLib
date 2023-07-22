package miku.lib.core;

import miku.lib.util.ClassUtil;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.net.URL;

public class MikuClassLoader extends LaunchClassLoader {
    public MikuClassLoader(URL[] sources) {
        super(sources);
        addClassLoaderExclusion("miku.");
    }

    protected static boolean isGoodTransformer(Object var1) {
        String s = var1.getClass().toString().substring(5).trim();
        return s.equals("guichaguri.betterfps.tweaker.BetterFpsTweaker") || s.equals("org.spongepowered.asm.launch.MixinTweaker") ||
                s.equals("miku.lib.core.MikuTweaker") || s.equals("optifine.OptiFineForgeTweaker") || s.equals("net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker") ||
                s.equals("net.minecraftforge.fml.common.launcher.FMLTweaer") || s.equals("net.minecraftforge.fml.common.launcher.FMLDeobfTweaker") ||
                s.equals("net.minecraft.launchwrapper.AlphaVanillaTweaker") || s.equals("org.spongepowered.asm.mixin.EnvironmentStateTweaker") ||
                s.equals("net.minecraftforge.fml.relauncher.CoreModManager.FMLPluginWrapper") || s.equals("net.minecraftforge.fml.common.launcher.FMLServerTweaker") ||
                s.equals("net.minecraft.launchwrapper.IndevVanillaTweaker") || s.equals("net.minecraftforge.fml.common.launcher.TerminalTweaker") ||
                s.equals("net.minecraft.launchwrapper.VanillaTweaker") || s.equals("net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer") ||
                s.equals("net.minecraftforge.fml.common.asm.transformers.AccessTransformer") || s.equals("net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer") ||
                s.equals("net.minecraftforge.fml.common.asm.transformers.ItemStackTransformer") || s.equals("net.minecraftforge.fml.common.asm.transformers.ItemBlockTransformer") ||
                s.equals("net.minecraftforge.fml.common.asm.transformers.ItemBlockSpecialTransformer") || s.equals("net.minecraftforge.fml.common.asm.transformers.PotionEffectTransformer") ||
                s.equals("org.spongepowered.asm.mixin.transformer.Proxy") || s.equals("net.minecraftforge.fml.common.asm.transformers.PatchingTransformer") || s.equals("optifine.OptiFineClassTransformer") ||
                s.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.SideTransformer") || s.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer") ||
                s.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.EventSubscriberTransformer") || s.equals("$wrapper.net.minecraftforge.fml.common.asm.transformers.SoundEngineFixTransformer") ||
                s.equals("miku.lib.core.MikuTransformer") || s.equals("net.minecraftforge.fml.common.asm.transformers.TerminalTransformer") || s.equals("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer");
    }

    @Override
    public void registerTransformer(String transformerClassName) {
        if (!ClassUtil.isGoodClass(transformerClassName) && !isGoodTransformer(transformerClassName)) return;
        super.registerTransformer(transformerClassName);
    }
}
