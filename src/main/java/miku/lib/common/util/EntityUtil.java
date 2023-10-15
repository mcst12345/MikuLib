package miku.lib.common.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import miku.lib.client.api.iMinecraft;
import miku.lib.client.util.GuiUtil;
import miku.lib.common.Native.NativeUtil;
import miku.lib.common.api.ProtectedEntity;
import miku.lib.common.api.iEntity;
import miku.lib.common.api.iEntityPlayer;
import miku.lib.common.api.iInventoryPlayer;
import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.thread.FuckEntityThread;
import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.ExitGame;
import miku.lib.network.packets.KillEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EntityUtil {
    private static final List<UUID> Dead = new ObjectArrayList<>();
    private static final List<Entity> DEAD = new ObjectArrayList<>();

    private static boolean Killing = false;

    public static boolean isProtected(@Nullable Object object) {
        if (!(object instanceof Entity)) return false;
        Entity entity = (Entity) object;
        if (entity instanceof EntityPlayer) {
            if (entity.world.isRemote) {
                if (((iMinecraft) Minecraft.getMinecraft()).protect()) return true;
            }
            EntityPlayer player = (EntityPlayer) entity;
            if (((iEntityPlayer) player).isMiku() && Loader.isModLoaded("miku")) {
                if (entity.world.isRemote) ((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                return true;
            }
            if (SpecialItem.isInList(player)) {
                if (entity.world.isRemote) ((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                return true;
            }
            if (player.inventory != null) {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                        if (((SpecialItem) stack.getItem()).isOwner(stack, player)) {
                            if (entity.world.isRemote) ((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                            return true;
                        } else {
                            ((iInventoryPlayer) player.inventory).Clear();
                            return false;
                        }
                    }
                }
                ItemStack stack = player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                    if (((SpecialItem) stack.getItem()).isOwner(stack, player)) {
                        if (entity.world.isRemote) ((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                        return true;
                    } else {
                        ((iInventoryPlayer) player.inventory).Clear();
                        return false;
                    }
                }
            }
        } else if (entity instanceof ProtectedEntity) {
            return !((ProtectedEntity) entity).CanBeKilled() || !((ProtectedEntity) entity).DEAD();
        }
        return false;
    }//is entity protected

    public static boolean isDEAD(Entity entity) {
        if (entity == null || isProtected(entity)) return false;
        boolean result = NativeUtil.MikuListContains("Dead", entity) || ((iEntity) entity).isDEAD() || entity.dimension == -25;
        try {
            result = result || NativeUtil.MikuListContains("DeadUUID", entity.getUniqueID());
        } catch (Throwable t) {
            t.printStackTrace();
            result = result || Dead.contains(entity.getUniqueID()) || DEAD.contains(entity);
        }
        return result;
    }//can the entity be alive.

    public static void Kill(@Nullable Entity entity) {//Kill Entity
        if (entity == null) return;
        if (isProtected(entity)) {
            if (entity instanceof ProtectedEntity) ((ProtectedEntity) entity).SetHealth(0);
            return;
        }
        try {
            NativeUtil.MikuListAdd("Dead", entity);
        } catch (Throwable t) {
            t.printStackTrace();
            DEAD.add(entity);
        }
        try {
            NativeUtil.MikuListAdd("DeadUUID", entity.getUniqueID());
        } catch (Throwable t) {
            t.printStackTrace();
            Dead.add(entity.getUniqueID());
        }
        Killing = true;


        if (!entity.world.isRemote)
            NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity(entity.getEntityId(), entity.getEntityId()), entity.world);


        MinecraftServer minecraftserver = entity.getServer();
        WorldServer worldserver = null;
        WorldServer worldserver1 = null;
        if (minecraftserver != null) {
            worldserver = minecraftserver.getWorld(entity.dimension);
            worldserver1 = minecraftserver.getWorld(-25);
        }
        entity.dimension = -25;

        try {
            NativeUtil.Kill(entity);
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) entity;
                if ((boolean) Sqlite.GetValueFromTable("miku_kill_kick_attack", "CONFIG", 0)) {
                    NetworkHandler.INSTANCE.sendMessageToPlayer(new ExitGame(), playerMP);
                    playerMP.connection.disconnect(new TextComponentString("Goodbye!"));
                }
            }
            if (Launch.Client) {
                if (entity.equals(Minecraft.getMinecraft().player)) {
                    GuiUtil.DisPlayTheGui();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            ((iEntity) entity).kill();
        }


        if (MikuInsaneMode.isMikuInsaneMode()) UnsafeUtil.Fuck(entity);

        if (minecraftserver != null) {
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
        }

        Thread Fuck = new FuckEntityThread(entity);
        Fuck.start();

        Killing = false;
    }

    public static synchronized boolean isKilling() {
        return Killing;
    }

    public static void RangeKill(World world, double x, double y, double z, double range) {
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range));
        Kill(list);
    }

    public static void Kill(Collection<Entity> entities) {//Kill a list if entity
        for (Entity entity : entities) {
            Kill(entity);
        }
    }

    public static void KillNoSizeEntity(Entity entity) {
        List<Entity> entities = Lists.newArrayList();
        for (int dist = 0; dist <= 100; dist += 2) {
            AxisAlignedBB bb = entity.getEntityBoundingBox();
            Vec3d vec = entity.getLookVec();
            vec = vec.normalize();
            bb = bb.grow(0.01 * dist + 2.0, 0.01 * dist + 0.25, 0.01 * dist + 2.0);
            bb = bb.offset(vec.x * dist, vec.y * dist, vec.z * dist);
            List<Entity> list = entity.world.getEntitiesWithinAABB(Entity.class, bb);
            list.removeAll(entities);
            list.removeIf(en -> en.getDistance(en) > 1000);
            list.remove(entity);
            entities.addAll(list);
        }
        Kill(entities);
    }

    public static void RangeKill(final Entity Player, int range) {
        List<Entity> list = Player.getEntityWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Player.posX - range, Player.posY - range, Player.posZ - range, Player.posX + range, Player.posY + range, Player.posZ + range));
        list.removeIf(miku.lib.common.util.EntityUtil::isProtected);
        Kill(list);
    }

    public static void REMOVE(List<Entity> list) {//REMOVE dead entities from world
        try {
            list.removeAll(Arrays.asList(NativeUtil.GetObjectsFromList("Dead")));
        } catch (Throwable t) {
            t.printStackTrace();
            list.removeAll(DEAD);
        }
        try {
            list.removeIf(e -> e.getUniqueID() != null && NativeUtil.MikuListContains("DeadUUID", e.getUniqueID()));
        } catch (Throwable t) {
            t.printStackTrace();
            list.removeIf(e -> e.getUniqueID() != null && Dead.contains(e.getUniqueID()));
        }
    }

    public static boolean isGoodEntity(@Nullable Entity entity) {
        if (entity == null) return false;
        Class<? extends Entity> cls = entity.getClass();
        return cls == EntityItem.class || cls == EntityEnderEye.class || cls == EntityEnderPearl.class || cls == EntityTNTPrimed.class ||
                cls == EntityExpBottle.class || cls == EntityFallingBlock.class || cls == EntityArrow.class || cls == EntityEgg.class ||
                cls == EntityFishHook.class || cls == EntityPotion.class || cls == EntityLargeFireball.class || cls == EntityDragonFireball.class ||
                cls == EntitySmallFireball.class || cls == EntitySnowball.class || cls == EntityTippedArrow.class;
    }
}
