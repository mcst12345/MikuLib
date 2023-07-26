package miku.lib.common.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioUtil {
    protected static boolean isPlaying=false;

    //only support wav file.
    public static void PlayMusic(String path){
        AudioInputStream as;
        try {
            if (isPlaying) {
                System.out.println("[info]Already playing music");
                return;
            }
            isPlaying = true;
            File MusicFile = new File(path);
            if (!MusicFile.exists()) {
                throw new RuntimeException("Audio File Not Found!");
            }
            as = AudioSystem.getAudioInputStream(MusicFile);
            AudioFormat format = as.getFormat();
            SourceDataLine sdl;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            sdl = (SourceDataLine) AudioSystem.getLine(info);
            sdl.open(format);
            sdl.start();
            int nBytesRead = 0;
            byte[] abData = new byte[512];
            while (nBytesRead != -1) {
                nBytesRead = as.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    sdl.write(abData, 0, nBytesRead);
            }
            sdl.drain();
            sdl.close();
            isPlaying = false;
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
}
