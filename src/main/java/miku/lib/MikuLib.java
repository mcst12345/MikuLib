package miku.lib;

import miku.lib.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;


@Mod(
        modid = MikuLib.MODID,
        name = MikuLib.NAME,
        version = MikuLib.VERSION
)
public class MikuLib {
    public static final String MODID = "mikulib";
    public static final String NAME = "MikuLib";
    public static final String VERSION = "1.2";

    public MikuLib() {
    }

    @SidedProxy(
            clientSide = "miku.lib.proxy.ClientProxy",
            serverSide = "miku.lib.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance
    public static MikuLib INSTANCE;

    protected Logger log;


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
