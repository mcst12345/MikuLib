package miku.lib.mixins.minecraft;

import miku.lib.client.util.GuiUtil;
import miku.lib.common.api.*;
import miku.lib.common.core.MikuLib;
import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.FieldUtil;
import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.ExitGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

@Mixin(value = Entity.class)
public abstract class MixinEntity implements iEntity, Serializable {
    public void SetTimeStop(boolean stop) {
        isTimeStop = !isTimeStop;
    }

    protected boolean DEAD;

    public boolean isDEAD() {
        return DEAD;
    }

    @Shadow
    public World world;
    @Shadow
    public double posX;
    @Shadow
    public double posY;
    @Shadow
    public double posZ;
    @Shadow
    public float rotationYaw;
    @Shadow
    public float rotationPitch;
    @Shadow
    public float width;
    @Shadow
    public float height;
    @Shadow
    protected Random rand;
    @Shadow
    protected EntityDataManager dataManager;
    @Shadow
    public int dimension;
    protected int _dimension;

    /**
     * @author mcst12345
     * @reason Hi, ojng.
     */
    @Overwrite
    public Vec3d getPositionEyes(float partialTicks) {
        if (partialTicks == 1.0F) {
            return new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        } else {
            double d0 = this.prevPosX + (this.posX - this.prevPosX) * partialTicks;
            double d1 = this.prevPosY + (this.posY - this.prevPosY) * partialTicks + this.getEyeHeight();
            double d2 = this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks;
            return new Vec3d(d0, d1, d2);
        }
    }

    @Inject(at = @At("HEAD"), method = "changeDimension(I)Lnet/minecraft/entity/Entity;", cancellable = true)
    public void changeDimension(int dimensionIn, CallbackInfoReturnable<Entity> cir) {
        if (EntityUtil.isDEAD((Entity) (Object) this)) cir.setReturnValue(null);
    }

    @Inject(at = @At("HEAD"), method = "changeDimension(ILnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/entity/Entity;", cancellable = true, remap = false)
    public void changeDimension(int dimensionIn, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        if (EntityUtil.isDEAD((Entity) (Object) this)) cir.setReturnValue(null);
    }

    @Final
    @Shadow
    protected static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(Entity.class, DataSerializers.BYTE);

    @Shadow
    @Final
    private static DataParameter<String> CUSTOM_NAME;

    @Shadow
    private int entityId;

    @Shadow
    public abstract float getEyeHeight();

    @Shadow
    public double prevPosX;

    @Shadow
    public double prevPosY;

    @Shadow
    public double prevPosZ;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getCustomNameTag() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.dataManager.get(CUSTOM_NAME);
    }

    protected boolean isTimeStop = false;

    @Override
    public void kill() {
        DEAD = true;
        long tmp;
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_70134_J);
            Launch.UNSAFE.putBooleanVolatile(this, tmp, true);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_190534_ay);
            Launch.UNSAFE.putIntVolatile(this, tmp, Integer.MAX_VALUE);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.isAddedToWorld);
            Launch.UNSAFE.putBooleanVolatile(this, tmp, false);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_70128_L);
            Launch.UNSAFE.putBooleanVolatile(this, tmp, true);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_70180_af);
            EntityDataManager manager = (EntityDataManager) Launch.UNSAFE.getObjectVolatile(this, tmp);
            byte b0 = manager.get(FLAGS);
            manager.set(FLAGS, (byte) (b0 | 1 << 5));
            manager.set(FLAGS, (byte) (b0 & ~(1)));
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_70133_I);
            Launch.UNSAFE.putBooleanVolatile(this, tmp, true);
        } catch (Throwable e) {
            System.out.println(e.getLocalizedMessage());
        }
        if (((Entity) (Object) this) instanceof EntityLivingBase) {
            ((iEntityLivingBase) this).Kill();
            if (((Entity) (Object) this) instanceof EntityLiving) {
                ((iEntityLiving) this).Kill();
            }
            if (((Entity) (Object) this) instanceof EntityPlayer) {
                ((iEntityPlayer) this).Kill();
                world.playerEntities.remove(this);
                if (((Entity) (Object) this) instanceof EntityPlayerMP) {
                    EntityPlayerMP playerMP = ((EntityPlayerMP) (Object) this);
                    if (Sqlite.GetBooleanFromTable("miku_kill_kick_attack", "CONFIG")) {
                        NetworkHandler.INSTANCE.sendMessageToPlayer(new ExitGame(), playerMP);
                        playerMP.connection.disconnect(new TextComponentString("Goodbye!"));
                    }
                }
                if (Launch.Client) GuiUtil.DisPlayTheGui();
            }
        }
        ((iWorld)world).remove(((Entity)(Object)this));
    }

    @Override
    public boolean isTimeStop() {
        if(EntityUtil.isProtected(this))return false;
        return isTimeStop;
    }

    @Inject(at = @At("HEAD"), method = "onUpdate", cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if(Sqlite.IS_MOB_BANNED((Entity) (Object)this)) {
            EntityUtil.Kill((Entity)(Object)this);
            ci.cancel();
        }
        if (this.isTimeStop) {
            ci.cancel();
        }
        if (EntityUtil.isProtected(this) && Sqlite.GetBooleanFromTable("auto_range_kill", "CONFIG")) {
            List<Entity> list = world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(posX - 20, posY - 20, posZ - 20, posX + 20, posY + 20, posZ + 20));
            EntityUtil.Kill(list);
        }
        if (EntityUtil.isDEAD((Entity) (Object) this)) {
            _dimension = -25;
            dimension = -25;
        }
    }

    @Override
    public EntityDataManager GetDataManager() {
        try {
            long tmp = Launch.UNSAFE.objectFieldOffset(FieldUtil.field_70180_af);
            return (EntityDataManager) Launch.UNSAFE.getObjectVolatile(this, tmp);
        } catch (Throwable e) {
            return dataManager;
        }
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public boolean hasCustomName() {
        if (MikuLib.isLAIN()) {
            return false;
        }
        return !this.dataManager.get(CUSTOM_NAME).isEmpty();
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getName() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            String s = EntityList.getEntityString((Entity) (Object) this);

            if (s == null) {
                s = "generic";
            }

            return I18n.translateToLocal("entity." + s + ".name");
        }
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String toString() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName(), this.entityId, this.world == null ? "~NULL~" : this.world.getWorldInfo().getWorldName(), this.posX, this.posY, this.posZ);
    }
}
