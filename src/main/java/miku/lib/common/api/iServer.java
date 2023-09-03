package miku.lib.common.api;

import net.minecraft.world.WorldType;

public interface iServer {
    void reloadWorld(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions);
}
