package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class MixinForgeHooksClient {
    /**
     * @author mcst12345
     * @reason The FUCK
     */
    @Overwrite
    public static float getOffsetFOV(EntityPlayer entity, float fov) {
        FOVUpdateEvent fovUpdateEvent = new FOVUpdateEvent(entity, fov);
        try {
            MikuLib.MikuEventBus().post(fovUpdateEvent);
        } catch (Throwable ignored) {

        }
        return fovUpdateEvent.getNewfov();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static boolean postMouseEvent() {
        if (MikuInsaneMode.isMikuInsaneMode()) return false;
        boolean result;
        try {
            result = MikuLib.MikuEventBus().post(new MouseEvent());
        } catch (Throwable t) {
            result = false;
            System.out.println("MikuWarn:Catch exception at MouseEvent");
            t.printStackTrace();
        }
        return result;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static boolean onDrawBlockHighlight(RenderGlobal context, EntityPlayer player, RayTraceResult target, int subID, float partialTicks) {
        boolean result;
        try {
            result = MikuLib.MikuEventBus().post(new DrawBlockHighlightEvent(context, player, target, subID, partialTicks));
        } catch (Throwable t) {
            result = false;
            System.out.println("MikuWarn:Catch exception at DrawBlockHighlightEvent");
            t.printStackTrace();
        }
        return result;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void dispatchRenderLast(RenderGlobal context, float partialTicks) {
        try {
            MikuLib.MikuEventBus().post(new RenderWorldLastEvent(context, partialTicks));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at RenderWorldLastEvent");
            t.printStackTrace();
        }

    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static boolean renderFirstPersonHand(RenderGlobal context, float partialTicks, int renderPass) {
        boolean result;
        try {
            result = MikuLib.MikuEventBus().post(new RenderHandEvent(context, partialTicks, renderPass));
        } catch (Throwable t) {
            result = false;
            System.out.println("MikuWarn:Catch exception at RenderHandEvent");
            t.printStackTrace();
        }
        return result;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static boolean renderSpecificFirstPersonHand(EnumHand hand, float partialTicks, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        boolean result;
        try {
            result = MikuLib.MikuEventBus().post(new RenderSpecificHandEvent(hand, partialTicks, interpPitch, swingProgress, equipProgress, stack));
        } catch (Throwable t) {
            result = false;
            System.out.println("MikuWarn:Catch exception at RenderSpecificHandEvent");
            t.printStackTrace();
        }
        return result;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static float getFOVModifier(EntityRenderer renderer, Entity entity, IBlockState state, double renderPartialTicks, float fov) {
        EntityViewRenderEvent.FOVModifier event = new EntityViewRenderEvent.FOVModifier(renderer, entity, state, renderPartialTicks, fov);
        try {
            MikuLib.MikuEventBus().post(event);
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at FOVModifier");
            t.printStackTrace();
        }
        return event.getFOV();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onInputUpdate(EntityPlayer player, MovementInput movementInput) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            MikuLib.MikuEventBus().post(new InputUpdateEvent(player, movementInput));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at InputUpdateEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public static void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks) {
        boolean flag = false;
        try {
            flag = MikuLib.MikuEventBus().post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at GuiScreenEvent.DrawScreenEvent.Pre");
            t.printStackTrace();
        }
        if (!flag)
            screen.drawScreen(mouseX, mouseY, partialTicks);
        try {
            MikuLib.MikuEventBus().post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at GuiScreenEvent.DrawScreenEvent.Post");
            t.printStackTrace();
        }
    }
}
