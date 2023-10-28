package miku.lib.client.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EmptyTutorial extends Tutorial {
    private static EmptyTutorial tutorial;

    private EmptyTutorial(Minecraft minecraft) {
        super(minecraft);
    }

    public void handleMovement(@NotNull MovementInput p_193293_1_) {
    }

    public void handleMouse(@NotNull MouseHelper p_193299_1_) {
    }

    public void onMouseHover(@Nullable WorldClient worldIn, @Nullable RayTraceResult result) {
    }

    public void onHitBlock(@NotNull WorldClient worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, float diggingStage) {
    }

    public void openInventory() {
    }

    public void handleSetSlot(@NotNull ItemStack stack) {
    }

    public void stop() {
    }

    public void reload() {
    }

    public void update() {
    }

    public void setStep(@NotNull TutorialSteps step) {
    }

    public static EmptyTutorial get(Object mc) {
        if (tutorial == null) {
            tutorial = new EmptyTutorial((Minecraft) mc);
        }
        return tutorial;
    }
}
