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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mixin(value = Entity.class)
public abstract class MixinEntity implements iEntity {

    protected boolean DEAD;

    public boolean isDEAD(){
        return DEAD;
    }

    @Shadow
    private int entityId;
    protected int _entityId;
    @Shadow
    public boolean preventEntitySpawning;
    protected boolean _preventEntitySpawning;
    @Shadow
    protected int rideCooldown;
    protected int _rideCooldown;
    @Shadow
    private Entity ridingEntity;
    protected Entity _ridingEntity;
    @Shadow
    public boolean forceSpawn;
    protected boolean _forceSpawn;
    @Shadow
    public World world;
    protected World _world;
    @Shadow
    public double prevPosX;
    protected double _prevPosX;
    @Shadow
    public double prevPosY;
    protected double _prevPosY;
    @Shadow
    public double prevPosZ;
    protected double _prevPosZ;
    @Shadow
    public double posX;
    protected double _posX;
    @Shadow
    public double posY;
    protected double _posY;
    @Shadow
    public double posZ;
    protected double _posZ;
    @Shadow
    public double motionX;
    @Shadow
    public double motionY;
    @Shadow
    public double motionZ;
    @Shadow
    public float rotationYaw;
    protected float _rotationYaw;
    @Shadow
    public float rotationPitch;
    protected float _rotationPitch;
    @Shadow
    public float prevRotationYaw;
    protected float _prevRotationYaw;
    @Shadow
    public float prevRotationPitch;
    protected float _prevRotationPitch;
    @Shadow
    private AxisAlignedBB boundingBox;
    protected AxisAlignedBB _boundingBox;
    @Shadow
    public boolean onGround;
    protected boolean _onGround;
    @Shadow
    public boolean collidedHorizontally;
    protected boolean _collidedHorizontally;
    @Shadow
    public boolean collidedVertically;
    protected boolean _collidedVertically;
    @Shadow
    public boolean collided;
    protected boolean _collided;
    @Shadow
    public boolean velocityChanged;
    protected boolean _velocityChanged;
    @Shadow
    protected boolean isInWeb;
    protected boolean _isInWeb;
    @Shadow
    private boolean isOutsideBorder;
    protected boolean _isOutsideBorder;
    @Shadow
    public boolean isDead;
    protected boolean _isDead;
    @Shadow
    public float width;
    protected float _width;
    @Shadow
    public float height;
    protected float _height;
    @Shadow
    public float prevDistanceWalkedModified;
    protected float _prevDistanceWalkedModified;
    @Shadow
    public float distanceWalkedModified;
    protected float _distanceWalkedModified;
    @Shadow
    public float distanceWalkedOnStepModified;
    protected float _distanceWalkedOnStepModified;
    @Shadow
    public float fallDistance;
    protected float _fallDistance;
    @Shadow
    private int nextStepDistance;
    protected int _nextStepDistance;
    @Shadow
    private float nextFlap;
    protected float _nextFlap;
    @Shadow
    public double lastTickPosX;
    protected double _lastTickPosX;
    @Shadow
    public double lastTickPosY;
    protected double _lastTickPosY;
    @Shadow
    public double lastTickPosZ;
    protected double _lastTickPosZ;
    @Shadow
    public float stepHeight;
    protected float _stepHeight;
    @Shadow
    public boolean noClip;
    protected boolean _noClip;
    @Shadow
    public float entityCollisionReduction;
    protected float _entityCollisionReduction;
    @Shadow
    protected Random rand;
    protected Random _rand;
    @Shadow
    public int ticksExisted;
    protected int _ticksExisted;
    @Shadow
    private int fire;
    protected int _fire;
    @Shadow
    protected boolean inWater;
    protected boolean _inWater;
    @Shadow
    public int hurtResistantTime;
    protected int _hurtResistantTime;
    @Shadow
    protected boolean firstUpdate;
    protected boolean _firstUpdate;
    @Shadow
    protected boolean isImmuneToFire;
    protected boolean _isImmuneToFire;
    @Shadow
    protected EntityDataManager dataManager;
    protected EntityDataManager _dataManager;
    @Shadow
    public boolean addedToChunk;
    protected boolean _addedToChunk;
    @Shadow
    public int chunkCoordX;
    protected int _chunkCoordX;
    @Shadow
    public int chunkCoordY;
    protected int _chunkCoordY;
    @Shadow
    public int chunkCoordZ;
    protected int _chunkCoordZ;
    @Shadow
    public boolean ignoreFrustumCheck;
    protected boolean _ignoreFrustumCheck;
    @Shadow
    public boolean isAirBorne;
    protected boolean _isAirBorne;
    @Shadow
    public int timeUntilPortal;
    protected int _timeUntilPortal;
    @Shadow
    protected boolean inPortal;
    protected boolean _inPortal;
    @Shadow
    protected int portalCounter;
    protected int _portalCounter;
    @Shadow
    public int dimension;
    protected int _dimension;
    @Shadow
    protected BlockPos lastPortalPos;
    protected BlockPos _lastPortalPos;
    @Shadow
    protected Vec3d lastPortalVec;
    protected Vec3d _lastPortalVec;
    @Shadow
    protected EnumFacing teleportDirection;
    protected EnumFacing _teleportDirection;
    @Shadow
    private boolean invulnerable;
    protected boolean _invulnerable;
    @Shadow
    protected UUID entityUniqueID;
    protected UUID _entityUniqueID;
    @Shadow
    protected String cachedUniqueIdString;
    protected String _cachedUniqueIdString;
    @Shadow
    protected boolean glowing;
    protected boolean _glowing;
    @Shadow
    private boolean isPositionDirty;
    protected boolean _isPositionDirty;
    @Shadow
    private long pistonDeltasGameTime;
    protected long _pistonDeltasGameTime;
    @Shadow(remap = false)
    private boolean isAddedToWorld;
    protected boolean _isAddedToWorld;

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

