package miku.lib.common.api;

import com.chaoswither.entity.EntityChaosWither;

import java.util.Set;

public interface iChaosUpdateEvent {
    
    Set<EntityChaosWither> GET();

    void KILL();
}
