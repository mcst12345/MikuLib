package miku.lib.mixins.minecraftforge;

import miku.lib.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FMLCommonHandler.class,remap = false)
public class MixinForgeCommandHandler {
    @Inject(at=@At("HEAD"),method = "exitJava", cancellable = true)
    public void exitJava(int exitCode, boolean hardExit, CallbackInfo ci){
        if(EntityUtil.isProtected(Minecraft.getMinecraft().player))ci.cancel();
    }
}
