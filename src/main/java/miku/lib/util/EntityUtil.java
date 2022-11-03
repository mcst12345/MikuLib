package miku.lib.util;

import com.google.common.collect.Lists;
import miku.lib.api.ProtectedEntity;
import miku.lib.api.iEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class EntityUtil {
    protected static boolean Killing=false;

    public static boolean isProtected(Entity entity){
        if(entity instanceof EntityPlayer){
            //TODO
        }
        return entity instanceof ProtectedEntity;
    }

    public static boolean isDEAD(Entity entity){
        return false;
    }

    public static void Kill(Entity entity){
        Killing=true;
        ((iEntity)entity).kill();
        Killing=false;
    }

    public boolean isKilling(){
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
}
