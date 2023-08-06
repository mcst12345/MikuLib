package miku.lib.client.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;

@SideOnly(Side.CLIENT)
public class SoundSystemStarterThread extends SoundSystem {
    public SoundSystemStarterThread() {
    }

    public boolean playing(String p_playing_1_) {
        synchronized (SoundSystemConfig.THREAD_SYNC) {
            if (this.soundLibrary == null) {
                return false;
            } else {
                Source source = this.soundLibrary.getSources().get(p_playing_1_);

                if (source == null) {
                    return false;
                } else {
                    return source.playing() || source.paused() || source.preLoad;
                }
            }
        }
    }
}
