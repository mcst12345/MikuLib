package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.DifficultyChangeEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onDifficultyChange(EnumDifficulty difficulty, EnumDifficulty oldDifficulty) {
        MikuLib.MikuEventBus().post(new DifficultyChangeEvent(difficulty, oldDifficulty));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingUpdate(EntityLivingBase entity) {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            return false;
        }
        return MikuLib.MikuEventBus().post(new LivingEvent.LivingUpdateEvent(entity));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onLivingSetAttackTarget(EntityLivingBase entity, EntityLivingBase target)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        if (EntityUtil.isProtected(target)) {
            target.isDead = false;
            target.deathTime = 0;
            Update(target);
            return;
        }
        MikuLib.MikuEventBus().post(new LivingSetAttackTargetEvent(entity, target));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return entity instanceof EntityPlayer || !MikuLib.MikuEventBus().post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttack(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return !MikuLib.MikuEventBus().post(new LivingAttackEvent(entity, src, amount));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static LivingKnockBackEvent onLivingKnockBack(EntityLivingBase target, Entity attacker, float strength, double ratioX, double ratioZ) {
        LivingKnockBackEvent event = new LivingKnockBackEvent(target, attacker, strength, ratioX, ratioZ);
        MikuLib.MikuEventBus().post(event);
        return event;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingHurt(EntityLivingBase entity, DamageSource src, float amount) {
        if (MikuInsaneMode.isMikuInsaneMode()) return Float.MAX_VALUE;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return 0.0f;
        }
        LivingHurtEvent event = new LivingHurtEvent(entity, src, amount);
        return (MikuLib.MikuEventBus().post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onLivingDamage(EntityLivingBase entity, DamageSource src, float amount)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return Float.MAX_VALUE;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return 0.0f;
        }
        LivingDamageEvent event = new LivingDamageEvent(entity, src, amount);
        return (MikuLib.MikuEventBus().post(event) ? 0 : event.getAmount());
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingDeath(EntityLivingBase entity, DamageSource src)
    {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(entity)) {
            entity.isDead = false;
            entity.deathTime = 0;
            Update(entity);
            return true;
        }
        return MikuLib.MikuEventBus().post(new LivingDeathEvent(entity, src));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onLivingDrops(EntityLivingBase entity, DamageSource source, ArrayList<EntityItem> drops, int lootingLevel, boolean recentlyHit) {
        return MikuLib.MikuEventBus().post(new LivingDropsEvent(entity, source, drops, lootingLevel, recentlyHit));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    @Nullable
    public static float[] onLivingFall(EntityLivingBase entity, float distance, float damageMultiplier) {
        LivingFallEvent event = new LivingFallEvent(entity, distance, damageMultiplier);
        return (MikuLib.MikuEventBus().post(event) ? null : new float[]{event.getDistance(), event.getDamageMultiplier()});
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static int getLootingLevel(EntityLivingBase target, DamageSource cause, int level) {
        LootingLevelEvent event = new LootingLevelEvent(target, cause, level);
        MikuLib.MikuEventBus().post(event);
        return event.getLootingLevel();
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static double getPlayerVisibilityDistance(EntityPlayer player, double xzDistance, double maxXZDistance) {
        if (EntityUtil.isProtected(player)) return Double.MAX_VALUE;
        PlayerEvent.Visibility event = new PlayerEvent.Visibility(player);
        MikuLib.MikuEventBus().post(event);
        double value = event.getVisibilityModifier() * xzDistance;
        return Math.min(value, maxXZDistance);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onLivingJump(EntityLivingBase entity) {
        MikuLib.MikuEventBus().post(new LivingEvent.LivingJumpEvent(entity));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    @Nullable
    public static EntityItem onPlayerTossEvent(@Nonnull EntityPlayer player, @Nonnull ItemStack item, boolean includeName) {
        player.captureDrops = true;
        EntityItem ret = player.dropItem(item, false, includeName);
        player.capturedDrops.clear();
        player.captureDrops = false;

        if (ret == null) {
            return null;
        }

        ItemTossEvent event = new ItemTossEvent(ret, player);
        if (MikuLib.MikuEventBus().post(event)) {
            return null;
        }

        if (!player.world.isRemote) {
            player.getEntityWorld().spawnEntity(event.getEntityItem());
        }
        return event.getEntityItem();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    @Nullable
    public static ITextComponent onServerChatEvent(NetHandlerPlayServer net, String raw, ITextComponent comp) {
        ServerChatEvent event = new ServerChatEvent(net.player, raw, comp);
        if (MikuLib.MikuEventBus().post(event)) {
            return null;
        }
        return event.getComponent();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static int onBlockBreakEvent(World world, GameType gameType, EntityPlayerMP entityPlayer, BlockPos pos) {
        // Logic from tryHarvestBlock for pre-canceling the event
        boolean preCancelEvent = false;
        ItemStack itemstack = entityPlayer.getHeldItemMainhand();
        if (gameType.isCreative() && !itemstack.isEmpty()
                && !itemstack.getItem().canDestroyBlockInCreative(world, pos, itemstack, entityPlayer))
            preCancelEvent = true;

        if (gameType.hasLimitedInteractions()) {
            if (gameType == GameType.SPECTATOR)
                preCancelEvent = true;

            if (!entityPlayer.isAllowEdit()) {
                if (itemstack.isEmpty() || !itemstack.canDestroy(world.getBlockState(pos).getBlock()))
                    preCancelEvent = true;
            }
        }

        // Tell client the block is gone immediately then process events
        if (world.getTileEntity(pos) == null) {
            SPacketBlockChange packet = new SPacketBlockChange(world, pos);
            packet.blockState = Blocks.AIR.getDefaultState();
            entityPlayer.connection.sendPacket(packet);
        }

        // Post the block break event
        IBlockState state = world.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        event.setCanceled(preCancelEvent);
        MikuLib.MikuEventBus().post(event);

        // Handle if the event is canceled
        if (event.isCanceled() && !EntityUtil.isProtected(entityPlayer)) {
            // Let the client know the block still exists
            entityPlayer.connection.sendPacket(new SPacketBlockChange(world, pos));

            // Update any tile entity data for this block
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity != null) {
                Packet<?> pkt = tileentity.getUpdatePacket();
                if (pkt != null) {
                    entityPlayer.connection.sendPacket(pkt);
                }
            }
        }
        return event.isCanceled() && !EntityUtil.isProtected(entityPlayer) ? -1 : event.getExpToDrop();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onAnvilChange(ContainerRepair container, @Nonnull ItemStack left, @Nonnull ItemStack right, IInventory outputSlot, String name, int baseCost) {
        AnvilUpdateEvent e = new AnvilUpdateEvent(left, right, name, baseCost);
        if (MikuLib.MikuEventBus().post(e)) return false;
        if (e.getOutput().isEmpty()) return true;

        outputSlot.setInventorySlotContents(0, e.getOutput());
        container.maximumCost = e.getCost();
        container.materialCost = e.getMaterialCost();
        return false;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static float onAnvilRepair(EntityPlayer player, @Nonnull ItemStack output, @Nonnull ItemStack left, @Nonnull ItemStack right) {
        AnvilRepairEvent e = new AnvilRepairEvent(player, left, right, output);
        MikuLib.MikuEventBus().post(e);
        return e.getBreakChance();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onNoteChange(TileEntityNote te, byte old) {
        NoteBlockEvent.Change e = new NoteBlockEvent.Change(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()), old, te.note);
        if (MikuLib.MikuEventBus().post(e)) {
            te.note = old;
            return false;
        }
        te.note = (byte) e.getVanillaNoteId();
        return true;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target) {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        if (EntityUtil.isProtected(target)) {
            target.isDead = false;
            if (target instanceof EntityLivingBase) ((EntityLivingBase) target).deathTime = 0;
            Update(target);
            return true;
        }
        if (MikuLib.MikuEventBus().post(new AttackEntityEvent(player, target))) return false;
        ItemStack stack = player.getHeldItemMainhand();
        return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onTravelToDimension(Entity entity, int dimension) {
        if (EntityUtil.isDEAD(entity)) return false;
        EntityTravelToDimensionEvent event = new EntityTravelToDimensionEvent(entity, dimension);
        MikuLib.MikuEventBus().post(event);
        if (event.isCanceled()) {
            // Revert variable back to true as it would have been set to false
            if (entity instanceof EntityMinecartContainer) {
                ((EntityMinecartContainer) entity).dropContentsWhenDead = true;
            }
        }
        return !event.isCanceled();
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static EnumActionResult onInteractEntityAt(EntityPlayer player, Entity entity, Vec3d vec3d, EnumHand hand) {
        PlayerInteractEvent.EntityInteractSpecific evt = new PlayerInteractEvent.EntityInteractSpecific(player, hand, entity, vec3d);
        MikuLib.MikuEventBus().post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static EnumActionResult onInteractEntity(EntityPlayer player, Entity entity, EnumHand hand) {
        PlayerInteractEvent.EntityInteract evt = new PlayerInteractEvent.EntityInteract(player, hand, entity);
        MikuLib.MikuEventBus().post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static EnumActionResult onItemRightClick(EntityPlayer player, EnumHand hand) {
        PlayerInteractEvent.RightClickItem evt = new PlayerInteractEvent.RightClickItem(player, hand);
        MikuLib.MikuEventBus().post(evt);
        return evt.isCanceled() ? evt.getCancellationResult() : null;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(EntityPlayer player, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        PlayerInteractEvent.LeftClickBlock evt = new PlayerInteractEvent.LeftClickBlock(player, pos, face, hitVec);
        MikuLib.MikuEventBus().post(evt);
        return evt;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static PlayerInteractEvent.RightClickBlock onRightClickBlock(EntityPlayer player, EnumHand hand, BlockPos pos, EnumFacing face, Vec3d hitVec) {
        PlayerInteractEvent.RightClickBlock evt = new PlayerInteractEvent.RightClickBlock(player, hand, pos, face, hitVec);
        MikuLib.MikuEventBus().post(evt);
        return evt;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onEmptyClick(EntityPlayer player, EnumHand hand) {
        MikuLib.MikuEventBus().post(new PlayerInteractEvent.RightClickEmpty(player, hand));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onEmptyLeftClick(EntityPlayer player) {
        MikuLib.MikuEventBus().post(new PlayerInteractEvent.LeftClickEmpty(player));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean onCropsGrowPre(World worldIn, BlockPos pos, IBlockState state, boolean def) {
        BlockEvent ev = new BlockEvent.CropGrowEvent.Pre(worldIn, pos, state);
        MikuLib.MikuEventBus().post(ev);
        return (ev.getResult() == Event.Result.ALLOW || (ev.getResult() == Event.Result.DEFAULT && def));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onCropsGrowPost(World worldIn, BlockPos pos, IBlockState state, IBlockState blockState) {
        MikuLib.MikuEventBus().post(new BlockEvent.CropGrowEvent.Post(worldIn, pos, state, worldIn.getBlockState(pos)));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    @Nullable
    public static CriticalHitEvent getCriticalHit(EntityPlayer player, Entity target, boolean vanillaCritical, float damageModifier) {
        CriticalHitEvent hitResult = new CriticalHitEvent(player, target, damageModifier, vanillaCritical);
        MikuLib.MikuEventBus().post(hitResult);
        if (hitResult.getResult() == Event.Result.ALLOW || (vanillaCritical && hitResult.getResult() == Event.Result.DEFAULT)) {
            return hitResult;
        }
        return null;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void onAdvancement(EntityPlayerMP player, Advancement advancement) {
        MikuLib.MikuEventBus().post(new AdvancementEvent(player, advancement));
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    public static boolean onFarmlandTrample(World world, BlockPos pos, IBlockState state, float fallDistance, Entity entity) {

        if (entity.canTrample(world, state.getBlock(), pos, fallDistance)) {
            BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(world, pos, state, fallDistance, entity);
            MikuLib.MikuEventBus().post(event);
            return !event.isCanceled();
        }
        return false;
    }

    private static void Update(Entity e) {
        MinecraftServer minecraftserver = e.getServer();
        if (minecraftserver != null) {
            WorldServer worldserver = minecraftserver.getWorld(e.dimension);
            worldserver.resetUpdateEntityTick();
            worldserver.updateEntityWithOptionalForce(e, false);
            e.world.profiler.endStartSection("reloading");
        }

    }
}
