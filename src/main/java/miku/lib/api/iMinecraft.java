package miku.lib.api;

import net.minecraft.client.renderer.block.model.ModelManager;

public interface iMinecraft {
    ModelManager GetModelManager();

    void SetTimeStop();

    void SetProtected();

    boolean protect();
}
