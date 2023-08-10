package miku.lib.mixins.minecraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CreativeTabs.class)
public abstract class MixinCreativeTabs {
    @Shadow
    private ItemStack icon;

    @Shadow
    public abstract ItemStack createIcon();

    /**
     * @author mcst12345
     * @reason Fuck!!
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public ItemStack getIcon() {
        if (this.icon.isEmpty()) {
            try {
                this.icon = this.createIcon();
            } catch (Throwable ignored) {
                return ItemStack.EMPTY;
            }
        }

        return this.icon;
    }
}
