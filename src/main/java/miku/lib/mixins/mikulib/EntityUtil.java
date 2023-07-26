package miku.lib.mixins.mikulib;

import com.anotherstar.common.entity.EntityLoli;
import com.chaoswither.entity.EntityChaosWither;
import com.google.common.collect.Lists;
import miku.lib.common.api.*;
import miku.lib.common.item.SpecialItem;
import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.KillEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Mixin(value = miku.lib.common.util.EntityUtil.class, remap = false)
public class EntityUtil {
    private static final List<UUID> Dead = new ArrayList<>();
    private static final List<Entity> DEAD = new ArrayList<>();

    private static boolean Killing = false;

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean isProtected(@Nullable Object object){
        if(!(object instanceof Entity))return false;
        Entity entity = (Entity) object;
        if(entity instanceof EntityPlayer){
            if(entity.world.isRemote){
                if(((iMinecraft) Minecraft.getMinecraft()).protect())return true;
            }
            EntityPlayer player = (EntityPlayer) entity;
            if(((iEntityPlayer)player).isMiku() && Loader.isModLoaded("miku")) {
                if(entity.world.isRemote)((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                return true;
            }
            if(SpecialItem.isInList(player)) {
                if(entity.world.isRemote)((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                return true;
            }
            if (player.inventory != null) {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                        if (((SpecialItem) stack.getItem()).isOwner(stack,player)) {
                            if(entity.world.isRemote)((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                            return true;
                        } else {
                            ((iInventoryPlayer) player.inventory).clear();
                            return false;
                        }
                    }
                }
                ItemStack stack = player.inventory.getItemStack();
                if (!stack.isEmpty() && stack.getItem() instanceof SpecialItem) {
                    if (((SpecialItem) stack.getItem()).isOwner(stack,player)) {
                        if(entity.world.isRemote)((iMinecraft) Minecraft.getMinecraft()).SetProtected();
                        return true;
                    } else {
                        ((iInventoryPlayer) player.inventory).clear();
                        return false;
                    }
                }
            }
        }
        if(entity instanceof ProtectedEntity){
            return !((ProtectedEntity) entity).CanBeKilled() || !((ProtectedEntity) entity).DEAD();
        }
        return false;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean isDEAD(Entity entity){
        if(entity == null || isProtected(entity))return false;
        return DEAD.contains(entity) || ((iEntity)entity).isDEAD() || Dead.contains(entity.getUniqueID()) || entity.dimension == -25;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void Kill(@Nullable Entity entity){
        if(entity == null)return;
        if(isProtected(entity)) {
            if(entity instanceof ProtectedEntity)((ProtectedEntity)entity).SetHealth(0);
            return;
        }
        DEAD.add(entity);
        Dead.add(entity.getUniqueID());
        Killing=true;


        if(!entity.world.isRemote) NetworkHandler.INSTANCE.sendMessageToAllPlayer(new KillEntity( entity.getEntityId(), entity.getEntityId()),entity.world);


        MinecraftServer minecraftserver = entity.getServer();
        WorldServer worldserver = null;
        WorldServer worldserver1 = null;
        if(minecraftserver!=null){
            worldserver = minecraftserver.getWorld(entity.dimension);
            worldserver1 = minecraftserver.getWorld(-25);
        }
        entity.dimension = -25;

        ((iEntity)entity).kill();

        if(minecraftserver!=null){
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
        }
        Killing=false;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static boolean isKilling(){
        return Killing;
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void RangeKill(World world, double x, double y, double z, double range){
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range));
        Kill(list);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void Kill(Collection<Entity> entities){
        for (Entity entity : entities) {
            Kill(entity);
        }
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
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

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void RangeKill(final Entity Player, int range){
        List<Entity> list = Player.getEntityWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(Player.posX - range, Player.posY - range, Player.posZ - range, Player.posX + range, Player.posY + range, Player.posZ + range));
        list.removeIf(miku.lib.common.util.EntityUtil::isProtected);
        Kill(list);
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void REMOVE(World world){
        world.loadedEntityList.removeAll(DEAD);
        world.loadedEntityList.removeIf(e -> Dead.contains(e.getUniqueID()));
    }

    /**
     * @author mcst12345
     * @reason ...
     */
    @Overwrite
    public static void ClearBadEntities(@Nullable World world){
        if(world == null)return;
        if(world.loadedEntityList == null)return;
        if(world.loadedEntityList.isEmpty())return;
        for(Entity entity : world.loadedEntityList){
            if(Loader.isModLoaded("lolipickaxe")){
                if(entity instanceof EntityLoli)Kill(entity);
            }
            if(Loader.isModLoaded("chaoswither")){
                if(entity instanceof EntityChaosWither)Kill(entity);
            }
        }
    }
}
