package miku.lib.mixins.minecraft;

import io.netty.buffer.Unpooled;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.api.iNetHandlerPlayClient;
import miku.lib.client.thread.ClientTNTThreads;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.UUID;

@Mixin(value = NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements iNetHandlerPlayClient {
    @Override
    public void setWorld(WorldClient world) {
        this.world = world;
    }

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

    @Shadow
    public abstract NetworkPlayerInfo getPlayerInfo(UUID uniqueId);

    @Shadow
    @Final
    private static Logger LOGGER;

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
            this.world = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, ((iMinecraft) this.client).MikuWorld().getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), ((iMinecraft) this.client).MikuProfiler());
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
        MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(), ((iMinecraft) this.client).MikuWorld());

        if (mapdata == null) {
            String s = "map_" + packetIn.getMapId();
            mapdata = new MapData(s);

            if (mapitemrenderer.getMapInstanceIfExists(s) != null) {
                MapData mapdata1 = mapitemrenderer.getData(mapitemrenderer.getMapInstanceIfExists(s));

                if (mapdata1 != null) {
                    mapdata = mapdata1;
                }
            }

            ((iMinecraft) this.client).MikuWorld().setData(s, mapdata);
        }

        packetIn.setMapdataTo(mapdata);
        mapitemrenderer.updateMapTexture(mapdata);
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!!
     */
    @Overwrite
    public void handleTimeUpdate(SPacketTimeUpdate packetIn) {
        if (packetIn == null) return;
        try {
            PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        } catch (Throwable ignored) {
        }
        try {
            ((iMinecraft) this.client).MikuWorld().setTotalWorldTime(packetIn.getTotalWorldTime());
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at setTotalWorldTime");
            t.printStackTrace();
        }
        try {
            ((iMinecraft) this.client).MikuWorld().setWorldTime(packetIn.getWorldTime());
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at setWorldTime");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void handleSpawnPlayer(SPacketSpawnPlayer packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();
        float f = (float) (packetIn.getYaw() * 360) / 256.0F;
        float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
        EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(((iMinecraft) this.client).MikuWorld(), this.getPlayerInfo(packetIn.getUniqueId()).getGameProfile());
        entityotherplayermp.prevPosX = d0;
        entityotherplayermp.lastTickPosX = d0;
        entityotherplayermp.prevPosY = d1;
        entityotherplayermp.lastTickPosY = d1;
        entityotherplayermp.prevPosZ = d2;
        entityotherplayermp.lastTickPosZ = d2;
        EntityTracker.updateServerPosition(entityotherplayermp, d0, d1, d2);
        entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
        this.world.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
        List<EntityDataManager.DataEntry<?>> list = packetIn.getDataManagerEntries();

        if (list != null) {
            entityotherplayermp.getDataManager().setEntryValues(list);
        }
    }

    /**
     * @author mcst12345
     * @reason FuckShit
     */
    @Overwrite
    public void handleSpawnMob(SPacketSpawnMob packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();
        float f = (float) (packetIn.getYaw() * 360) / 256.0F;
        float f1 = (float) (packetIn.getPitch() * 360) / 256.0F;
        EntityLivingBase entitylivingbase = (EntityLivingBase) EntityList.createEntityByID(packetIn.getEntityType(), ((iMinecraft) this.client).MikuWorld());

        if (entitylivingbase != null) {
            if (Sqlite.DEBUG()) {
                System.out.println("MikuInfo:Spawn mob:" + entitylivingbase.getClass());
            }
            EntityTracker.updateServerPosition(entitylivingbase, d0, d1, d2);
            entitylivingbase.renderYawOffset = (float) (packetIn.getHeadPitch() * 360) / 256.0F;
            entitylivingbase.rotationYawHead = (float) (packetIn.getHeadPitch() * 360) / 256.0F;
            Entity[] aentity = entitylivingbase.getParts();

            if (aentity != null) {
                int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

                for (Entity entity : aentity) {
                    entity.setEntityId(entity.getEntityId() + i);
                }
            }

            entitylivingbase.setEntityId(packetIn.getEntityID());
            entitylivingbase.setUniqueId(packetIn.getUniqueId());
            entitylivingbase.setPositionAndRotation(d0, d1, d2, f, f1);
            entitylivingbase.motionX = (float) packetIn.getVelocityX() / 8000.0F;
            entitylivingbase.motionY = (float) packetIn.getVelocityY() / 8000.0F;
            entitylivingbase.motionZ = (float) packetIn.getVelocityZ() / 8000.0F;
            this.world.addEntityToWorld(packetIn.getEntityID(), entitylivingbase);
            List<EntityDataManager.DataEntry<?>> list = packetIn.getDataManagerEntries();

            if (list != null) {
                entitylivingbase.getDataManager().setEntryValues(list);
            }
        } else {
            LOGGER.warn("Skipping Entity with id {}", packetIn.getEntityType());
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void handleSpawnPosition(SPacketSpawnPosition packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        this.client.player.setSpawnPoint(packetIn.getSpawnPos(), true);
        ((iMinecraft) this.client).MikuWorld().getWorldInfo().setSpawn(packetIn.getSpawnPos());
    }

    /**
     * @author mcst12345
     * @reason f
     */
    @Overwrite
    public void handleExplosion(SPacketExplosion packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        Explosion explosion = new Explosion(((iMinecraft) this.client).MikuWorld(), null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        ClientTNTThreads.AddExplosion(explosion);
        this.client.player.motionX += packetIn.getMotionX();
        this.client.player.motionY += packetIn.getMotionY();
        this.client.player.motionZ += packetIn.getMotionZ();
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void handleUpdateTileEntity(SPacketUpdateTileEntity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);

        if (((iMinecraft) this.client).MikuWorld().isBlockLoaded(packetIn.getPos())) {
            TileEntity tileentity = ((iMinecraft) this.client).MikuWorld().getTileEntity(packetIn.getPos());
            int i = packetIn.getTileEntityType();
            boolean flag = i == 2 && tileentity instanceof TileEntityCommandBlock;

            if (i == 1 && tileentity instanceof TileEntityMobSpawner || flag || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner || i == 7 && tileentity instanceof TileEntityStructure || i == 8 && tileentity instanceof TileEntityEndGateway || i == 9 && tileentity instanceof TileEntitySign || i == 10 && tileentity instanceof TileEntityShulkerBox || i == 11 && tileentity instanceof TileEntityBed) {
                tileentity.readFromNBT(packetIn.getNbtCompound());
            } else {
                if (tileentity == null) {
                    LOGGER.error("Received invalid update packet for null tile entity at {} with data: {}", packetIn.getPos(), packetIn.getNbtCompound());
                    return;
                }
                tileentity.onDataPacket(netManager, packetIn);
            }

            if (flag && this.client.currentScreen instanceof GuiCommandBlock) {
                ((GuiCommandBlock) this.client.currentScreen).updateGui();
            }
        }
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void handleBlockAction(SPacketBlockAction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        ((iMinecraft) this.client).MikuWorld().addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void handleBlockBreakAnim(SPacketBlockBreakAnim packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        ((iMinecraft) this.client).MikuWorld().sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void handleEffect(SPacketEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);

        if (packetIn.isSoundServerwide()) {
            ((iMinecraft) this.client).MikuWorld().playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        } else {
            ((iMinecraft) this.client).MikuWorld().playEvent(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void handleServerDifficulty(SPacketServerDifficulty packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        ((iMinecraft) this.client).MikuWorld().getWorldInfo().setDifficulty(packetIn.getDifficulty());
        ((iMinecraft) this.client).MikuWorld().getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
    }

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void handleSoundEffect(SPacketSoundEffect packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.client);
        ((iMinecraft) this.client).MikuWorld().playSound(this.client.player, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSound(), packetIn.getCategory(), packetIn.getVolume(), packetIn.getPitch());
    }
}
