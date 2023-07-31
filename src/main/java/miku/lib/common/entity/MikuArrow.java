package miku.lib.common.entity;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MikuArrow extends EntityArrow {
    public MikuArrow(World worldIn) {
        super(worldIn);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
