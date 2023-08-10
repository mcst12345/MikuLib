package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.core.MikuLib;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FMLCommonHandler.class, remap = false)
public abstract class MixinFMLCommonHandler {
    @Shadow
    private EventBus eventBus;

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    @Deprecated
    public EventBus bus() {
        return MikuLib.MikuEventBus();
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci) {
        this.eventBus = MikuLib.MikuEventBus();
    }

    /**
     * @author mcst12345
     * @reason Holy Fuck
     */
    @Overwrite
    public void onPreClientTick() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ClientTickEvent StartPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onPostClientTick() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ClientTickEvent EndPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onPostServerTick() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ServerTickEvent EndPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onPostWorldTick(World world) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.END, world));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at WorldTickEvent ServerSide EndPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onPreServerTick() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ServerTickEvent StartPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onPreWorldTick(World world) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.WorldTickEvent(Side.SERVER, TickEvent.Phase.START, world));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at WorldTickEvent ServerSide StartPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onRenderTickStart(float timer) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        Animation.setClientPartialTickTime(timer);
        try {
            bus().post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, timer));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at RenderTickEvent StartPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onRenderTickEnd(float timer) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, timer));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at RenderTickEvent EndPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onPlayerPreTick(EntityPlayer player) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerTickEvent StartPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void onPlayerPostTick(EntityPlayer player) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerTickEvent EndPhase");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason No You Cannot Exit
     */
    @Overwrite
    public void exitJava(int exitCode, boolean hardExit) {
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void fireMouseInput() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new InputEvent.MouseInputEvent());
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at MouseInputEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void fireKeyInput() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new InputEvent.KeyInputEvent());
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at KeyInputEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerChangedDimensionEvent(EntityPlayer player, int fromDim, int toDim) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.PlayerChangedDimensionEvent(player, fromDim, toDim));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerChangedDimensionEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerLoggedIn(EntityPlayer player) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.PlayerLoggedInEvent(player));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerLoggedInEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerLoggedOut(EntityPlayer player) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.PlayerLoggedOutEvent(player));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerLoggedOutEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerRespawnEvent(EntityPlayer player, boolean endConquered) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.PlayerRespawnEvent(player, endConquered));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at PlayerRespawnEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerItemPickupEvent(EntityPlayer player, EntityItem item, ItemStack clone) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.ItemPickupEvent(player, item, clone));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ItemPickupEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerCraftingEvent(EntityPlayer player, ItemStack crafted, IInventory craftMatrix) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.ItemCraftedEvent(player, crafted, craftMatrix));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ItemCraftedEvent");
            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void firePlayerSmeltedEvent(EntityPlayer player, ItemStack smelted) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        try {
            bus().post(new PlayerEvent.ItemSmeltedEvent(player, smelted));
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at ItemSmeltedEvent");
            t.printStackTrace();
        }
    }
}
