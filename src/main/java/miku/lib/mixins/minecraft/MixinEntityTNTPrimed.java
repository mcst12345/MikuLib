package miku.lib.mixins.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EntityTNTPrimed.class)
public abstract class MixinEntityTNTPrimed extends Entity {

    public MixinEntityTNTPrimed(World worldIn) {
        super(worldIn);
    }
}
