package miku.lib.mixins.minecraftforge;

import com.google.common.io.ByteSource;
import miku.lib.common.util.JarFucker;
import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mixin(value = ModAccessTransformer.class, remap = false)
public class MixinModAccessTransformer {
    @Shadow
    private static Map<String, String> embedded;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void addJar(JarFile jar, String atList) throws IOException {
        if (JarFucker.isBadJar(jar)) return;
        for (String at : atList.split(" ")) {
            JarEntry jarEntry = jar.getJarEntry("META-INF/" + at);
            if (jarEntry != null) {
                embedded.put(String.format("%s!META-INF/%s", jar.getName(), at),
                        new MixinModAccessTransformer.JarByteSource(jar, jarEntry).asCharSource(StandardCharsets.UTF_8).read());
            }
        }
    }

    @Mixin(targets = "net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer.JarByteSource")
    private static class JarByteSource extends ByteSource {
        private final JarFile jar;
        private final JarEntry entry;

        public JarByteSource(JarFile jar, JarEntry entry) {
            this.jar = jar;
            this.entry = entry;
        }

        @Override
        public InputStream openStream() throws IOException {
            return jar.getInputStream(entry);
        }
    }
}
