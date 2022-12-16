package miku.lib;

import miku.lib.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;

import java.util.logging.Logger;

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
}
