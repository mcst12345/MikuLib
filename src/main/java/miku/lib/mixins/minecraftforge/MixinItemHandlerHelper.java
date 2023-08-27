package miku.lib.mixins.minecraftforge;

import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.ItemUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;

import static net.minecraftforge.items.ItemHandlerHelper.insertItemStacked;

@Mixin(value = ItemHandlerHelper.class, remap = false)
public class MixinItemHandlerHelper {
    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public static void giveItemToPlayer(EntityPlayer player, @Nonnull ItemStack stack) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        if (EntityUtil.isProtected(player)) {
            if (ItemUtil.BadItem(stack)) return;
        }
        giveItemToPlayer(player, stack, -1);
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public static void giveItemToPlayer(EntityPlayer player, @Nonnull ItemStack stack, int preferredSlot) {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        if (EntityUtil.isProtected(player)) {
            if (ItemUtil.BadItem(stack)) return;
        }
        if (stack.isEmpty()) return;

        IItemHandler inventory = new PlayerMainInvWrapper(player.inventory);
        World world = player.world;

        // try adding it into the inventory
        ItemStack remainder = stack;
        // insert into preferred slot first
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, stack, false);
        }
        // then into the inventory in general
        if (!remainder.isEmpty()) {
            remainder = insertItemStacked(inventory, remainder, false);
        }

        // play sound if something got picked up
        if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
            world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        // drop remaining itemstack into the world
        if (!remainder.isEmpty() && !world.isRemote) {
            EntityItem entityitem = new EntityItem(world, player.posX, player.posY + 0.5, player.posZ, remainder);
            entityitem.setPickupDelay(40);
            entityitem.motionX = 0;
            entityitem.motionZ = 0;

            world.spawnEntity(entityitem);
        }
    }
}
