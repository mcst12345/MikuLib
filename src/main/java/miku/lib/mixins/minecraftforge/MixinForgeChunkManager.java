package miku.lib.mixins.minecraftforge;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import miku.lib.common.core.MikuLib;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = ForgeChunkManager.class)
public abstract class MixinForgeChunkManager {
    @Shadow
    private static SetMultimap<String, ForgeChunkManager.Ticket> playerTickets;

    @Shadow
    private static Map<World, Multimap<String, ForgeChunkManager.Ticket>> tickets;

    @Shadow
    private static Map<World, ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket>> forcedChunks;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void forceChunk(ForgeChunkManager.Ticket ticket, ChunkPos chunk) {
        if (ticket == null || chunk == null) {
            return;
        }
        if (ticket.ticketType == ForgeChunkManager.Type.ENTITY && ticket.entity == null) {
            throw new RuntimeException("Attempted to use an entity ticket to force a chunk, without an entity");
        }
        if (ticket.isPlayerTicket() ? !playerTickets.containsValue(ticket) : !tickets.get(ticket.world).containsEntry(ticket.modId, ticket)) {
            FMLLog.log.fatal("The mod {} attempted to force load a chunk with an invalid ticket. This is not permitted.", ticket.modId);
            return;
        }
        ticket.requestedChunks.add(chunk);
        MikuLib.MikuEventBus().post(new ForgeChunkManager.ForceChunkEvent(ticket, chunk));

        ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> newMap = ImmutableSetMultimap.<ChunkPos, ForgeChunkManager.Ticket>builder().putAll(forcedChunks.get(ticket.world)).put(chunk, ticket).build();
        forcedChunks.put(ticket.world, newMap);
        if (ticket.maxDepth > 0 && ticket.requestedChunks.size() > ticket.maxDepth) {
            ChunkPos removed = ticket.requestedChunks.iterator().next();
            unforceChunk(ticket, removed);
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static void unforceChunk(ForgeChunkManager.Ticket ticket, ChunkPos chunk) {
        if (ticket == null || chunk == null) {
            return;
        }
        ticket.requestedChunks.remove(chunk);
        MikuLib.MikuEventBus().post(new ForgeChunkManager.UnforceChunkEvent(ticket, chunk));
        LinkedHashMultimap<ChunkPos, ForgeChunkManager.Ticket> copy = LinkedHashMultimap.create(forcedChunks.get(ticket.world));
        copy.remove(chunk, ticket);
        ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> newMap = ImmutableSetMultimap.copyOf(copy);
        forcedChunks.put(ticket.world, newMap);
    }
}
