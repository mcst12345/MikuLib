package miku.lib.mixins.minecraft;

import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiIngameForge.class, remap = false)
public class MixinGuiIngameForge {
    @Shadow
    private RenderGameOverlayEvent eventParent;

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    private boolean pre(RenderGameOverlayEvent.ElementType type) {
        boolean flag = false;
        try {
            flag = MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
        } catch (Throwable ignored) {
        }
        return flag;
    }
}
