package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = World.class)
public abstract class ClientMixinWorld {
    @Shadow
    public abstract Chunk getChunk(int chunkX, int chunkZ);

    @Shadow
    @Final
    public List<Entity> loadedEntityList;

    /**
     * @author mcst12345
     * @reason Shit Fuck
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void joinEntityInSurroundings(Entity entityIn) {
        if (EntityUtil.isDEAD(entityIn)) return;
        int j2 = MathHelper.floor(entityIn.posX / 16.0D);
        int k2 = MathHelper.floor(entityIn.posZ / 16.0D);

        for (int i3 = -2; i3 <= 2; ++i3) {
            for (int j3 = -2; j3 <= 2; ++j3) {
                this.getChunk(j2 + i3, k2 + j3);
            }
        }

        if (!this.loadedEntityList.contains(entityIn)) {
            if (!MikuLib.MikuEventBus.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (World) (Object) this)))
                this.loadedEntityList.add(entityIn);
        }
    }
}
