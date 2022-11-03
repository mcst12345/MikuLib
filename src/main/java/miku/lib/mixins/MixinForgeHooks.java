package miku.lib.mixins;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ForgeHooks.class)
public class MixinForgeHooks {
    @Final
    @Shadow
    private static final ForgeRegistry<DataSerializerEntry> serializerRegistry = (ForgeRegistry<DataSerializerEntry>) ForgeRegistries.DATA_SERIALIZERS;


    public void ForgeHooks(){}
}
