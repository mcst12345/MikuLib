package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = ForgeEventFactory.class, remap = false)
public class MixinForgeEventFactory {
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
}
