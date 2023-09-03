package miku.lib.mixins.minecraft;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = FurnaceRecipes.class)
public class MixinFurnaceRecipes {
    @Mutable
    @Shadow
    @Final
    private Map<ItemStack, ItemStack> smeltingList;

    @Mutable
    @Shadow
    @Final
    private Map<ItemStack, Float> experienceList;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci) {
        Map<ItemStack, ItemStack> better1 = new Object2ObjectOpenHashMap<>();
        better1.putAll(smeltingList);
        smeltingList = better1;
        Map<ItemStack, Float> better2 = new Object2FloatOpenHashMap<>();
        better2.putAll(experienceList);
        experienceList = better2;
    }
}
