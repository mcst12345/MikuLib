package miku.lib.client.event;

import miku.lib.network.NetworkHandler;
import miku.lib.network.packets.KillAllEntities;
import miku.lib.network.packets.TimeStop;
import miku.lib.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
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
    protected static final fuck thread = new fuck();

    @SideOnly(Side.CLIENT)
    public static final KeyBinding IN_GAME_FOCUS = new KeyBinding("key.miku.in_game_focus", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_I, "key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding FUCK = new KeyBinding("key.miku.in_game_focus_while", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_O, "key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding TIME_STOP = new KeyBinding("key.miku.time_stop", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_L,"key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding KILL_ALL = new KeyBinding("key.miku.kill_all",KeyConflictContext.UNIVERSAL,KeyModifier.ALT,Keyboard.KEY_K,"key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding GAME_MODE = new KeyBinding("key.miku.game_mode",KeyConflictContext.UNIVERSAL,KeyModifier.ALT,Keyboard.KEY_M,"key.category.miku");

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyPressed(InputEvent.KeyInputEvent event){
        if(IN_GAME_FOCUS.isPressed()){
            if(Minecraft.getMinecraft().inGameHasFocus)Minecraft.getMinecraft().setIngameNotInFocus();
            else Minecraft.getMinecraft().setIngameFocus();
            thread.flag = !thread.flag;
        }
        if(FUCK.isPressed()){
            if(!thread.isAlive()) {
                thread.flag = !Minecraft.getMinecraft().inGameHasFocus;
                thread.start();
            }
            else {
                thread.enabled = ! thread.enabled;
            }
        }
        if(TIME_STOP.isPressed()){
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player)){
                NetworkHandler.INSTANCE.sendMessageToServer(new TimeStop(Minecraft.getMinecraft().player.dimension));
            }
        }
        if(KILL_ALL.isPressed()){
            if(EntityUtil.isProtected(Minecraft.getMinecraft().player)){
                NetworkHandler.INSTANCE.sendMessageToServer(new KillAllEntities(Minecraft.getMinecraft().player.dimension));
                List<Entity> entities = new ArrayList<>(Minecraft.getMinecraft().player.world.loadedEntityList);
                EntityUtil.Kill(entities);
            }
        }
    }

    static class fuck extends Thread{
        public boolean flag;
        public boolean enabled = true;

        @Override
        public void run(){
            while(true){
                if(enabled){
                    if (flag) Minecraft.getMinecraft().setIngameFocus();
                    else Minecraft.getMinecraft().setIngameNotInFocus();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void Init(){
        ClientRegistry.registerKeyBinding(IN_GAME_FOCUS);
        ClientRegistry.registerKeyBinding(FUCK);
        ClientRegistry.registerKeyBinding(TIME_STOP);
        ClientRegistry.registerKeyBinding(KILL_ALL);
    }
}
