package miku.lib.api;

import net.minecraft.network.datasync.EntityDataManager;

public interface iEntity {
    void kill();
    boolean isTimeStop();
    void SetTimeStop();
    void TimeStop();
    EntityDataManager GetDataManager();
    boolean isDEAD();
}
