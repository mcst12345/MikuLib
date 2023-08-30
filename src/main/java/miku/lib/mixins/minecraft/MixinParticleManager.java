package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ParticleManager.class)
public class MixinParticleManager {
    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    private void tickParticle(final Particle particle) {
        if (TimeStopUtil.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop()) return;
        try {
            particle.onUpdate();
        } catch (Throwable throwable) {
            System.out.println("MikuWarn:Failed to ticking particle:" + particle.toString());
            throwable.printStackTrace();
        }
    }
}
