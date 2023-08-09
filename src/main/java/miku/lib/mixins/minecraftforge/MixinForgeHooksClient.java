package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.model.ModelDynBucket;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.Animation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;
import java.io.File;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.BOSSINFO;

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

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public static void onTextureStitchedPre(TextureMap map) {
        MikuLib.MikuEventBus().post(new TextureStitchEvent.Pre(map));
        ModelLoader.White.INSTANCE.register(map);
        ModelDynBucket.LoaderDynBucket.INSTANCE.register(map);
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onTextureStitchedPost(TextureMap map) {
        MikuLib.MikuEventBus().post(new TextureStitchEvent.Post(map));
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onBlockColorsInit(BlockColors blockColors) {
        MikuLib.MikuEventBus().post(new ColorHandlerEvent.Block(blockColors));
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onItemColorsInit(ItemColors itemColors, BlockColors blockColors) {
        MikuLib.MikuEventBus().post(new ColorHandlerEvent.Item(itemColors, blockColors));
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static ISound playSound(SoundManager manager, ISound sound) {
        PlaySoundEvent e = new PlaySoundEvent(manager, sound);
        MikuLib.MikuEventBus().post(e);
        return e.getResultSound();
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static float getFogDensity(EntityRenderer renderer, Entity entity, IBlockState state, float partial, float density) {
        EntityViewRenderEvent.FogDensity event = new EntityViewRenderEvent.FogDensity(renderer, entity, state, partial, density);
        if (MikuLib.MikuEventBus().post(event)) return event.getDensity();
        return -1;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onFogRender(EntityRenderer renderer, Entity entity, IBlockState state, float partial, int mode, float distance) {
        MikuLib.MikuEventBus().post(new EntityViewRenderEvent.RenderFogEvent(renderer, entity, state, partial, mode, distance));
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void onModelBake(ModelManager modelManager, IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, ModelLoader modelLoader) {
        MikuLib.MikuEventBus().post(new ModelBakeEvent(modelManager, modelRegistry, modelLoader));
        modelLoader.onPostBakeEvent(modelRegistry);
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static RenderGameOverlayEvent.BossInfo bossBarRenderPre(ScaledResolution res, BossInfoClient bossInfo, int x, int y, int increment) {
        RenderGameOverlayEvent.BossInfo evt = new RenderGameOverlayEvent.BossInfo(new RenderGameOverlayEvent(Animation.getPartialTickTime(), res),
                BOSSINFO, bossInfo, x, y, increment);
        MikuLib.MikuEventBus().post(evt);
        return evt;
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static void bossBarRenderPost(ScaledResolution res) {
        MikuLib.MikuEventBus().post(new RenderGameOverlayEvent.Post(new RenderGameOverlayEvent(Animation.getPartialTickTime(), res), BOSSINFO));
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public static ScreenshotEvent onScreenshot(BufferedImage image, File screenshotFile) {
        ScreenshotEvent event = new ScreenshotEvent(image, screenshotFile);
        MikuLib.MikuEventBus().post(event);
        return event;
    }
}
