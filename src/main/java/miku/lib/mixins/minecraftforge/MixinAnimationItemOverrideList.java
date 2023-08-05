package miku.lib.mixins.minecraftforge;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelStateComposition;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.AnimationItemOverrideList;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

@Mixin(value = AnimationItemOverrideList.class, remap = false)
public abstract class MixinAnimationItemOverrideList extends ItemOverrideList {

    @Shadow
    @Final
    private IModel model;

    @Shadow
    @Final
    private IModelState state;

    @Shadow
    @Final
    private VertexFormat format;

    @Shadow
    @Final
    private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

    public MixinAnimationItemOverrideList(List<ItemOverride> overridesIn) {
        super(overridesIn);
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    @Override
    @Nonnull
    public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
        IAnimationStateMachine asm = stack.getCapability(CapabilityAnimation.ANIMATION_CAPABILITY, null);
        if (asm != null) {
            // TODO: caching?
            if (world == null && entity != null) {
                world = entity.world;
            }
            if (world == null) {
                world = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
            }
            IModelState state = asm.apply(Animation.getWorldTime(world, Animation.getPartialTickTime())).getLeft();
            return model.bake(new ModelStateComposition(state, this.state), format, bakedTextureGetter);
        }
        return super.handleItemState(originalModel, stack, world, entity);
    }
}
