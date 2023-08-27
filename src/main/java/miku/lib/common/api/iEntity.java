package miku.lib.common.api;

import net.minecraft.network.datasync.EntityDataManager;

public interface iEntity {
    void kill();
    boolean isTimeStop();

    EntityDataManager GetDataManager();

    boolean isDEAD();

    void SetTimeStop(boolean stop);
}
