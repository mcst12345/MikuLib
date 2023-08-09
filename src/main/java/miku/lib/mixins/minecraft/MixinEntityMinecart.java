package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IMinecartCollisionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = EntityMinecart.class)
public abstract class MixinEntityMinecart extends Entity implements IWorldNameable {
    @Shadow
    public abstract int getRollingAmplitude();

    @Shadow
    public abstract void setRollingAmplitude(int rollingAmplitude);

    @Shadow
    public abstract float getDamage();

    @Shadow
    public abstract void setDamage(float damage);

    @Shadow
    private int turnProgress;

    @Shadow
    private double minecartX;

    @Shadow
    private double minecartY;

    @Shadow
    private double minecartZ;

    @Shadow
    private double minecartYaw;

    @Shadow
    private double minecartPitch;

    @Shadow(remap = false)
    public abstract boolean canUseRail();

    @Shadow
    protected abstract void moveAlongTrack(BlockPos pos, IBlockState state);

    @Shadow
    public abstract void onActivatorRailPass(int x, int y, int z, boolean receivingPower);

    @Shadow
    protected abstract void moveDerailedMinecart();

    @Shadow
    private boolean isInReverse;

    @Shadow(remap = false)
    @Nullable
    public static IMinecartCollisionHandler getCollisionHandler() {
        return null;
    }

    @Shadow(remap = false)
    protected abstract BlockPos getCurrentRailPosition();

    @Shadow(remap = false)
    public abstract boolean canBeRidden();

    @Shadow(remap = false)
    public abstract boolean isPoweredCart();

    public MixinEntityMinecart(World worldIn) {
        super(worldIn);
    }

    /**
     * @author mcst12345
     * @reason Fuck!!
     */
    @Overwrite
    public void onUpdate() {
        if (this.getRollingAmplitude() > 0) {
            this.setRollingAmplitude(this.getRollingAmplitude() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        if (this.posY < -64.0D) {
            this.outOfWorld();
        }

        if (!this.world.isRemote && this.world instanceof WorldServer) {
            this.world.profiler.startSection("portal");
            MinecraftServer minecraftserver = this.world.getMinecraftServer();
            int i = this.getMaxInPortalTime();

            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (!this.isRiding() && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        int j;

                        if (this.world.provider.getDimensionType().getId() == -1) {
                            j = 0;
                        } else {
                            j = -1;
                        }

                        this.changeDimension(j);
                    }

                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0) {
                    this.portalCounter = 0;
                }
            }

            if (this.timeUntilPortal > 0) {
                --this.timeUntilPortal;
            }

            this.world.profiler.endSection();
        }

        if (this.world.isRemote) {
            if (this.turnProgress > 0) {
                double d4 = this.posX + (this.minecartX - this.posX) / (double) this.turnProgress;
                double d5 = this.posY + (this.minecartY - this.posY) / (double) this.turnProgress;
                double d6 = this.posZ + (this.minecartZ - this.posZ) / (double) this.turnProgress;
                double d1 = MathHelper.wrapDegrees(this.minecartYaw - (double) this.rotationYaw);
                this.rotationYaw = (float) ((double) this.rotationYaw + d1 / (double) this.turnProgress);
                this.rotationPitch = (float) ((double) this.rotationPitch + (this.minecartPitch - (double) this.rotationPitch) / (double) this.turnProgress);
                --this.turnProgress;
                this.setPosition(d4, d5, d6);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            } else {
                this.setPosition(this.posX, this.posY, this.posZ);
                this.setRotation(this.rotationYaw, this.rotationPitch);
            }
        } else {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (!this.hasNoGravity()) {
                this.motionY -= 0.03999999910593033D;
            }

            int k = MathHelper.floor(this.posX);
            int l = MathHelper.floor(this.posY);
            int i1 = MathHelper.floor(this.posZ);

            if (BlockRailBase.isRailBlock(this.world, new BlockPos(k, l - 1, i1))) {
                --l;
            }

            BlockPos blockpos = new BlockPos(k, l, i1);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (canUseRail() && BlockRailBase.isRailBlock(iblockstate)) {
                this.moveAlongTrack(blockpos, iblockstate);

                if (iblockstate.getBlock() == Blocks.ACTIVATOR_RAIL) {
                    this.onActivatorRailPass(k, l, i1, iblockstate.getValue(BlockRailPowered.POWERED));
                }
            } else {
                this.moveDerailedMinecart();
            }

            this.doBlockCollisions();
            this.rotationPitch = 0.0F;
            double d0 = this.prevPosX - this.posX;
            double d2 = this.prevPosZ - this.posZ;

            if (d0 * d0 + d2 * d2 > 0.001D) {
                this.rotationYaw = (float) (MathHelper.atan2(d2, d0) * 180.0D / Math.PI);

                if (this.isInReverse) {
                    this.rotationYaw += 180.0F;
                }
            }

            double d3 = MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);

            if (d3 < -170.0D || d3 >= 170.0D) {
                this.rotationYaw += 180.0F;
                this.isInReverse = !this.isInReverse;
            }

            this.setRotation(this.rotationYaw, this.rotationPitch);

            AxisAlignedBB box;
            if (getCollisionHandler() != null)
                box = getCollisionHandler().getMinecartCollisionBox((EntityMinecart) (Object) this);
            else box = this.getEntityBoundingBox().grow(0.20000000298023224D, 0.0D, 0.20000000298023224D);

            if (canBeRidden() && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
                List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, EntitySelectors.getTeamCollisionPredicate(this));

                if (!list.isEmpty()) {
                    for (Entity entity1 : list) {
                        if (!(entity1 instanceof EntityPlayer) && !(entity1 instanceof EntityIronGolem) && !(entity1 instanceof EntityMinecart) && !this.isBeingRidden() && !entity1.isRiding()) {
                            entity1.startRiding(this);
                        } else {
                            entity1.applyEntityCollision(this);
                        }
                    }
                }
            } else {
                for (Entity entity : this.world.getEntitiesWithinAABBExcludingEntity(this, box)) {
                    if (!this.isPassenger(entity) && entity.canBePushed() && entity instanceof EntityMinecart) {
                        entity.applyEntityCollision(this);
                    }
                }
            }

