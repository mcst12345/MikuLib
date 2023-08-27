package miku.lib.mixins.minecraftforge;

import miku.lib.common.core.MikuLib;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(value = UniversalBucket.class, remap = false)
public abstract class MixinUniversalBucket extends Item {
    @Shadow
    @Nullable
    public abstract FluidStack getFluid(@NotNull ItemStack container);

    @Shadow
    @Nonnull
    public abstract ItemStack getEmpty();

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        FluidStack fluidStack = getFluid(stack);
        if (fluidStack == null) {
            if (!getEmpty().isEmpty()) {
                return getEmpty().getDisplayName();
            }
            return super.getItemStackDisplayName(stack);
        }

        String unloc = this.getUnlocalizedNameInefficiently(stack);

        if (I18n.canTranslate(unloc + "." + fluidStack.getFluid().getName())) {
            return I18n.translateToLocal(unloc + "." + fluidStack.getFluid().getName());
        }

        return I18n.translateToLocalFormatted(unloc + ".name", fluidStack.getLocalizedName());
    }
}
