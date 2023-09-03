package miku.lib.client.event;

import miku.lib.common.api.iEntityPlayer;
import miku.lib.common.util.EntityUtil;
import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KeyBoardEvent {
    public static final KeyBoardEvent INSTANCE = new KeyBoardEvent();
    @SideOnly(Side.CLIENT)//key to stop the time
    public static final KeyBinding TIME_STOP = new KeyBinding("key.miku.time_stop", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_L, "key.category.miku");

    @SideOnly(Side.CLIENT)//key to kill all entities
    public static final KeyBinding KILL_ALL = new KeyBinding("key.miku.kill_all", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_K, "key.category.miku");

    @SideOnly(Side.CLIENT)//key to change your game mode
    public static final KeyBinding GAME_MODE = new KeyBinding("key.miku.game_mode", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_M, "key.category.miku");
    @SideOnly(Side.CLIENT)//key to change SpecialItem's mode
    public static final KeyBinding MIKU_MODE = new KeyBinding("key.miku.miku_mode", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_U, "key.category.miku");

    @SideOnly(Side.CLIENT)//key to record a time point
    public static final KeyBinding RECORD_TIME_POINT = new KeyBinding("key.miku.rtp", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_R, "key.category.miku");
    @SideOnly(Side.CLIENT)
    public static final KeyBinding BACK_TO_TIME_POINT = new KeyBinding("key.miku.bttp", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_B, "key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding SWITCH_TIME_POINT = new KeyBinding("key.miku.stp", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_B, "key.category.miku");

    @SideOnly(Side.CLIENT)
    public static void Init() {
        ClientRegistry.registerKeyBinding(TIME_STOP);
        ClientRegistry.registerKeyBinding(KILL_ALL);
        ClientRegistry.registerKeyBinding(GAME_MODE);
        ClientRegistry.registerKeyBinding(MIKU_MODE);
        ClientRegistry.registerKeyBinding(RECORD_TIME_POINT);
        ClientRegistry.registerKeyBinding(BACK_TO_TIME_POINT);
        ClientRegistry.registerKeyBinding(SWITCH_TIME_POINT);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (TIME_STOP.isPressed()) {
            if (EntityUtil.isProtected(Minecraft.getMinecraft().player)) {
                NetworkHandler.INSTANCE.sendMessageToServer(new TimeStop(Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.getEntityId()));
            }
        }
        if (KILL_ALL.isPressed()) {
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player)){
                NetworkHandler.INSTANCE.sendMessageToServer(new KillAllEntities(Minecraft.getMinecraft().player.dimension,Minecraft.getMinecraft().player.getEntityId()));
                List<Entity> entities = new ArrayList<>(Minecraft.getMinecraft().player.world.loadedEntityList);
                EntityUtil.Kill(entities);
            }
        }
        if(GAME_MODE.isPressed()){
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player)){
                int mode;
                int current = ((iEntityPlayer) Minecraft.getMinecraft().player).GetGameMode();
                if (current == -1 || current == 3) {
                    mode = 0;
                    ((iEntityPlayer) Minecraft.getMinecraft().player).SetGameMode(0);
                } else {
                    mode = current + 1;
                    ((iEntityPlayer) Minecraft.getMinecraft().player).SetGameMode(mode);
                }
                NetworkHandler.INSTANCE.sendMessageToServer(new GameModeChange(mode, Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.getEntityId()));
            }
        }
        if (MIKU_MODE.isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            NetworkHandler.INSTANCE.sendMessageToServer(new MikuModeChange(player.getEntityId(), player.dimension));
        }
        if (RECORD_TIME_POINT.isPressed()) {
            NetworkHandler.INSTANCE.sendMessageToServer(new RecordTimePoint());
        }
        if (SWITCH_TIME_POINT.isPressed()) {
            NetworkHandler.INSTANCE.sendMessageToServer(new SwitchTimePoint(Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.getEntityId()));
        }
        if (BACK_TO_TIME_POINT.isPressed()) {
            NetworkHandler.INSTANCE.sendMessageToServer(new BackToTimePoint(Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.getEntityId()));
        }
    }
}
