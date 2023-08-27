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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(value = Entity.class)
public abstract class MixinEntity implements iEntity {
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

    @Inject(at=@At("HEAD"),method = "changeDimension(I)Lnet/minecraft/entity/Entity;", cancellable = true)
    public void changeDimension(int dimensionIn, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isDEAD((Entity)(Object)this))cir.setReturnValue(null);
    }

    @Inject(at=@At("HEAD"),method = "changeDimension(ILnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/entity/Entity;", cancellable = true,remap = false)
    public void changeDimension(int dimensionIn, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir){
        if(EntityUtil.isDEAD((Entity)(Object)this))cir.setReturnValue(null);
    }

    @Final
    @Shadow
    protected static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(Entity.class, DataSerializers.BYTE);

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
                    if ((boolean) Sqlite.GetValueFromTable("miku_kill_kick_attack", "CONFIG", 0)) {
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
        if(EntityUtil.isProtected(this) && ((boolean)Sqlite.GetValueFromTable("auto_range_kill","CONFIG",0))){
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

    @Inject(at = @At("HEAD"), method = "<clinit>")
    private static void Entity(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
    }
}
