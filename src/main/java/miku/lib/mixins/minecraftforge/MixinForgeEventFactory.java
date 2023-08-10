package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.GameRuleChangeEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.brewing.PlayerBrewedPotionEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.village.MerchantTradeOffersEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

@Mixin(value = ForgeEventFactory.class, remap = false)
public class MixinForgeEventFactory {

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static BlockEvent.EntityMultiPlaceEvent onMultiBlockPlace(@Nullable Entity entity, List<BlockSnapshot> blockSnapshots, EnumFacing direction) {
        BlockSnapshot snap = blockSnapshots.get(0);
        IBlockState placedAgainst = snap.getWorld().getBlockState(snap.getPos().offset(direction.getOpposite()));
        BlockEvent.EntityMultiPlaceEvent event = new BlockEvent.EntityMultiPlaceEvent(blockSnapshots, placedAgainst, entity);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static BlockEvent.MultiPlaceEvent onPlayerMultiBlockPlace(EntityPlayer player, List<BlockSnapshot> blockSnapshots, EnumFacing direction, EnumHand hand) {
        BlockSnapshot snap = blockSnapshots.get(0);
        IBlockState placedAgainst = snap.getWorld().getBlockState(snap.getPos().offset(direction.getOpposite()));
        BlockEvent.MultiPlaceEvent event = new BlockEvent.MultiPlaceEvent(blockSnapshots, placedAgainst, player, hand);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static BlockEvent.EntityPlaceEvent onBlockPlace(@Nullable Entity entity, @Nonnull BlockSnapshot blockSnapshot, @Nonnull EnumFacing direction) {
        IBlockState placedAgainst = blockSnapshot.getWorld().getBlockState(blockSnapshot.getPos().offset(direction.getOpposite()));
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot, placedAgainst, entity);
        MikuLib.MikuEventBus().post(event);
        return event;
    }


    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static BlockEvent.PlaceEvent onPlayerBlockPlace(@Nonnull EntityPlayer player, @Nonnull BlockSnapshot blockSnapshot, @Nonnull EnumFacing direction, @Nonnull EnumHand hand) {
        IBlockState placedAgainst = blockSnapshot.getWorld().getBlockState(blockSnapshot.getPos().offset(direction.getOpposite()));
        BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(blockSnapshot, placedAgainst, player, hand);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static BlockEvent.NeighborNotifyEvent onNeighborNotify(World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {
        BlockEvent.NeighborNotifyEvent event = new BlockEvent.NeighborNotifyEvent(world, pos, state, notifiedSides, forceRedstoneUpdate);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean doPlayerHarvestCheck(EntityPlayer player, IBlockState state, boolean success) {
        PlayerEvent.HarvestCheck event = new PlayerEvent.HarvestCheck(player, state, success);
        MikuLib.MikuEventBus().post(event);
        return EntityUtil.isProtected(player) || event.canHarvest();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static float getBreakSpeed(EntityPlayer player, IBlockState state, float original, BlockPos pos) {
        PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(player, state, original, pos);
        return (MikuLib.MikuEventBus().post(event) ? -1 : event.getNewSpeed());
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerDestroyItem(EntityPlayer player, @Nonnull ItemStack stack, @Nullable EnumHand hand) {
        MikuLib.MikuEventBus().post(new PlayerDestroyItemEvent(player, stack, hand));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Deprecated
    @Overwrite
    public static Event.Result canEntitySpawn(EntityLiving entity, World world, float x, float y, float z, boolean isSpawner) {
        if (entity == null)
            return Event.Result.DEFAULT;
        LivingSpawnEvent.CheckSpawn event = new LivingSpawnEvent.CheckSpawn(entity, world, x, y, z, isSpawner); // TODO: replace isSpawner with null in 1.13
        MikuLib.MikuEventBus().post(event);
        return event.getResult();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static Event.Result canEntitySpawn(EntityLiving entity, World world, float x, float y, float z, MobSpawnerBaseLogic spawner) {
        if (entity == null)
            return Event.Result.DEFAULT;
        LivingSpawnEvent.CheckSpawn event = new LivingSpawnEvent.CheckSpawn(entity, world, x, y, z, spawner);
        MikuLib.MikuEventBus().post(event);
        return event.getResult();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Deprecated
    @Overwrite
    public static boolean doSpecialSpawn(EntityLiving entity, World world, float x, float y, float z) {
        return MikuLib.MikuEventBus().post(new LivingSpawnEvent.SpecialSpawn(entity, world, x, y, z, null));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean doSpecialSpawn(EntityLiving entity, World world, float x, float y, float z, MobSpawnerBaseLogic spawner) {
        return MikuLib.MikuEventBus().post(new LivingSpawnEvent.SpecialSpawn(entity, world, x, y, z, spawner));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static Event.Result canEntityDespawn(EntityLiving entity) {
        LivingSpawnEvent.AllowDespawn event = new LivingSpawnEvent.AllowDespawn(entity);
        MikuLib.MikuEventBus().post(event);
        return event.getResult();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int getItemBurnTime(@Nonnull ItemStack itemStack) {
        Item item = itemStack.getItem();
        int burnTime = item.getItemBurnTime(itemStack);
        FurnaceFuelBurnTimeEvent event = new FurnaceFuelBurnTimeEvent(itemStack, burnTime);
        MikuLib.MikuEventBus().post(event);
        if (event.getBurnTime() < 0) {
            // legacy handling
            int fuelValue = GameRegistry.getFuelValueLegacy(itemStack);
            if (fuelValue > 0) {
                return fuelValue;
            }
        }
        return event.getBurnTime();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int getExperienceDrop(EntityLivingBase entity, EntityPlayer attackingPlayer, int originalExperience) {
        LivingExperienceDropEvent event = new LivingExperienceDropEvent(entity, attackingPlayer, originalExperience);
        if (MikuLib.MikuEventBus().post(event)) {
            return 0;
        }
        return event.getDroppedExperience();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Nullable
    public static List<Biome.SpawnListEntry> getPotentialSpawns(WorldServer world, EnumCreatureType type, BlockPos pos, List<Biome.SpawnListEntry> oldList) {
        WorldEvent.PotentialSpawns event = new WorldEvent.PotentialSpawns(world, type, pos, oldList);
        if (MikuLib.MikuEventBus().post(event)) {
            return null;
        }
        return event.getList();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int getMaxSpawnPackSize(EntityLiving entity) {
        LivingPackSizeEvent maxCanSpawnEvent = new LivingPackSizeEvent(entity);
        MikuLib.MikuEventBus().post(maxCanSpawnEvent);
        return maxCanSpawnEvent.getResult() == Event.Result.ALLOW ? maxCanSpawnEvent.getMaxPackSize() : entity.getMaxSpawnedInChunk();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static String getPlayerDisplayName(EntityPlayer player, String username) {
        PlayerEvent.NameFormat event = new PlayerEvent.NameFormat(player, username);
        MikuLib.MikuEventBus().post(event);
        return event.getDisplayname();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static float fireBlockHarvesting(List<ItemStack> drops, World world, BlockPos pos, IBlockState state, int fortune, float dropChance, boolean silkTouch, EntityPlayer player) {
        BlockEvent.HarvestDropsEvent event = new BlockEvent.HarvestDropsEvent(world, pos, state, fortune, dropChance, drops, player, silkTouch);
        MikuLib.MikuEventBus().post(event);
        return event.getDropChance();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static IBlockState fireFluidPlaceBlockEvent(World world, BlockPos pos, BlockPos liquidPos, IBlockState state) {
        BlockEvent.FluidPlaceBlockEvent event = new BlockEvent.FluidPlaceBlockEvent(world, pos, liquidPos, state);
        MikuLib.MikuEventBus().post(event);
        return event.getNewState();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static ItemTooltipEvent onItemTooltip(ItemStack itemStack, @Nullable EntityPlayer entityPlayer, List<String> toolTip, ITooltipFlag flags) {
        ItemTooltipEvent event = new ItemTooltipEvent(itemStack, entityPlayer, toolTip, flags);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static ZombieEvent.SummonAidEvent fireZombieSummonAid(EntityZombie zombie, World world, int x, int y, int z, EntityLivingBase attacker, double summonChance) {
        ZombieEvent.SummonAidEvent summonEvent = new ZombieEvent.SummonAidEvent(zombie, world, x, y, z, attacker, summonChance);
        MikuLib.MikuEventBus().post(summonEvent);
        return summonEvent;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onEntityStruckByLightning(Entity entity, EntityLightningBolt bolt) {
        return MikuLib.MikuEventBus().post(new EntityStruckByLightningEvent(entity, bolt));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onStartEntityTracking(Entity entity, EntityPlayer player) {
        MikuLib.MikuEventBus().post(new PlayerEvent.StartTracking(player, entity));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onStopEntityTracking(Entity entity, EntityPlayer player) {
        MikuLib.MikuEventBus().post(new PlayerEvent.StopTracking(player, entity));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void firePlayerLoadingEvent(EntityPlayer player, File playerDirectory, String uuidString) {
        MikuLib.MikuEventBus().post(new PlayerEvent.LoadFromFile(player, playerDirectory, uuidString));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void firePlayerSavingEvent(EntityPlayer player, File playerDirectory, String uuidString) {
        MikuLib.MikuEventBus().post(new PlayerEvent.SaveToFile(player, playerDirectory, uuidString));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void firePlayerLoadingEvent(EntityPlayer player, IPlayerFileData playerFileData, String uuidString) {
        SaveHandler sh = (SaveHandler) playerFileData;
        File dir = ObfuscationReflectionHelper.getPrivateValue(SaveHandler.class, sh, "field_" + "75771_c");
        MikuLib.MikuEventBus().post(new PlayerEvent.LoadFromFile(player, dir, uuidString));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Nullable
    public static ITextComponent onClientChat(ChatType type, ITextComponent message) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(type, message);
        return MikuLib.MikuEventBus().post(event) ? null : event.getMessage();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Nonnull
    public static String onClientSendMessage(String message) {
        ClientChatEvent event = new ClientChatEvent(message);
        return MikuLib.MikuEventBus().post(event) ? "" : event.getMessage();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onHoeUse(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos) {
        UseHoeEvent event = new UseHoeEvent(player, stack, worldIn, pos);
        if (MikuLib.MikuEventBus().post(event)) return -1;
        if (event.getResult() == Event.Result.ALLOW) {
            stack.damageItem(1, player);
            return 1;
        }
        return 0;
    }


    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onApplyBonemeal(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull ItemStack stack, @Nullable EnumHand hand) {
        BonemealEvent event = new BonemealEvent(player, world, pos, state, hand, stack);
        if (MikuLib.MikuEventBus().post(event)) return -1;
        if (event.getResult() == Event.Result.ALLOW) {
            if (!world.isRemote)
                stack.shrink(1);
            return 1;
        }
        return 0;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Nullable
    public static ActionResult<ItemStack> onBucketUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull ItemStack stack, @Nullable RayTraceResult target) {
        FillBucketEvent event = new FillBucketEvent(player, stack, world, target);
        if (MikuLib.MikuEventBus().post(event)) return new ActionResult<>(EnumActionResult.FAIL, stack);

        if (event.getResult() == Event.Result.ALLOW) {
            if (player.capabilities.isCreativeMode)
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);

            stack.shrink(1);
            if (stack.isEmpty())
                return new ActionResult<>(EnumActionResult.SUCCESS, event.getFilledBucket());

            if (!player.inventory.addItemStackToInventory(event.getFilledBucket()))
                player.dropItem(event.getFilledBucket(), false);

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return null;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean canEntityUpdate(Entity entity) {
        EntityEvent.CanUpdate event = new EntityEvent.CanUpdate(entity);
        MikuLib.MikuEventBus().post(event);
        return event.getCanUpdate();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static PlaySoundAtEntityEvent onPlaySoundAtEntity(Entity entity, SoundEvent name, SoundCategory category, float volume, float pitch) {
        PlaySoundAtEntityEvent event = new PlaySoundAtEntityEvent(entity, name, category, volume, pitch);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onItemExpire(EntityItem entity, @Nonnull ItemStack item) {
        if (item.isEmpty()) return -1;
        ItemExpireEvent event = new ItemExpireEvent(entity, (item.isEmpty() ? 6000 : item.getItem().getEntityLifespan(item, entity.world)));
        if (!MikuLib.MikuEventBus().post(event)) return -1;
        return event.getExtraLife();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onItemPickup(EntityItem entityItem, EntityPlayer player) {
        Event event = new EntityItemPickupEvent(player, entityItem);
        if (MikuLib.MikuEventBus().post(event)) return -1;
        return event.getResult() == Event.Result.ALLOW ? 1 : 0;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerDrops(EntityPlayer player, DamageSource cause, List<EntityItem> capturedDrops, boolean recentlyHit) {
        PlayerDropsEvent event = new PlayerDropsEvent(player, cause, capturedDrops, recentlyHit);
        if (!MikuLib.MikuEventBus().post(event)) {
            for (EntityItem item : capturedDrops) {
                player.dropItemAndGetStack(item);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean canMountEntity(Entity entityMounting, Entity entityBeingMounted, boolean isMounting) {
        boolean isCanceled = MikuLib.MikuEventBus().post(new EntityMountEvent(entityMounting, entityBeingMounted, entityMounting.world, isMounting));

        if (isCanceled) {
            entityMounting.setPositionAndRotation(entityMounting.posX, entityMounting.posY, entityMounting.posZ, entityMounting.prevRotationYaw, entityMounting.prevRotationPitch);
            return false;
        } else
            return true;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onAnimalTame(EntityAnimal animal, EntityPlayer tamer) {
        return MikuLib.MikuEventBus().post(new AnimalTameEvent(animal, tamer));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static EntityPlayer.SleepResult onPlayerSleepInBed(EntityPlayer player, BlockPos pos) {
        PlayerSleepInBedEvent event = new PlayerSleepInBedEvent(player, pos);
        MikuLib.MikuEventBus().post(event);
        return event.getResultStatus();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerWakeup(EntityPlayer player, boolean wakeImmediately, boolean updateWorldFlag, boolean setSpawn) {
        MikuLib.MikuEventBus().post(new PlayerWakeUpEvent(player, wakeImmediately, updateWorldFlag, setSpawn));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerFall(EntityPlayer player, float distance, float multiplier) {
        MikuLib.MikuEventBus().post(new PlayerFlyableFallEvent(player, distance, multiplier));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onPlayerSpawnSet(EntityPlayer player, BlockPos pos, boolean forced) {
        return MikuLib.MikuEventBus().post(new PlayerSetSpawnEvent(player, pos, forced));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerClone(EntityPlayer player, EntityPlayer oldPlayer, boolean wasDeath) {
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(player, oldPlayer, wasDeath));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onExplosionStart(World world, Explosion explosion) {
        return MikuLib.MikuEventBus().post(new ExplosionEvent.Start(world, explosion));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onExplosionDetonate(World world, Explosion explosion, List<Entity> list, double diameter) {
        MikuLib.MikuEventBus().post(new ExplosionEvent.Detonate(world, explosion, list));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onCreateWorldSpawn(World world, WorldSettings settings) {
        return MikuLib.MikuEventBus().post(new WorldEvent.CreateSpawnPosition(world, settings));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static float onLivingHeal(EntityLivingBase entity, float amount) {
        LivingHealEvent event = new LivingHealEvent(entity, amount);
        return (MikuLib.MikuEventBus().post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onPotionAttemptBrew(NonNullList<ItemStack> stacks) {
        NonNullList<ItemStack> tmp = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        for (int x = 0; x < tmp.size(); x++)
            tmp.set(x, stacks.get(x).copy());

        PotionBrewEvent.Pre event = new PotionBrewEvent.Pre(tmp);
        if (MikuLib.MikuEventBus().post(event)) {
            boolean changed = false;
            for (int x = 0; x < stacks.size(); x++) {
                changed |= ItemStack.areItemStacksEqual(tmp.get(x), stacks.get(x));
                stacks.set(x, event.getItem(x));
            }
            if (changed)
                onPotionBrewed(stacks);
            return true;
        }
        return false;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPotionBrewed(NonNullList<ItemStack> brewingItemStacks) {
        MikuLib.MikuEventBus().post(new PotionBrewEvent.Post(brewingItemStacks));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onPlayerBrewedPotion(EntityPlayer player, ItemStack stack) {
        MikuLib.MikuEventBus().post(new PlayerBrewedPotionEvent(player, stack));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean renderBlockOverlay(EntityPlayer player, float renderPartialTicks, RenderBlockOverlayEvent.OverlayType type, IBlockState block, BlockPos pos) {
        return MikuLib.MikuEventBus().post(new RenderBlockOverlayEvent(player, renderPartialTicks, type, block, pos));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean gatherCollisionBoxes(World world, Entity entity, AxisAlignedBB aabb, List<AxisAlignedBB> outList) {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        try {
            MikuLib.MikuEventBus().post(new GetCollisionBoxesEvent(world, entity, aabb, outList));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at GetCollisionBoxesEvent");
            t.printStackTrace();
        }
        return outList.isEmpty();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onItemUseStart(EntityLivingBase entity, ItemStack item, int duration) {
        if (MikuInsaneMode.isMikuInsaneMode() && !EntityUtil.isProtected(entity)) return 0;
        LivingEntityUseItemEvent event = new LivingEntityUseItemEvent.Start(entity, item, duration);
        return MikuLib.MikuEventBus().post(event) ? -1 : event.getDuration();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onItemUseTick(EntityLivingBase entity, ItemStack item, int duration) {
        if (MikuInsaneMode.isMikuInsaneMode() && !EntityUtil.isProtected(entity)) return 0;
        LivingEntityUseItemEvent event = new LivingEntityUseItemEvent.Tick(entity, item, duration);
        return MikuLib.MikuEventBus().post(event) ? -1 : event.getDuration();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onUseItemStop(EntityLivingBase entity, ItemStack item, int duration) {
        if (MikuInsaneMode.isMikuInsaneMode() && !EntityUtil.isProtected(entity)) return true;
        return MikuLib.MikuEventBus().post(new LivingEntityUseItemEvent.Stop(entity, item, duration));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static ItemStack onItemUseFinish(EntityLivingBase entity, ItemStack item, int duration, ItemStack result) {
        if (MikuInsaneMode.isMikuInsaneMode() && !EntityUtil.isProtected(entity)) return ItemStack.EMPTY;
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, item, duration, result);
        MikuLib.MikuEventBus().post(event);
        return event.getResultStack();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    @Nullable
    public static CapabilityDispatcher gatherCapabilities(AttachCapabilitiesEvent<?> event, @Nullable ICapabilityProvider parent) {
        MikuLib.MikuEventBus().post(event);
        return event.getCapabilities().size() > 0 || parent != null ? new CapabilityDispatcher(event.getCapabilities(), parent) : null;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean fireSleepingLocationCheck(EntityPlayer player, BlockPos sleepingLocation) {
        SleepingLocationCheckEvent evt = new SleepingLocationCheckEvent(player, sleepingLocation);
        MikuLib.MikuEventBus().post(evt);

        Event.Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Event.Result.DEFAULT) {
            IBlockState state = player.world.getBlockState(player.bedLocation);
            return state.getBlock().isBed(state, player.world, player.bedLocation, player);
        } else
            return canContinueSleep == Event.Result.ALLOW;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean fireSleepingTimeCheck(EntityPlayer player, BlockPos sleepingLocation) {
        SleepingTimeCheckEvent evt = new SleepingTimeCheckEvent(player, sleepingLocation);
        MikuLib.MikuEventBus().post(evt);

        Event.Result canContinueSleep = evt.getResult();
        if (canContinueSleep == Event.Result.DEFAULT)
            return !player.world.isDaytime();
        else
            return canContinueSleep == Event.Result.ALLOW;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static ActionResult<ItemStack> onArrowNock(ItemStack item, World world, EntityPlayer player, EnumHand hand, boolean hasAmmo) {
        ArrowNockEvent event = new ArrowNockEvent(player, item, hand, world, hasAmmo);
        if (MikuLib.MikuEventBus().post(event))
            return new ActionResult<>(EnumActionResult.FAIL, item);
        return event.getAction();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onArrowLoose(ItemStack stack, World world, EntityPlayer player, int charge, boolean hasAmmo) {
        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, world, charge, hasAmmo);
        if (MikuLib.MikuEventBus().post(event))
            return -1;
        return event.getCharge();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onProjectileImpact(Entity entity, RayTraceResult ray) {
        return MikuLib.MikuEventBus().post(new ProjectileImpactEvent(entity, ray));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onProjectileImpact(EntityArrow arrow, RayTraceResult ray) {
        return MikuLib.MikuEventBus().post(new ProjectileImpactEvent.Arrow(arrow, ray));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onProjectileImpact(EntityFireball fireball, RayTraceResult ray) {
        return MikuLib.MikuEventBus().post(new ProjectileImpactEvent.Fireball(fireball, ray));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onProjectileImpact(EntityThrowable throwable, RayTraceResult ray) {
        boolean oldEvent = MikuLib.MikuEventBus().post(new ThrowableImpactEvent(throwable, ray));
        boolean newEvent = MikuLib.MikuEventBus().post(new ProjectileImpactEvent.Throwable(throwable, ray));
        return oldEvent || newEvent; // TODO: clean up when old event is removed
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onReplaceBiomeBlocks(IChunkGenerator gen, int x, int z, ChunkPrimer primer, World world) {
        ChunkGeneratorEvent.ReplaceBiomeBlocks event = new ChunkGeneratorEvent.ReplaceBiomeBlocks(gen, x, z, primer, world);
        MikuLib.MikuEventBus().post(event);
        return event.getResult() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onChunkPopulate(boolean pre, IChunkGenerator gen, World world, Random rand, int x, int z, boolean hasVillageGenerated) {
        MikuLib.MikuEventBus().post(pre ? new PopulateChunkEvent.Pre(gen, world, rand, x, z, hasVillageGenerated) : new PopulateChunkEvent.Post(gen, world, rand, x, z, hasVillageGenerated));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static LootTable loadLootTable(ResourceLocation name, LootTable table, LootTableManager lootTableManager) {
        LootTableLoadEvent event = new LootTableLoadEvent(name, table, lootTableManager);
        if (MikuLib.MikuEventBus().post(event))
            return LootTable.EMPTY_LOOT_TABLE;
        return event.getTable();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean canCreateFluidSource(World world, BlockPos pos, IBlockState state, boolean def) {
        BlockEvent.CreateFluidSourceEvent evt = new BlockEvent.CreateFluidSourceEvent(world, pos, state);
        MikuLib.MikuEventBus().post(evt);

        Event.Result result = evt.getResult();
        return result == Event.Result.DEFAULT ? def : result == Event.Result.ALLOW;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onTrySpawnPortal(World world, BlockPos pos, BlockPortal.Size size) {
        return MikuLib.MikuEventBus().post(new BlockEvent.PortalSpawnEvent(world, pos, world.getBlockState(pos), size));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static int onEnchantmentLevelSet(World world, BlockPos pos, int enchantRow, int power, ItemStack itemStack, int level) {
        net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent e = new net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent(world, pos, enchantRow, power, itemStack, level);
        MikuLib.MikuEventBus().post(e);
        return e.getLevel();
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean onEntityDestroyBlock(EntityLivingBase entity, BlockPos pos, IBlockState state) {
        return !MikuLib.MikuEventBus().post(new LivingDestroyBlockEvent(entity, pos, state));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static boolean getMobGriefingEvent(World world, Entity entity) {
        EntityMobGriefingEvent event = new EntityMobGriefingEvent(entity);
        MikuLib.MikuEventBus().post(event);

        Event.Result result = event.getResult();
        return result == Event.Result.DEFAULT ? world.getGameRules().getBoolean("mobGriefing") : result == Event.Result.ALLOW;
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static void onGameRuleChange(GameRules rules, String ruleName, MinecraftServer server) {
        MikuLib.MikuEventBus().post(new GameRuleChangeEvent(rules, ruleName, server));
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public static MerchantRecipeList listTradeOffers(IMerchant merchant, EntityPlayer player, @Nullable MerchantRecipeList list) {
        MerchantRecipeList dupeList = null;
        if (list != null) {
            dupeList = new MerchantRecipeList();
            dupeList.addAll(list);
        }
        MerchantTradeOffersEvent event = new MerchantTradeOffersEvent(merchant, player, dupeList);
        MikuLib.MikuEventBus().post(event);
        return event.getList();
    }
}
