package miku.lib.mixins.minecraft;

import miku.lib.common.api.iChunk;
import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = Chunk.class)
public abstract class MixinChunk implements iChunk {
    @Final
    @Shadow
    private final ClassInheritanceMultiMap[] entityLists = new ClassInheritanceMultiMap[16];

    @Shadow
    public abstract void markDirty();


    @Shadow
    private boolean hasEntities;

    @Shadow
    @Final
    public int x;

    @Shadow
    @Final
    public int z;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private boolean loaded;

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private Map<BlockPos, TileEntity> tileEntities;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void removeEntity(Entity entityIn) {
        if (EntityUtil.isProtected(entityIn)) return;
        this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void removeEntityAtIndex(Entity entityIn, int index)
    {
        if(EntityUtil.isProtected(entityIn))return;
        if (index < 0)
        {
            index = 0;
        }

        if (index >= this.entityLists.length)
        {
            index = this.entityLists.length - 1;
        }

        this.entityLists[index].remove(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after entity removals
    }

    @Override
    public void remove(Entity entity) {
        if (entity.chunkCoordY < 0) {
            entity.chunkCoordY = 0;
        }

        if (entity.chunkCoordY >= entityLists.length) {
            entity.chunkCoordY = entityLists.length - 1;
        }

        entityLists[entity.chunkCoordY].remove(entity);
        ((Chunk) (Object) this).markDirty(); // Forge - ensure chunks are marked to save after entity removals
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void addEntity(Entity entityIn) {
        if (EntityUtil.isDEAD(entityIn)) return;
        this.hasEntities = true;
        int i = MathHelper.floor(entityIn.posX / 16.0D);
        int j = MathHelper.floor(entityIn.posZ / 16.0D);

        if (i != this.x || j != this.z) {
            LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.x, this.z, entityIn);
            entityIn.setDead();
        }

        int k = MathHelper.floor(entityIn.posY / 16.0D);

        if (k < 0) {
            k = 0;
        }

        if (k >= this.entityLists.length) {
            k = this.entityLists.length - 1;
        }

        MikuLib.MikuEventBus.post(new net.minecraftforge.event.entity.EntityEvent.EnteringChunk(entityIn, this.x, this.z, entityIn.chunkCoordX, entityIn.chunkCoordZ));
        entityIn.addedToChunk = true;
        entityIn.chunkCoordX = this.x;
        entityIn.chunkCoordY = k;
        entityIn.chunkCoordZ = this.z;
        this.entityLists[k].add(entityIn);
        this.markDirty(); // Forge - ensure chunks are marked to save after an entity add
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onLoad() {
        this.loaded = true;
        this.world.addTileEntities(this.tileEntities.values());

        for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : this.entityLists) {
            this.world.loadEntities(com.google.common.collect.ImmutableList.copyOf(classinheritancemultimap));
        }
        MikuLib.MikuEventBus.post(new net.minecraftforge.event.world.ChunkEvent.Load((Chunk) (Object) this));
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onUnload() {
        java.util.Arrays.stream(entityLists).forEach(multimap -> com.google.common.collect.Lists.newArrayList(multimap.getByClass(net.minecraft.entity.player.EntityPlayer.class)).forEach(player -> world.updateEntityWithOptionalForce((Entity) player, false))); // FORGE - Fix for MC-92916
        this.loaded = false;

        for (TileEntity tileentity : this.tileEntities.values()) {
            this.world.markTileEntityForRemoval(tileentity);
        }

        for (ClassInheritanceMultiMap<Entity> classinheritancemultimap : this.entityLists) {
            this.world.unloadEntities(classinheritancemultimap);
        }
        MikuLib.MikuEventBus.post(new net.minecraftforge.event.world.ChunkEvent.Unload((Chunk) (Object) this));
    }
}
