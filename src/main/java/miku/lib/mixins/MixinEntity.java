package miku.lib.mixins;

import miku.lib.api.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.UUID;

@Mixin(value = Entity.class)
public abstract class MixinEntity implements iEntity {


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

    @Final
    @Shadow
    protected static final DataParameter<Byte> FLAGS = EntityDataManager.createKey(Entity.class, DataSerializers.BYTE);

    protected boolean isTimeStop;

    @Override
    public void kill() {
        isInWeb = true;
        fire = Integer.MAX_VALUE;
        isDead=true;
        byte b0 = this.dataManager.get(FLAGS);
        this.dataManager.set(FLAGS, (byte) (b0 | 1 << 5));
        this.dataManager.set(FLAGS, (byte) (b0 & ~(1)));
        isAddedToWorld=false;
        if(((Entity)(Object)this) instanceof EntityLivingBase){
            ((iEntityLivingBase)this).Kill();
            if(((Entity)(Object)this) instanceof EntityLiving){
                ((iEntityLiving)this).Kill();
            }
            if(((Entity)(Object)this) instanceof EntityPlayer){
                ((iEntityPlayer)this).Kill();
                world.playerEntities.remove(this);
                if(((Entity)(Object)this) instanceof EntityPlayerMP){
                    EntityPlayerMP playerMP = ((EntityPlayerMP)(Object)this);
                    playerMP.connection.disconnect(new TextComponentString("Goodbye!"));
                }
            }
        }
        ((iWorld)world).remove(((Entity)(Object)this));
    }

    @Override
    public boolean isTimeStop() {
        return isTimeStop;
    }

    @Override
    public void SetTimeStop() {
        isTimeStop=true;
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
        _boundingBox=boundingBox;
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
    }

    @Override
    public void TimeStop() {
        entityId=_entityId;
        ridingEntity=_ridingEntity;
        forceSpawn=_forceSpawn;
        rideCooldown=_rideCooldown;
        preventEntitySpawning=_preventEntitySpawning;
        world=_world;
        posX=_posX;
        posY=_posY;
        posZ=_posZ;
        prevPosX=_prevPosX;
        prevPosY=_prevPosY;
        prevPosZ=_prevPosZ;
        prevRotationPitch=_prevRotationPitch;
        prevRotationYaw=_prevRotationYaw;
        boundingBox=_boundingBox;
        rotationYaw=_rotationYaw;
        rotationPitch=_rotationPitch;
        onGround=_onGround;
        collided=_collided;
        collidedHorizontally=_collidedHorizontally;
        collidedVertically=_collidedVertically;
        isDead=_isDead;
        isInWeb=_isInWeb;
        isOutsideBorder=_isOutsideBorder;
        velocityChanged=_velocityChanged;
        width=_width;
        height=_height;
        prevDistanceWalkedModified=_prevDistanceWalkedModified;
        distanceWalkedModified=_distanceWalkedModified;
        distanceWalkedOnStepModified=_distanceWalkedOnStepModified;
        fallDistance=_fallDistance;
        nextStepDistance=_nextStepDistance;
        nextFlap=_nextFlap;
        lastTickPosX=_lastTickPosX;
        lastTickPosZ=_lastTickPosY;
        lastTickPosY=_lastTickPosZ;
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
    }

    @Inject(at = @At("HEAD"), method = "onUpdate", cancellable = true)
    public void onUpdate(CallbackInfo ci) {
        if (this.isTimeStop) {
            TimeStop();
            ci.cancel();
        }
    }

    @Override
    public EntityDataManager GetDataManager(){
        return dataManager;
    }
}
