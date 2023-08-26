package miku.lib.common.entity;

//PRESENT DAY
//PRESENT TIME
//HaHaHaHaHa!

import miku.lib.common.api.ProtectedEntity;
import miku.lib.common.core.MikuLib;
import miku.lib.common.entity.ai.LainRemoveMobs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.world.World;

public class Lain extends EntityCreature implements ProtectedEntity {
    static {
        MikuLib.setLAIN();
    }

    public Lain(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean CanBeKilled() {
        return false;
    }

    @Override
    public boolean DEAD() {
        return false;
    }

    @Override
    public void SetHealth(int health) {

    }

    @Override
    public int GetHealth() {
        return 20;
    }

    @Override
    public void Hurt(int amount) {

    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIMoveIndoors(this));
        this.tasks.addTask(2, new EntityAILookIdle(this));
        this.tasks.addTask(3, new LainRemoveMobs(this));
    }
}
