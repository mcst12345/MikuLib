package miku.lib.client.api;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.profiler.Profiler;

public interface iMinecraft {
    void SetTimeStop();

    void SetProtected();

    boolean protect();

    boolean isTimeStop();

    void Stop();

    void SET_INGAME_FOCUS();

    void SET_INGAME_NOT_FOCUS();

    Profiler MikuProfiler();

    EntityRenderer MikuEntityRenderer();
}
