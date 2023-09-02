package miku.lib.mixins.minecraft;

import miku.lib.common.api.iMapStorage;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(value = MapStorage.class)
public abstract class MixinMapStorage implements iMapStorage {
    @Shadow
    protected Map<String, WorldSavedData> loadedDataMap;

    @Shadow
    @Final
    private List<WorldSavedData> loadedDataList;

    @Shadow
    @Final
    private Map<String, Short> idCounts;

    @Override
    public void clearData() {
        loadedDataMap.clear();
        loadedDataList.clear();
        idCounts.clear();
    }

}
