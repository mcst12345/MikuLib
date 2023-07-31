package miku.lib.common.entity;

import miku.lib.common.api.ProtectedEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MikuTNT extends EntityTNTPrimed implements ProtectedEntity {
    protected boolean remove = false;
    private int fuse;

    public MikuTNT(World worldIn) {
        super(worldIn);
        this.fuse = 80;
    }

    @Override
    public boolean CanBeKilled() {
        return true;
    }

    @Override
    public boolean DEAD() {
        return remove;
    }

    @Override
    public void SetHealth(int health) {

    }

    @Override
    public int GetHealth() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void Hurt(int amount) {

    }

    public void explode() {
        this.world.createExplosion(this, this.posX, this.posY, this.posZ, 1000.0F, true);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        --this.fuse;

        if (this.fuse <= 0) {
            if (!this.world.isRemote) {
                this.explode();
            }
            remove = true;
        } else {
            this.handleWaterMovement();
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    public EntityLivingBase getTntPlacedBy() {
        return null;
    }
}
