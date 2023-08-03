package miku.lib.mixins.minecraft;

import io.netty.buffer.Unpooled;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {
    @Shadow
    private Minecraft client;

    @Shadow
    private WorldClient world;

    @Shadow
    public int currentServerMaxPlayers;

    @Shadow
    @Final
    private NetworkManager netManager;

    @Shadow
    public abstract NetworkManager getNetworkManager();

    @Shadow
    private boolean doneLoadingTerrain;

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void handleJoinGame(SPacketJoinGame packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        this.client.playerController = new PlayerControllerMP(this.client, (NetHandlerPlayClient) (Object) this);
        this.world = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), net.minecraftforge.fml.common.network.handshake.NetworkDispatcher.get(getNetworkManager()).getOverrideDimension(packetIn), packetIn.getDifficulty(), ((iMinecraft) this.client).MikuProfiler());
        this.client.gameSettings.difficulty = packetIn.getDifficulty();
        this.client.loadWorld(this.world);
        this.client.player.dimension = packetIn.getDimension();
        this.client.displayGuiScreen(new GuiDownloadTerrain());
        this.client.player.setEntityId(packetIn.getPlayerId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.client.player.setReducedDebug(packetIn.isReducedDebugInfo());
        this.client.playerController.setGameType(packetIn.getGameType());
        this.client.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new CPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void handleRespawn(SPacketRespawn packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);

        if (packetIn.getDimensionID() != this.client.player.dimension) {
            this.doneLoadingTerrain = false;
            Scoreboard scoreboard = this.world.getScoreboard();
            this.world = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, this.client.world.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), ((iMinecraft) this.client).MikuProfiler());
            this.world.setWorldScoreboard(scoreboard);
            this.client.loadWorld(this.world);
            this.client.player.dimension = packetIn.getDimensionID();
            this.client.displayGuiScreen(new GuiDownloadTerrain());
        }

        this.client.setDimensionAndSpawnPlayer(packetIn.getDimensionID());
        this.client.playerController.setGameType(packetIn.getGameType());
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void handleEntityStatus(SPacketEntityStatus packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        Entity entity = packetIn.getEntity(this.world);

        if (entity != null) {
            if (packetIn.getOpCode() == 21) {
                this.client.getSoundHandler().playSound(new GuardianSound((EntityGuardian) entity));
            } else if (packetIn.getOpCode() == 35) {
                this.client.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.TOTEM, 30);
                this.world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);

                if (entity == this.client.player) {
                    ((iMinecraft) this.client).MikuEntityRenderer().displayItemActivation(new ItemStack(Items.TOTEM_OF_UNDYING));
                }
            } else {
                entity.handleStatusUpdate(packetIn.getOpCode());
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void handleMaps(SPacketMaps packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        MapItemRenderer mapitemrenderer = ((iMinecraft) this.client).MikuEntityRenderer().getMapItemRenderer();
        MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(), this.client.world);

        if (mapdata == null) {
            String s = "map_" + packetIn.getMapId();
            mapdata = new MapData(s);

            if (mapitemrenderer.getMapInstanceIfExists(s) != null) {
                MapData mapdata1 = mapitemrenderer.getData(mapitemrenderer.getMapInstanceIfExists(s));

                if (mapdata1 != null) {
                    mapdata = mapdata1;
                }
            }

            this.client.world.setData(s, mapdata);
        }

        packetIn.setMapdataTo(mapdata);
        mapitemrenderer.updateMapTexture(mapdata);
    }
}
