package miku.lib.mixins.minecraft;

import miku.lib.common.sqlite.Sqlite;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {
    @Shadow
    @Final
    private GameSettings gameSettings;

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void updatePlayerMoveState() {
        boolean debug = Sqlite.DEBUG() && Sqlite.GetBooleanFromTable("key_info", "LOG_CONFIG");

        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            if (debug) {
                System.out.println("MikuInfo:keyBindForward is pressed.");
            }
            ++this.moveForward;
            this.forwardKeyDown = true;
        } else {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            if (debug) {
                System.out.println("MikuInfo:keyBindBack is pressed.");
            }
            --this.moveForward;
            this.backKeyDown = true;
        } else {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            if (debug) {
                System.out.println("MikuInfo:keyBindLeft is pressed.");
            }
            ++this.moveStrafe;
            this.leftKeyDown = true;
        } else {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            if (debug) {
                System.out.println("MikuInfo:keyBindRight is pressed.");
            }
            --this.moveStrafe;
            this.rightKeyDown = true;
        } else {
            this.rightKeyDown = false;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        if (this.sneak) {
            this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
            this.moveForward = (float) ((double) this.moveForward * 0.3D);
        }
    }
}