            this.handleWaterMovement();
            MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.minecart.MinecartUpdateEvent((EntityMinecart) (Object) this, this.getCurrentRailPosition()));
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!!
     */
    @Overwrite
    public void applyEntityCollision(@Nonnull Entity entityIn) {
        MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.minecart.MinecartCollisionEvent((EntityMinecart) (Object) this, entityIn));
        if (getCollisionHandler() != null) {
            getCollisionHandler().onEntityCollision((EntityMinecart) (Object) this, entityIn);
            return;
        }
        if (!this.world.isRemote) {
            if (!entityIn.noClip && !this.noClip) {
                if (!this.isPassenger(entityIn)) {
                    double d0 = entityIn.posX - this.posX;
                    double d1 = entityIn.posZ - this.posZ;
                    double d2 = d0 * d0 + d1 * d1;

                    if (d2 >= 9.999999747378752E-5D) {
                        d2 = MathHelper.sqrt(d2);
                        d0 = d0 / d2;
                        d1 = d1 / d2;
                        double d3 = 1.0D / d2;

                        if (d3 > 1.0D) {
                            d3 = 1.0D;
                        }

                        d0 = d0 * d3;
                        d1 = d1 * d3;
                        d0 = d0 * 0.10000000149011612D;
                        d1 = d1 * 0.10000000149011612D;
                        d0 = d0 * (double) (1.0F - this.entityCollisionReduction);
                        d1 = d1 * (double) (1.0F - this.entityCollisionReduction);
                        d0 = d0 * 0.5D;
                        d1 = d1 * 0.5D;

                        if (entityIn instanceof EntityMinecart) {
                            double d4 = entityIn.posX - this.posX;
                            double d5 = entityIn.posZ - this.posZ;
                            Vec3d vec3d = (new Vec3d(d4, 0.0D, d5)).normalize();
                            Vec3d vec3d1 = (new Vec3d(MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
                            double d6 = Math.abs(vec3d.dotProduct(vec3d1));

                            if (d6 < 0.800000011920929D) {
                                return;
                            }

                            double d7 = entityIn.motionX + this.motionX;
                            double d8 = entityIn.motionZ + this.motionZ;

                            if (((EntityMinecart) entityIn).isPoweredCart() && !isPoweredCart()) {
                                this.motionX *= 0.20000000298023224D;
                                this.motionZ *= 0.20000000298023224D;
                                this.addVelocity(entityIn.motionX - d0, 0.0D, entityIn.motionZ - d1);
                                entityIn.motionX *= 0.949999988079071D;
                                entityIn.motionZ *= 0.949999988079071D;
                            } else if (!((EntityMinecart) entityIn).isPoweredCart() && isPoweredCart()) {
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                                this.motionX *= 0.949999988079071D;
                                this.motionZ *= 0.949999988079071D;
                            } else {
                                d7 = d7 / 2.0D;
                                d8 = d8 / 2.0D;
                                this.motionX *= 0.20000000298023224D;
                                this.motionZ *= 0.20000000298023224D;
                                this.addVelocity(d7 - d0, 0.0D, d8 - d1);
                                entityIn.motionX *= 0.20000000298023224D;
                                entityIn.motionZ *= 0.20000000298023224D;
                                entityIn.addVelocity(d7 + d0, 0.0D, d8 + d1);
                            }
                        } else {
                            this.addVelocity(-d0, 0.0D, -d1);
                            entityIn.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
                        }
                    }
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public boolean processInitialInteract(@Nonnull EntityPlayer player, @Nonnull net.minecraft.util.EnumHand hand) {
        return MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.minecart.MinecartInteractEvent((EntityMinecart) (Object) this, player, hand));
    }
}
