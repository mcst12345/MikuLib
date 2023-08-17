package miku.lib.common.api;

import net.minecraft.util.ResourceLocation;

public interface iRegistryDelegate<T> {
    void SetName(ResourceLocation rs);

    void ChangeReference(T newTarget);
}