    @Override
    public void SetTimeStop() {
        _entityId=entityId;
        _ridingEntity=ridingEntity;
        _forceSpawn=forceSpawn;
        _rideCooldown=rideCooldown;
        _preventEntitySpawning=preventEntitySpawning;
        _world=world;
        _posX=posX;
        _posY=posY;
        _posZ=posZ;
        _prevPosX=prevPosX;
        _prevPosY=prevPosY;
        _prevPosZ=prevPosZ;
        _prevRotationPitch=prevRotationPitch;
        _prevRotationYaw=prevRotationYaw;
        _rotationYaw=rotationYaw;
        _rotationPitch=rotationPitch;
        _onGround=onGround;
        _collided=collided;
        _collidedHorizontally=collidedHorizontally;
        _collidedVertically=collidedVertically;
        _isDead=isDead;
        _isInWeb=isInWeb;
        _isOutsideBorder=isOutsideBorder;
        _velocityChanged=velocityChanged;
        _width=width;
        _height=height;
        _prevDistanceWalkedModified=prevDistanceWalkedModified;
        _distanceWalkedModified=distanceWalkedModified;
        _distanceWalkedOnStepModified=distanceWalkedOnStepModified;
        _fallDistance=fallDistance;
        _nextStepDistance=nextStepDistance;
        _nextFlap=nextFlap;
        _lastTickPosX=lastTickPosX;
        _lastTickPosZ=lastTickPosY;
        _lastTickPosY=lastTickPosZ;
        _stepHeight=stepHeight;
        _noClip=noClip;
        _entityCollisionReduction=entityCollisionReduction;
        _rand=rand;
        _ticksExisted=ticksExisted;
        _fire=fire;
        _inWater=inWater;
        _hurtResistantTime=hurtResistantTime;
        _isImmuneToFire=isImmuneToFire;
        _firstUpdate=firstUpdate;
        _dataManager=dataManager;
        _addedToChunk=addedToChunk;
        _chunkCoordX=chunkCoordX;
        _chunkCoordY=chunkCoordY;
        _chunkCoordZ=chunkCoordZ;
        _ignoreFrustumCheck=ignoreFrustumCheck;
        _isAirBorne=isAirBorne;
        _timeUntilPortal=timeUntilPortal;
        _inPortal=inPortal;
        _portalCounter=portalCounter;
        _dimension=dimension;
        _lastPortalPos=lastPortalPos;
        _lastPortalVec=lastPortalVec;
        _teleportDirection=teleportDirection;
        _invulnerable=invulnerable;
        _entityUniqueID=entityUniqueID;
        _cachedUniqueIdString=cachedUniqueIdString;
        _glowing=glowing;
        _isPositionDirty=isPositionDirty;
        _pistonDeltasGameTime=pistonDeltasGameTime;
        _isAddedToWorld=isAddedToWorld;
        _boundingBox=boundingBox;
        isTimeStop=!isTimeStop;
    }

