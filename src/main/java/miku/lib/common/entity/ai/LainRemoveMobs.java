package miku.lib.common.entity.ai;

import miku.lib.common.entity.Lain;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;

public class LainRemoveMobs extends EntityAIBase {
    private final Lain lain;

    public LainRemoveMobs(Lain lain) {
        this.lain = lain;
    }


    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void startExecuting() {
        for (Entity entity : lain.world.loadedEntityList) {
            if (entity instanceof EntityMob) EntityUtil.Kill(entity);
        }
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }
}
