package miku.lib.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

public class EmptyTeleporter implements ITeleporter {
    public static final EmptyTeleporter INSTANCE = new EmptyTeleporter();

    private EmptyTeleporter() {
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {

    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
