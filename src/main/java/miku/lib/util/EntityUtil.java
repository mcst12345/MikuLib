package miku.lib.util;

import com.chaoswither.chaoswither;
import com.chaoswither.event.ChaosUpdateEvent;
import com.chaoswither.event.DetermineEvent;
import com.google.common.collect.Lists;
import miku.lib.api.ProtectedEntity;
import miku.lib.api.iChaosUpdateEvent;
import miku.lib.api.iEntity;
import miku.lib.api.iInventoryPlayer;
import miku.lib.item.SpecialItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityUtil {
    public static ChaosUpdateEvent chaos_event;

    protected static List<Entity> DEAD = new ArrayList<>();

    protected static boolean Killing=false;

    public static boolean isProtected(@Nullable Object object){
        if(!(object instanceof Entity))return false;
        Entity entity = (Entity) object;
        if(entity instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) entity;
            if(SpecialItem.isInList(player))return true;
            if (player.inventory != null) {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                        if (((SpecialItem) stack.getItem()).isOwner(player)) {
                            return true;
                        } else {
                            ((iInventoryPlayer) player.inventory).clear();
                            EntityUtil.Kill(player);
                        }
                    }
                }
                ItemStack stack = player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                    if (((SpecialItem) stack.getItem()).isOwner(player)) {
                        return true;
                    } else {
                        ((iInventoryPlayer) player.inventory).clear();
                        EntityUtil.Kill(player);
                    }
                }
            }
        }
        return entity instanceof ProtectedEntity;
    }

    public static boolean isDEAD(Entity entity){
        return DEAD.contains(entity) || ((iEntity)entity).isDEAD();
    }

    public static void Kill(@Nullable Entity entity){
        if(entity == null)return;
        if(isProtected(entity))return;
        DEAD.add(entity);
        Killing=true;
        ((iEntity)entity).kill();
        if(Loader.isModLoaded("chaoswither")){
            try {
                chaoswither.happymode = false;
                DetermineEvent.WITHERLIVE = false;
                ChaosUpdateEvent.godlist.clear();
                ((iChaosUpdateEvent)chaos_event).KILL();
            } catch (Exception ignored) {
            }
        }
        Killing=false;
    }

    public static boolean isKilling(){
        return Killing;
    }

    public static void RangeKill(World world, double x, double y, double z, double range){
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range));
        Kill(list);
    }

    public static void Kill(Collection<Entity> entities){
        for (Entity entity : entities) {
            Kill(entity);
        }
    }

    public static void KillNoSizeEntity(Entity entity){
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

    public static void RangeKill(final EntityPlayer Player, int range){
        List<Entity> list = Player.getEntityWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Player.posX - range, Player.posY - range, Player.posZ - range, Player.posX + range, Player.posY + range, Player.posZ + range));
        list.remove(Player);
        Kill(list);
    }


}
