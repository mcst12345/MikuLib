package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.util.TimeStopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Timer.class)
public class MixinTimer {
    @Shadow
    private long lastSyncSysClock;

    @Shadow
    public float renderPartialTicks;

    @Shadow
    public float elapsedPartialTicks;

    @Shadow
    public int elapsedTicks;

    @Shadow
    private float tickLength;

    /**
     * @author mcst12345
     * @reason Stop!
     */
    @Overwrite
    public void updateTimer() {
        if (TimeStopUtil.isTimeStop() || ((iMinecraft) Minecraft.getMinecraft()).isTimeStop()) return;
        long i = Minecraft.getSystemTime();
        this.elapsedPartialTicks = (i - this.lastSyncSysClock) / this.tickLength;
        this.lastSyncSysClock = i;
        this.renderPartialTicks += this.elapsedPartialTicks;
        this.elapsedTicks = (int) this.renderPartialTicks;
        this.renderPartialTicks -= this.elapsedTicks;
    }
}
