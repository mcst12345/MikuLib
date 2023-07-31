package miku.lib.common.core;

import miku.lib.common.core.proxy.CommonProxy;
import miku.lib.common.util.ClassUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


@Mod(
        modid = MikuLib.MODID,
        name = MikuLib.NAME,
        version = MikuLib.VERSION
)
public class MikuLib {
    public static final String MODID = "mikulib";
    public static final String NAME = "MikuLib";
    public static final String VERSION = "1.10";
    @SidedProxy(
            clientSide = "miku.lib.common.core.proxy.ClientProxy",
            serverSide = "miku.lib.common.core.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public MikuLib() throws IOException {
        if (ClassUtil.Init()) {
            MikuCore.FuckLaunchWrapper();
            System.out.println("Well,if you see this message,that probably means someone fucked my coremod.\n" +
                    "Now I have fucked theirs.Restart the game.");
            Runtime.getRuntime().exit(-39);
        }

        Launch.FuckNative();
    }

    @Mod.Instance
    public static MikuLib INSTANCE;

    protected Logger log;//I never use this.I prefer System.out.println.That's better for stacktracing.


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        this.log = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
