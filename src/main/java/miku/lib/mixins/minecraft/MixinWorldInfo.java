package miku.lib.mixins.minecraft;

import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorldInfo.class)
public class MixinWorldInfo {
    @Shadow
    private EnumDifficulty difficulty;

    /**
     * @author mcst12345
     * @reason null check :)
     */
    @Overwrite
    public void setDifficulty(EnumDifficulty newDifficulty) {
        if (newDifficulty == null || this.difficulty == null) {
            if (this.difficulty == null)
                this.difficulty = newDifficulty == null ? EnumDifficulty.NORMAL : newDifficulty;
            return;
        }
        net.minecraftforge.common.ForgeHooks.onDifficultyChange(newDifficulty, this.difficulty);
        this.difficulty = newDifficulty;
    }
}
