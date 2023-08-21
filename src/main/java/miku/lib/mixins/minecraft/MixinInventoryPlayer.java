package miku.lib.mixins.minecraft;

import miku.lib.common.api.iInventoryPlayer;
import miku.lib.common.command.MikuInsaneMode;
import miku.lib.common.item.SpecialItem;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.ItemUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(value = InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements iInventoryPlayer {
    @Shadow
    public EntityPlayer player;


    @Shadow
    @Final
    private List<NonNullList<ItemStack>> allInventories;

    @Shadow
    public abstract int getFirstEmptyStack();

    @Shadow
    @Final
    public NonNullList<ItemStack> mainInventory;

    @Shadow
    @Final
    public NonNullList<ItemStack> armorInventory;

    @Shadow
    public int currentItem;

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void clear() {
        if (EntityUtil.isProtected(player)) {
            return;
        }
        for (List<ItemStack> list : this.allInventories) {
            list.clear();
        }
    }

    @Override
    public void Clear() {
        for (List<ItemStack> list : allInventories) {
            Collections.fill(list, ItemStack.EMPTY);
        }
    }

    @Override
    public void ADD(ItemStack stack,int i) {
        int a = getFirstEmptyStack();
        if(a!=-1)mainInventory.set(a,stack);
        else mainInventory.set(i,stack);
    }

    @Inject(at = @At("HEAD"), method = "clearMatchingItems", cancellable = true)
    public void clearMatchingItems(Item itemIn, int metadataIn, int removeCount, NBTTagCompound itemNBT, CallbackInfoReturnable<Integer> cir) {
        if (EntityUtil.isProtected(player)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(at = @At("HEAD"), method = "dropAllItems", cancellable = true)
    public void dropAllItems(CallbackInfo ci) {
        if (EntityUtil.isProtected(player)) {
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    public void decrementAnimations() {
        if (MikuInsaneMode.isMikuInsaneMode()) return;
        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (!nonnulllist.get(i).isEmpty()) {
                    try {
                        nonnulllist.get(i).updateAnimation(this.player.world, this.player, i, this.currentItem == i);
                    } catch (Throwable t) {
                        System.out.println("MikuWarn:Catch exception when updatingItemAnimation,item:" + nonnulllist.get(i).getItem().getRegistryName());
                        t.printStackTrace();
                    }
                }
            }
        }
        for (ItemStack is : armorInventory) // FORGE: Tick armor on animation ticks
        {
            if (!is.isEmpty()) {
                try {
                    is.getItem().onArmorTick(player.world, player, is);
                } catch (Throwable t) {
                    System.out.println("MikuWarn:Catch exception at onArmorTick,item:" + is.getItem().getRegistryName());
                    t.printStackTrace();
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "add", cancellable = true)
    public void add(int p_191971_1_, ItemStack p_191971_2_, CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.isProtected(this.player)) {
            if (ItemUtil.BadItem(p_191971_2_)) cir.setReturnValue(true);
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void deleteStack(ItemStack stack) {
        if (stack.getItem() instanceof SpecialItem) {
            return;
        }
        for (NonNullList<ItemStack> nonnulllist : this.allInventories) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (nonnulllist.get(i) == stack) {
                    nonnulllist.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public ItemStack removeStackFromSlot(int index) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null && !nonnulllist.get(index).isEmpty()) {
            ItemStack itemstack = nonnulllist.get(index);
            if (!(itemstack.getItem() instanceof SpecialItem)) nonnulllist.set(index, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void setInventorySlotContents(int index, ItemStack stack) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.allInventories) {
            if (index < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            index -= nonnulllist1.size();
        }

        if (nonnulllist != null) {
            ItemStack stack1 = nonnulllist.get(index);
            if (stack1.getItem() instanceof SpecialItem) return;
            nonnulllist.set(index, stack);
        }
    }
}
