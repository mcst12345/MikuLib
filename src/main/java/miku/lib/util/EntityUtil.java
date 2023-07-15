package miku.lib.util;

import com.chaoswither.event.ChaosUpdateEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;

public class EntityUtil {
    public static boolean isProtected(@Nullable Object object){
        return false;
    }

    public static boolean isDEAD(Entity entity){
        return false;
    }

    public static void Kill(@Nullable Entity entity){
    }

    public static boolean isKilling(){
        return false;
    }

    public static void RangeKill(World world, double x, double y, double z, double range){
    }

    public static void Kill(Collection<Entity> entities){
    }

    public static void KillNoSizeEntity(Entity entity){
    }

    public static void RangeKill(final Entity Player, int range){
    }

    public static void REMOVE(World world){
    }

    public static void ClearBadEntities(@Nullable World world){
    }
}
