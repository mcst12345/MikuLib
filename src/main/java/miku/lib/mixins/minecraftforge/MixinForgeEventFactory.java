package miku.lib.mixins.minecraftforge;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
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
        try {
            MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(world, entity, aabb, outList));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at GetCollisionBoxesEvent");
            t.printStackTrace();
        }
        return outList.isEmpty();
    }
}
