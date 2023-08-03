package miku.lib.mixins.minecraft;

import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemRenderer.class)
public class MixinItemRenderer {
}
