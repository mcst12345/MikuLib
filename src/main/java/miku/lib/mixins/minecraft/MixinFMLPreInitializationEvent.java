package miku.lib.mixins.minecraft;

import miku.lib.core.MikuAccessTransformer;
import miku.lib.sqlite.Sqlite;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FMLPreInitializationEvent.class)
public class MixinFMLPreInitializationEvent {

    @Inject(at=@At("TAIL"),method = "<init>")
    public void Init(Object[] data, CallbackInfo ci){
        Sqlite.Init();
        for(FieldNode field : MikuAccessTransformer.BadFields){

        }
    }
}
