package miku.lib.common.entity;

//PRESENT DAY
//PRESENT TIME
//HaHaHaHaHa!

import miku.lib.common.api.ProtectedEntity;
import miku.lib.common.core.MikuLib;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.world.World;

//https://bgm.tv/subject/2582

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
    }

    @Override
    public void onUpdate() {
        this.deathTime = 0;
        this.hurtTime = 0;
        super.onUpdate();
        this.deathTime = 0;
        this.hurtTime = 0;
    }
}
