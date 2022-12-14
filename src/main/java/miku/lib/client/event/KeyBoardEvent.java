package miku.lib.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBoardEvent {
    protected static final fuck thread = new fuck();

    @SideOnly(Side.CLIENT)
    public static final KeyBinding IN_GAME_FOCUS = new KeyBinding("key.miku.in_game_focus", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_I, "key.category.miku");

    @SideOnly(Side.CLIENT)
    public static final KeyBinding FUCK = new KeyBinding("key.miku.in_game_focus_while", KeyConflictContext.UNIVERSAL, KeyModifier.SHIFT, Keyboard.KEY_O, "key.category.miku");


    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyPressed(InputEvent.KeyInputEvent event){
        if(IN_GAME_FOCUS.isPressed()){
            if(Minecraft.getMinecraft().inGameHasFocus)Minecraft.getMinecraft().setIngameNotInFocus();
            else Minecraft.getMinecraft().setIngameFocus();
        }
        if(FUCK.isPressed()){
            if(!thread.isAlive()) {
                thread.flag = !Minecraft.getMinecraft().inGameHasFocus;
                thread.start();
            }
            else {
                thread.stop();
            }
        }
    }

    static class fuck extends Thread{
        public boolean flag;

        @Override
        public void run(){
            while(true){
                if(flag)Minecraft.getMinecraft().setIngameFocus();
                else Minecraft.getMinecraft().setIngameNotInFocus();
            }
        }
    }

    public static void Init(){
        ClientRegistry.registerKeyBinding(IN_GAME_FOCUS);
        ClientRegistry.registerKeyBinding(FUCK);
    }
}
