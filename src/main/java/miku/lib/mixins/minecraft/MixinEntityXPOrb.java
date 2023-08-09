package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;

@Mixin(value = EntityXPOrb.class)
public abstract class MixinEntityXPOrb extends Entity {
    @Shadow
    public int delayBeforeCanPickup;

    @Shadow
    private static int roundAverage(float value) {
        return 0;
    }

    @Shadow
    public int xpValue;

    public MixinEntityXPOrb(World worldIn) {
        super(worldIn);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onCollideWithPlayer(@Nonnull EntityPlayer entityIn) {
        if (!this.world.isRemote) {
            if (this.delayBeforeCanPickup == 0 && entityIn.xpCooldown == 0) {
                if (MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerPickupXpEvent(entityIn, (EntityXPOrb) (Object) this)))
                    return;
                entityIn.xpCooldown = 2;
                entityIn.onItemPickup(this, 1);
                ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, entityIn);

                if (!itemstack.isEmpty() && itemstack.isItemDamaged()) {
                    float ratio = itemstack.getItem().getXpRepairRatio(itemstack);
                    int i = Math.min(roundAverage(this.xpValue * ratio), itemstack.getItemDamage());
                    this.xpValue -= roundAverage(i / ratio);
                    itemstack.setItemDamage(itemstack.getItemDamage() - i);
                }

                if (this.xpValue > 0) {
                    entityIn.addExperience(this.xpValue);
                }

                this.setDead();
            }
        }
    }
}