    @Override
    public void TimeStop() {
        entityId=_entityId;
        ridingEntity=_ridingEntity;
        forceSpawn=_forceSpawn;
        rideCooldown=_rideCooldown;
        preventEntitySpawning=_preventEntitySpawning;
        world=_world;
        if(ticksExisted>0){
            posX = lastTickPosX;
            posY = lastTickPosY;
            posZ = lastTickPosZ;
        }
        prevPosX=_prevPosX;
        prevPosY=_prevPosY;
        prevPosZ=_prevPosZ;
        rotationYaw=prevRotationYaw;
        rotationPitch=prevRotationPitch;
        onGround=_onGround;
        boundingBox=_boundingBox;
        collided=_collided;
        collidedHorizontally=_collidedHorizontally;
        collidedVertically=_collidedVertically;
        isDead=_isDead;
        isInWeb=_isInWeb;
        isOutsideBorder=_isOutsideBorder;
        velocityChanged=_velocityChanged;
        width=_width;
        height=_height;
        distanceWalkedModified=prevDistanceWalkedModified;
        distanceWalkedOnStepModified=_distanceWalkedOnStepModified;
        fallDistance=_fallDistance;
        nextStepDistance=_nextStepDistance;
        nextFlap=_nextFlap;
        stepHeight=_stepHeight;
        noClip=_noClip;
        entityCollisionReduction=_entityCollisionReduction;
        rand=_rand;
        ticksExisted=_ticksExisted;
        fire=_fire;
        inWater=_inWater;
        hurtResistantTime=_hurtResistantTime;
        isImmuneToFire=_isImmuneToFire;
        firstUpdate=_firstUpdate;
        dataManager=_dataManager;
        addedToChunk=_addedToChunk;
        chunkCoordX=_chunkCoordX;
        chunkCoordY=_chunkCoordY;
        chunkCoordZ=_chunkCoordZ;
        ignoreFrustumCheck=_ignoreFrustumCheck;
        isAirBorne=_isAirBorne;
        timeUntilPortal=_timeUntilPortal;
        inPortal=_inPortal;
        portalCounter=_portalCounter;
        dimension=_dimension;
        lastPortalPos=_lastPortalPos;
        lastPortalVec=_lastPortalVec;
        teleportDirection=_teleportDirection;
        invulnerable=_invulnerable;
        entityUniqueID=_entityUniqueID;
        cachedUniqueIdString=_cachedUniqueIdString;
        glowing=_glowing;
        isPositionDirty=_isPositionDirty;
        pistonDeltasGameTime=_pistonDeltasGameTime;
        isAddedToWorld=_isAddedToWorld;
        motionX=0;
        motionY=0;
        motionZ=0;

        if(((Entity)(Object)this) instanceof EntityLivingBase){
            ((iEntityLivingBase)this).TimeStop();
        }
    }

    @Inject(at = @At("HEAD"), method = "onUpdate", cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if(Sqlite.IS_MOB_BANNED((Entity) (Object)this)) {
            EntityUtil.Kill((Entity)(Object)this);
            ci.cancel();
        }
        if (this.isTimeStop) {
            TimeStop();
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
            Field field = Entity.class.getDeclaredField("field_70180_af");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            return (EntityDataManager) Launch.UNSAFE.getObjectVolatile(this, tmp);
        } catch (NoSuchFieldException e) {
            return dataManager;
        }
    }

    @Inject(at = @At("HEAD"), method = "<clinit>")
    private static void Entity(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
    }
}
