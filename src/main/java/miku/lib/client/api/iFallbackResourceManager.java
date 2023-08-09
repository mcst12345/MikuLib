package miku.lib.client.api;

import net.minecraft.client.resources.IResourcePack;

import java.util.List;

public interface iFallbackResourceManager {
    List<IResourcePack> getResourcePacks();
}
