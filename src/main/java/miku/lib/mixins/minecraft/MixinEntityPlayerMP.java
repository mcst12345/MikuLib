package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import miku.lib.common.api.iEntityPlayer;
import miku.lib.common.api.iEntityPlayerMP;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.*;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ILootContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(value = EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements iEntityPlayerMP {


    @Shadow
    private boolean disconnected;

    @Shadow
    public NetHandlerPlayServer connection;

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public abstract void getNextWindowId();

    @Shadow
    public int currentWindowId;

    public MixinEntityPlayerMP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Override
    public boolean disconnected() {
        return disconnected;
    }

    @Override
    public void SetDisconnected(boolean value){
        disconnected = value;
    }


    @Inject(at = @At("HEAD"), method = "canAttackPlayer", cancellable = true)
    public void canAttackPlayer(EntityPlayer other, CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.isProtected(other)) cir.setReturnValue(false);
        if (EntityUtil.isProtected(this)) cir.setReturnValue(true);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onDeath(@Nonnull DamageSource cause) {
        if (EntityUtil.isProtected(this)) return;
        if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this, cause)) return;
        boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
        this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

        if (flag) {
            Team team = this.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().sendMessageToAllTeamMembers(this, this.getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().sendMessageToTeamOrAllPlayers(this, this.getCombatTracker().getDeathMessage());
                }
            } else {
                this.server.getPlayerList().sendMessage(this.getCombatTracker().getDeathMessage());
            }
        }

        this.spawnShoulderEntities();

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            captureDrops = true;
            capturedDrops.clear();
            this.destroyVanishingCursedItems();
            this.inventory.dropAllItems();

            captureDrops = false;
            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(this, cause, capturedDrops, recentlyHit > 0);
            if (!MikuLib.MikuEventBus().post(event)) {
                for (net.minecraft.entity.item.EntityItem item : capturedDrops) {
                    this.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : this.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
            Score score = this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = this.getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null) {
                this.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

            entitylivingbase.awardKillScore(this, this.scoreValue, cause);
        }

        this.addStat(StatList.DEATHS);
        this.takeStat(StatList.TIME_SINCE_DEATH);
        this.extinguish();
        this.setFlag(0, false);
        this.getCombatTracker().reset();
    }

    @Inject(at = @At("HEAD"), method = "attackEntityFrom", cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.isProtected(this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "setGameType", cancellable = true)
    public void setGameType(GameType gameType, CallbackInfo ci) {
        if (EntityUtil.isProtected(this)) {
            EntityPlayerMP player  = ((EntityPlayerMP) (Object) this);
            int i = ((iEntityPlayer)this).GetGameMode();
            if(i == 0) {
                player.interactionManager.setGameType(GameType.CREATIVE);
                player.connection.sendPacket(new SPacketChangeGameState(3, (float) GameType.CREATIVE.getID()));
                player.sendPlayerAbilities();
            } else if(i == 1) {
                player.interactionManager.setGameType(GameType.SURVIVAL);
                player.connection.sendPacket(new SPacketChangeGameState(3, (float) GameType.SURVIVAL.getID()));
                player.sendPlayerAbilities();
            } else if(i == 2) {
                player.interactionManager.setGameType(GameType.ADVENTURE);
                player.connection.sendPacket(new SPacketChangeGameState(3, (float) GameType.ADVENTURE.getID()));
                player.sendPlayerAbilities();
            } else if (i == 3) {
                player.interactionManager.setGameType(GameType.SPECTATOR);
                player.connection.sendPacket(new SPacketChangeGameState(3, (float) GameType.SPECTATOR.getID()));
                player.sendPlayerAbilities();
            }
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void displayGui(@Nonnull IInteractionObject guiOwner) {
        if (guiOwner instanceof ILootContainer && ((ILootContainer) guiOwner).getLootTable() != null && this.isSpectator()) {
            this.sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen")).setStyle((new Style()).setColor(TextFormatting.RED)), true);
        } else {
            this.getNextWindowId();
            this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, guiOwner.getGuiID(), guiOwner.getDisplayName()));
            this.openContainer = guiOwner.createContainer(this.inventory, this);
            this.openContainer.windowId = this.currentWindowId;
            this.openContainer.addListener((EntityPlayerMP) (Object) this);
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(this, this.openContainer));
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void displayGUIChest(@Nonnull IInventory chestInventory) {
        if (chestInventory instanceof ILootContainer && ((ILootContainer) chestInventory).getLootTable() != null && this.isSpectator()) {
            this.sendStatusMessage((new TextComponentTranslation("container.spectatorCantOpen")).setStyle((new Style()).setColor(TextFormatting.RED)), true);
        } else {
            if (this.openContainer != this.inventoryContainer) {
                this.closeScreen();
            }

            if (chestInventory instanceof ILockableContainer) {
                ILockableContainer ilockablecontainer = (ILockableContainer) chestInventory;

                if (ilockablecontainer.isLocked() && !this.canOpen(ilockablecontainer.getLockCode()) && !this.isSpectator()) {
                    this.connection.sendPacket(new SPacketChat(new TextComponentTranslation("container.isLocked", chestInventory.getDisplayName()), ChatType.GAME_INFO));
                    this.connection.sendPacket(new SPacketSoundEffect(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, this.posX, this.posY, this.posZ, 1.0F, 1.0F));
                    return;
                }
            }

            this.getNextWindowId();

            if (chestInventory instanceof IInteractionObject) {
                this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, ((IInteractionObject) chestInventory).getGuiID(), chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
                this.openContainer = ((IInteractionObject) chestInventory).createContainer(this.inventory, this);
            } else {
                this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:container", chestInventory.getDisplayName(), chestInventory.getSizeInventory()));
                this.openContainer = new ContainerChest(this.inventory, chestInventory, this);
            }

            this.openContainer.windowId = this.currentWindowId;
            this.openContainer.addListener((EntityPlayerMP) (Object) this);
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(this, this.openContainer));
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void displayVillagerTradeGui(@Nonnull IMerchant villager) {
        this.getNextWindowId();
        this.openContainer = new ContainerMerchant(this.inventory, villager, this.world);
        this.openContainer.windowId = this.currentWindowId;
        this.openContainer.addListener((EntityPlayerMP) (Object) this);
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(this, this.openContainer));
        IInventory iinventory = ((ContainerMerchant) this.openContainer).getMerchantInventory();
        ITextComponent itextcomponent = villager.getDisplayName();
        this.connection.sendPacket(new SPacketOpenWindow(this.currentWindowId, "minecraft:villager", itextcomponent, iinventory.getSizeInventory()));
        MerchantRecipeList merchantrecipelist = villager.getRecipes(this);

        if (merchantrecipelist != null) {
            PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
            packetbuffer.writeInt(this.currentWindowId);
            merchantrecipelist.writeToBuf(packetbuffer);
            this.connection.sendPacket(new SPacketCustomPayload("MC|TrList", packetbuffer));
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void closeContainer() {
        this.openContainer.onContainerClosed(this);
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Close(this, this.openContainer));
        this.openContainer = this.inventoryContainer;
    }
}
