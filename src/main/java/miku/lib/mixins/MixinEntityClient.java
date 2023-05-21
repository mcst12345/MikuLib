package miku.lib.mixins;

import miku.lib.api.iEntity;
import miku.lib.item.SpecialItem;
import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class)
public class MixinEntityClient {
    @Inject(at=@At("HEAD"),method = "getBrightnessForRender", cancellable = true)
    @SideOnly(Side.CLIENT)
    public void getBrightnessForRender(CallbackInfoReturnable<Integer> cir){
        if(SpecialItem.isTimeStop() || ((iEntity)this).isTimeStop())cir.setReturnValue(EntityUtil.isProtected(this) ? 15728880 : 0);
    }
}
