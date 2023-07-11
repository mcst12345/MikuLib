package miku.lib.mixins.minecraft;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class ClientMixinEntityLivingBase extends Entity {
    public ClientMixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @SideOnly(Side.CLIENT)
    @Inject(at=@At("HEAD"),method = "handleStatusUpdate", cancellable = true)
    public void handleStatusUpdate(byte id, CallbackInfo ci){
        if(EntityUtil.isProtected(this)){
            if(id == 3 || id == 2)ci.cancel();
        }
    }
}
