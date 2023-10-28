package miku.lib.mixins.minecraft;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.DebugRendererNeighborsUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mixin(value = DebugRendererNeighborsUpdate.class)
public class MixinDebugRendererNeighborsUpdate {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Map<Long, Map<BlockPos, Integer>> lastUpdate;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void render(float partialTicks, long finishTimeNano) {
        long i = ((iMinecraft) this.minecraft).MikuWorld().getTotalWorldTime();
        EntityPlayer entityplayer = ((iMinecraft) this.minecraft).MikuPlayer();
        double d0 = entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double) partialTicks;
        double d1 = entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double) partialTicks;
        double d2 = entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double) partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        Set<BlockPos> set = Sets.newHashSet();
        Map<BlockPos, Integer> map = Maps.newHashMap();
        Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> iterator = this.lastUpdate.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Long, Map<BlockPos, Integer>> entry = iterator.next();
            Long olong = entry.getKey();
            Map<BlockPos, Integer> map1 = entry.getValue();
            long k = i - olong;

            if (k > 200L) {
                iterator.remove();
            } else {
                for (Map.Entry<BlockPos, Integer> entry1 : map1.entrySet()) {
                    BlockPos blockpos = entry1.getKey();
                    Integer integer = entry1.getValue();

                    if (set.add(blockpos)) {
                        RenderGlobal.drawSelectionBoundingBox((new AxisAlignedBB(BlockPos.ORIGIN)).grow(0.002D).shrink(0.0025D * (double) k).offset(blockpos.getX(), blockpos.getY(), blockpos.getZ()).offset(-d0, -d1, -d2), 1.0F, 1.0F, 1.0F, 1.0F);
                        map.put(blockpos, integer);
                    }
                }
            }
        }

        for (Map.Entry<BlockPos, Integer> entry2 : map.entrySet()) {
            BlockPos blockpos1 = entry2.getKey();
            Integer integer1 = entry2.getValue();
            DebugRenderer.renderDebugText(String.valueOf(integer1), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), partialTicks, -1);
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
