package miku.lib.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;

//Ha!Ha!
@SuppressWarnings("ALL")
public class EntityUtil {
    public static boolean isProtected(@Nullable Object object){
        return false;
    }//is entity protected

    public static boolean isDEAD(Entity entity){
        return false;
    }//can the entity be alive

    public static void Kill(@Nullable Entity entity){//Kill entity.
    }

    public static boolean isKilling(){
        return false;
    }

    public static void RangeKill(World world, double x, double y, double z, double range){
    }

    public static void Kill(Collection<Entity> entities){//kill a list of entity
    }

    public static void KillNoSizeEntity(Entity entity){
    }

    public static void RangeKill(final Entity Player, int range){
    }

    public static void REMOVE(World world){//remove dead entities from world.
    }

    public static void ClearBadEntities(@Nullable World world){
    }
}
