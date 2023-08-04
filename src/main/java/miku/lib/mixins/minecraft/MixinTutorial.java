package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.tutorial.ITutorialStep;
import net.minecraft.client.tutorial.Tutorial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = Tutorial.class)
public abstract class MixinTutorial {
    @Shadow
    @Nullable
    private ITutorialStep tutorialStep;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void stop();

    @Shadow
    public abstract void reload();

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void update() {
        if (this.tutorialStep != null) {
            if (((iMinecraft) this.minecraft).MikuWorld() != null) {
                this.tutorialStep.update();
            } else {
                this.stop();
            }
        } else if (((iMinecraft) this.minecraft).MikuWorld() != null) {
            this.reload();
        }
    }
}
