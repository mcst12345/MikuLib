package miku.lib.mixins.minecraft;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import miku.lib.common.api.iPlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(value = PlayerChunkMap.class)
public class MixinPlayerChunkMap implements iPlayerChunkMap {
    @Shadow
    @Final
    private Long2ObjectMap<PlayerChunkMapEntry> entryMap;

    @Shadow
    @Final
    private List<PlayerChunkMapEntry> entries;

    @Shadow
    @Final
    private Set<PlayerChunkMapEntry> dirtyEntries;

    @Shadow
    @Final
    private List<PlayerChunkMapEntry> pendingSendToPlayers;

    @Shadow
    @Final
    private List<PlayerChunkMapEntry> entriesWithoutChunks;

    @Shadow
    private boolean sortMissingChunks;

    @Shadow
    private boolean sortSendToPlayers;

    @Override
    public void reload() {
        this.entryMap.clear();
        this.entries.clear();
        this.dirtyEntries.clear();
        this.pendingSendToPlayers.clear();
        this.entriesWithoutChunks.clear();
        this.sortMissingChunks = true;
        this.sortSendToPlayers = true;
    }
}
