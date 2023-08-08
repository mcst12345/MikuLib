package miku.lib.mixins.minecraft;

import com.google.common.collect.Multimap;
import io.netty.util.internal.ThreadLocalRandom;
import miku.lib.common.core.MikuLib;
import net.minecraft.client.audio.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = SoundManager.class)
public abstract class MixinSoundManager {
    @Shadow
    @Final
    private static Set<ResourceLocation> UNABLE_TO_PLAY;

    @Shadow
    @Final
    public SoundHandler sndHandler;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract void unloadSoundSystem();

    @Shadow
    protected abstract void loadSoundSystem();

    @Shadow
    private boolean loaded;

    @Shadow
    @Final
    private static Marker LOG_MARKER;

    @Shadow
    @Final
    private List<ISoundEventListener> listeners;

    @Shadow
    protected abstract float getClampedVolume(ISound soundIn);

    @Shadow
    protected abstract float getClampedPitch(ISound soundIn);

    @Shadow
    private static URL getURLForSoundResource(ResourceLocation p_148612_0_) {
        return null;
    }

    @Shadow
    @Final
    private Map<String, Integer> playingSoundsStopTime;

    @Shadow
    private int playTime;

    @Shadow
    @Final
    private Map<String, ISound> playingSounds;

    @Shadow
    @Final
    private Multimap<SoundCategory, String> categorySounds;

    @Shadow
    @Final
    private List<ITickableSound> tickableSounds;

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void SoundManager(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS = MikuLib.MikuEventBus();
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void reloadSoundSystem() {
        UNABLE_TO_PLAY.clear();

        for (SoundEvent soundevent : SoundEvent.REGISTRY) {
            ResourceLocation resourcelocation = soundevent.getSoundName();

            if (this.sndHandler.getAccessor(resourcelocation) == null) {
                LOGGER.warn("Missing sound for event: {}", SoundEvent.REGISTRY.getNameForObject(soundevent));
                UNABLE_TO_PLAY.add(resourcelocation);
            }
        }

        this.unloadSoundSystem();
        this.loadSoundSystem();
        MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.sound.SoundLoadEvent((SoundManager) (Object) this));
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void playSound(ISound p_sound) {
        SoundSystem o;
        try {
            o = (SoundSystem) SndSystem.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (this.loaded) {
            p_sound = net.minecraftforge.client.ForgeHooksClient.playSound((SoundManager) (Object) this, p_sound);
            if (p_sound == null) return;

            SoundEventAccessor soundeventaccessor = p_sound.createAccessor(this.sndHandler);
            ResourceLocation resourcelocation = p_sound.getSoundLocation();

            if (soundeventaccessor == null) {
                if (UNABLE_TO_PLAY.add(resourcelocation)) {
                    LOGGER.warn(LOG_MARKER, "Unable to play unknown soundEvent: {}", resourcelocation);
                }
            } else {
                if (!this.listeners.isEmpty()) {
                    for (ISoundEventListener isoundeventlistener : this.listeners) {
                        isoundeventlistener.soundPlay(p_sound, soundeventaccessor);
                    }
                }

                if (o.getMasterVolume() <= 0.0F) {
                    LOGGER.debug(LOG_MARKER, "Skipped playing soundEvent: {}, master volume was zero", resourcelocation);
                } else {
                    Sound sound = p_sound.getSound();

                    if (sound == SoundHandler.MISSING_SOUND) {
                        if (UNABLE_TO_PLAY.add(resourcelocation)) {
                            LOGGER.warn(LOG_MARKER, "Unable to play empty soundEvent: {}", resourcelocation);
                        }
                    } else {
                        float f3 = p_sound.getVolume();
                        float f = 16.0F;

                        if (f3 > 1.0F) {
                            f *= f3;
                        }

                        SoundCategory soundcategory = p_sound.getCategory();
                        float f1 = this.getClampedVolume(p_sound);
                        float f2 = this.getClampedPitch(p_sound);

                        if (f1 == 0.0F) {
                            LOGGER.debug(LOG_MARKER, "Skipped playing sound {}, volume was zero.", sound.getSoundLocation());
                        } else {
                            boolean flag = p_sound.canRepeat() && p_sound.getRepeatDelay() == 0;
                            String s = MathHelper.getRandomUUID(ThreadLocalRandom.current()).toString();
                            ResourceLocation resourcelocation1 = sound.getSoundAsOggLocation();

                            if (sound.isStreaming()) {
                                o.newStreamingSource(false, s, getURLForSoundResource(resourcelocation1), resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(), p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                                MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.sound.PlayStreamingSourceEvent((SoundManager) (Object) this, p_sound, s));
                            } else {
                                o.newSource(false, s, getURLForSoundResource(resourcelocation1), resourcelocation1.toString(), flag, p_sound.getXPosF(), p_sound.getYPosF(), p_sound.getZPosF(), p_sound.getAttenuationType().getTypeInt(), f);
                                MikuLib.MikuEventBus().post(new net.minecraftforge.client.event.sound.PlaySoundSourceEvent((SoundManager) (Object) this, p_sound, s));
                            }

                            LOGGER.debug(LOG_MARKER, "Playing sound {} for event {} as channel {}", sound.getSoundLocation(), resourcelocation, s);
                            o.setPitch(s, f2);
                            o.setVolume(s, f1);
                            o.play(s);
                            this.playingSoundsStopTime.put(s, this.playTime + 20);
                            this.playingSounds.put(s, p_sound);
                            this.categorySounds.put(soundcategory, s);

                            if (p_sound instanceof ITickableSound) {
                                this.tickableSounds.add((ITickableSound) p_sound);
                            }
                        }
                    }
                }
            }
        }
    }

    private static Field SndSystem;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(SoundHandler p_i45119_1_, GameSettings p_i45119_2_, CallbackInfo ci) {
        try {
            SndSystem = SoundManager.class.getDeclaredField("sndSystem");
            SndSystem.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
