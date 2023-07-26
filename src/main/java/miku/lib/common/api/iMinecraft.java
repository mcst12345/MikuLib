package miku.lib.common.api;

public interface iMinecraft {
    void SetTimeStop();
    void SetProtected();
    boolean protect();
    boolean isTimeStop();
    void Stop();
    void SET_INGAME_FOCUS();
    void SET_INGAME_NOT_FOCUS();
}
