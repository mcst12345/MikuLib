package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityEnderman.class)
public abstract class MixinEntityEnderman extends EntityMob {
    public MixinEntityEnderman(World worldIn) {
        super(worldIn);
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!
     */
    @Overwrite
    public boolean teleportTo(double x, double y, double z) {
        if (TimeStopUtil.isTimeStop()) return false;
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(this, x, y, z, 0);
        if (MikuLib.MikuEventBus.post(event)) return false;
        boolean flag = this.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (flag) {
            this.world.playSound(null, this.prevPosX, this.prevPosY, this.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        }

        return flag;
    }

    @Inject(at = @At("HEAD"), method = "isScreaming", cancellable = true)
    public void isScreaming(CallbackInfoReturnable<Boolean> cir) {
        if (TimeStopUtil.isTimeStop()) cir.setReturnValue(false);
    }
}
