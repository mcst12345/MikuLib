package miku.lib.client.api;

import net.minecraft.client.resources.FallbackResourceManager;

import java.util.Map;

public interface iSimpleReloadableResourceManager {
    Map<String, FallbackResourceManager> getDomainResourceManagers();
}
