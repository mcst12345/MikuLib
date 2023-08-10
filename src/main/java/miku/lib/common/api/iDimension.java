package miku.lib.common.api;

import net.minecraft.world.DimensionType;

public interface iDimension {
    DimensionType type();

    int ticksWaited();

    void ticksWaitedAdd();

    void setTicksWaited(int i);
}
