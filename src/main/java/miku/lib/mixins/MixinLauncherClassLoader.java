package miku.lib.mixins;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Mixin(value = LaunchClassLoader.class,remap = false)
public abstract class MixinLauncherClassLoader extends URLClassLoader {
    @Shadow private Set<String> invalidClasses;

    @Shadow private Set<String> classLoaderExceptions;

    @Shadow private ClassLoader parent;

    @Shadow private Map<String, Class<?>> cachedClasses;

    @Shadow private Set<String> transformerExceptions;

    @Shadow protected abstract String transformName(String name);

    @Shadow protected abstract String untransformName(String name);

    @Shadow protected abstract URLConnection findCodeSourceConnectionFor(String name);

    @Shadow public abstract byte[] getClassBytes(String name) throws IOException;

    @Shadow private Map<Package, Manifest> packageManifests;

    @Shadow protected abstract boolean isSealed(String path, Manifest manifest);

    @Shadow @Final private static Manifest EMPTY;

    @Shadow protected abstract byte[] runTransformers(String name, String transformedName, byte[] basicClass);

    @Shadow @Final private static boolean DEBUG_SAVE;

    @Shadow protected abstract void saveTransformedClass(byte[] data, String transformedName);

    @Shadow @Final private static boolean DEBUG;

    public MixinLauncherClassLoader(URL[] urls) {
        super(urls);
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {

        for (final String exception : classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return parent.loadClass(name);
            }
        }

        if (cachedClasses.containsKey(name)) {
            return cachedClasses.get(name);
        }

        for (final String exception : transformerExceptions) {
            if (name.startsWith(exception)) {
                try {
                    final Class<?> clazz = super.findClass(name);
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    invalidClasses.add(name);
                    throw e;
                }
            }
        }

        try {
            final String transformedName = transformName(name);
            if (cachedClasses.containsKey(transformedName)) {
                return cachedClasses.get(transformedName);
            }

            final String untransformedName = untransformName(name);

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            final String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = findCodeSourceConnectionFor(fileName);

            CodeSigner[] signers = null;

            if (lastDot > -1 && !untransformedName.startsWith("net.minecraft.")) {
                if (urlConnection instanceof JarURLConnection) {
                    final JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                    final JarFile jarFile = jarURLConnection.getJarFile();

                    if (jarFile != null && jarFile.getManifest() != null) {
                        final Manifest manifest = jarFile.getManifest();
                        final JarEntry entry = jarFile.getJarEntry(fileName);

                        Package pkg = getPackage(packageName);
                        getClassBytes(untransformedName);
                        signers = entry.getCodeSigners();
                        if (pkg == null) {
                            pkg = definePackage(packageName, manifest, jarURLConnection.getJarFileURL());
                            packageManifests.put(pkg, manifest);
                        } else {
                            if (pkg.isSealed() && !pkg.isSealed(jarURLConnection.getJarFileURL())) {
                                LogWrapper.severe("The jar file %s is trying to seal already secured path %s", jarFile.getName(), packageName);
                            } else if (isSealed(packageName, manifest)) {
                                LogWrapper.severe("The jar file %s has a security seal for path %s, but that path is defined and not secure", jarFile.getName(), packageName);
                            }
                        }
                    }
                } else {
                    Package pkg = getPackage(packageName);
                    if (pkg == null) {
                        pkg = definePackage(packageName, null, null, null, null, null, null, null);
                        packageManifests.put(pkg, EMPTY);
                    } else if (pkg.isSealed()) {
                        LogWrapper.severe("The URL %s is defining elements for sealed path %s", urlConnection.getURL(), packageName);
                    }
                }
            }

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, getClassBytes(untransformedName));
            if (DEBUG_SAVE) {
                saveTransformedClass(transformedClass, transformedName);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            invalidClasses.add(name);
            if (DEBUG) {
                LogWrapper.log(Level.TRACE, e, "Exception encountered attempting classloading of %s", name);
                LogManager.getLogger("LaunchWrapper").log(Level.ERROR, "Exception encountered attempting classloading of %s", e);
            }
            throw new ClassNotFoundException(name, e);
        }
    }
}
