package miku.lib.mixins.minecraftforge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ForgeHooksClient.class)
public class MixinForgeHooksClient {
    /**
     * @author mcst12345
     * @reason The FUCK
     */
    @Overwrite
    public static float getOffsetFOV(EntityPlayer entity, float fov) {
        FOVUpdateEvent fovUpdateEvent = new FOVUpdateEvent(entity, fov);
        try {
            MinecraftForge.EVENT_BUS.post(fovUpdateEvent);
        } catch (Throwable ignored) {

        }
        return fovUpdateEvent.getNewfov();
    }
}
