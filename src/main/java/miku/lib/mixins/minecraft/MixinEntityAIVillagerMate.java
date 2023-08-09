package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityAIVillagerMate.class)
public abstract class MixinEntityAIVillagerMate extends EntityAIBase {
    @Shadow
    @Final
    private EntityVillager villager;

    @Shadow
    private EntityVillager mate;

    @Shadow
    @Final
    private World world;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    private void giveBirth() {
        net.minecraft.entity.EntityAgeable entityvillager = this.villager.createChild(this.mate);
        this.mate.setGrowingAge(6000);
        this.villager.setGrowingAge(6000);
        this.mate.setIsWillingToMate(false);
        this.villager.setIsWillingToMate(false);

        final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(villager, mate, entityvillager);
        if (MikuLib.MikuEventBus().post(event) || event.getChild() == null) {
            return;
        }
        entityvillager = event.getChild();
        entityvillager.setGrowingAge(-24000);
        entityvillager.setLocationAndAngles(this.villager.posX, this.villager.posY, this.villager.posZ, 0.0F, 0.0F);
        this.world.spawnEntity(entityvillager);
        this.world.setEntityState(entityvillager, (byte) 12);
    }
}
