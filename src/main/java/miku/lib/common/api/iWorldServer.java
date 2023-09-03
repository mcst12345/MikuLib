package miku.lib.common.api;

import net.minecraft.world.storage.ISaveHandler;

public interface iWorldServer {
    void reload(ISaveHandler saveHandler);
}
