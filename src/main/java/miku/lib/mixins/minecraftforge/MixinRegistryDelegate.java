package miku.lib.mixins.minecraftforge;

import miku.lib.common.api.iRegistryDelegate;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraftforge.registries.RegistryDelegate")
public abstract class MixinRegistryDelegate<T> implements IRegistryDelegate<T>, iRegistryDelegate {
    @Shadow
    public abstract void setName(ResourceLocation name);

    @Shadow
    abstract void changeReference(T newTarget);

    public void SetName(ResourceLocation rs) {
        this.setName(rs);
    }

    public void ChangeReference(Object newTarget) {
        changeReference((T) newTarget);
    }

}
