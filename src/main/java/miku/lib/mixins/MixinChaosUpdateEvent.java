package miku.lib.mixins;

import com.chaoswither.entity.EntityChaosWither;
import com.chaoswither.event.ChaosUpdateEvent;
import miku.lib.api.iChaosUpdateEvent;
import miku.lib.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = ChaosUpdateEvent.class)
public class MixinChaosUpdateEvent implements iChaosUpdateEvent {
    @Shadow private Set<EntityChaosWither> cwither;

    private static ChaosUpdateEvent event = null;


    @Override
    public Set<EntityChaosWither> GET() {
        return cwither;
    }

    @Override
    public void KILL() {
        ((iChaosUpdateEvent)event).GET().clear();
    }

    @Inject(at=@At("TAIL"),method = "<init>")
    public void init(CallbackInfo ci){
        event = (ChaosUpdateEvent) (Object) this;
        EntityUtil.chaos_event= (ChaosUpdateEvent) (Object) this;
    }

}
