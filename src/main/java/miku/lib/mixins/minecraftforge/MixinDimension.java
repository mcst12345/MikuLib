package miku.lib.mixins.minecraftforge;

import miku.lib.common.api.iDimension;
import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraftforge.common.DimensionManager.Dimension")
public class MixinDimension implements iDimension {
    @Final
    @Shadow
    private DimensionType type;

    public void setTicksWaited(int ticksWaited) {
        this.ticksWaited = ticksWaited;
    }

    @Shadow
    private int ticksWaited;

    @Override
    public DimensionType type() {
        return type;
    }

    @Override
    public int ticksWaited() {
        return ticksWaited;
    }

    @Override
    public void ticksWaitedAdd() {
        ticksWaited++;
    }
}
