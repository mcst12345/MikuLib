package miku.lib.mixins.minecraftforge;

import miku.lib.common.core.MikuTransformer;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.IFMLSidedHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = FMLServerHandler.class,remap = false)
public abstract class MixinFMLServerHandler implements IFMLSidedHandler {
    @Shadow private MinecraftServer server;

    @Shadow private List<String> injectedModContainers;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Override
    public void beginServerLoading(MinecraftServer minecraftServer) {
        Launch.NoReflection(MinecraftServer.class);
        Launch.NoReflection(FMLServerHandler.class);
        server = minecraftServer;
        Loader.instance().loadMods(injectedModContainers);
        Sqlite.Init();
        for (FieldNode field : MikuTransformer.BadFields) {
            //To Do ?
        }
        Loader.instance().preinitializeMods();
    }
}
