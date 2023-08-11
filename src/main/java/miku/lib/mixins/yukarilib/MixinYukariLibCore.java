package miku.lib.mixins.yukarilib;

import c6h2cl2.YukariLib.YukariLibCore;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = YukariLibCore.class)
public class MixinYukariLibCore {
    /**
     * @author mcst12345
     * @reason Let me play offline
     */
    @Overwrite
    private final void check(FMLPreInitializationEvent event) {
    }
}
