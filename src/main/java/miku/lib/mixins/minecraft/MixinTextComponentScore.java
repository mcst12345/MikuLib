package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentScore;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TextComponentScore.class)
public abstract class MixinTextComponentScore extends TextComponentBase {
    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private String objective;

    @Shadow
    private String value;

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getName() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.name;
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @Overwrite
    public String getObjective() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.objective;
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @NotNull
    @Overwrite
    public String getUnformattedComponentText() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return this.value;
    }

    /**
     * @author mcst12345
     * @reason Let's all love Lain
     */
    @NotNull
    @Overwrite
    public String toString() {
        if (MikuLib.isLAIN()) {
            return "Let's all love Lain";
        }
        return "ScoreComponent{name='" + this.name + '\'' + "objective='" + this.objective + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }
}
