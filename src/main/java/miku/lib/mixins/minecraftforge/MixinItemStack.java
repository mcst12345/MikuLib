package miku.lib.mixins.minecraftforge;

import miku.lib.common.item.SpecialItem;
import miku.lib.common.sqlite.Sqlite;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

@Mixin(value = ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Shadow private int stackSize;

    @Shadow
    private boolean isEmpty;

    @Shadow
    @Final
    private Item item;

    @Shadow
    @Final
    public static ItemStack EMPTY;

    @Shadow
    int itemDamage;

    @Shadow
    protected abstract void updateEmptyState();

    @Shadow
    private NBTTagCompound stackTagCompound;

    @Shadow
    public abstract void shrink(int quantity);

    @Shadow
    public abstract boolean attemptDamageItem(int amount, Random rand, @Nullable EntityPlayerMP damager);

    @Shadow
    public abstract boolean isItemStackDamageable();

    @Inject(at = @At("HEAD"), method = "isItemStackDamageable", cancellable = true)
    public void isItemStackDamageable(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "isItemDamaged", cancellable = true)
    public void isItemDamaged(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "setItemDamage", cancellable = true)
    public void setItemDamage(int meta, CallbackInfo ci) {
        if (this.getItem() instanceof SpecialItem) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getItemDamage", cancellable = true)
    public void getItemDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(0);
        }
    }

    @Inject(at = @At("HEAD"), method = "getMaxDamage", cancellable = true)
    public void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(Integer.MAX_VALUE);
        }
        if(Sqlite.IS_ITEM_BANNED(this.getItem()))cir.setReturnValue(0);
    }

    @Inject(at = @At("HEAD"), method = "attemptDamageItem", cancellable = true)
    public void attemptDamageItem(int amount, Random rand, EntityPlayerMP damager, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "canDestroy", cancellable = true)
    public void canDestroy(Block blockIn, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof SpecialItem) {
            cir.setReturnValue(true);
        }
        if (Sqlite.IS_ITEM_BANNED(this.getItem())) cir.setReturnValue(false);
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void damageItem(int amount, EntityLivingBase entityIn) {
        if (this.item instanceof SpecialItem) return;
        if (!(entityIn instanceof EntityPlayer) || !((EntityPlayer) entityIn).capabilities.isCreativeMode) {
            if (this.isItemStackDamageable()) {
                if (this.attemptDamageItem(amount, entityIn.getRNG(), entityIn instanceof EntityPlayerMP ? (EntityPlayerMP) entityIn : null)) {
                    entityIn.renderBrokenItemStack((ItemStack) (Object) this);
                    this.shrink(1);

                    if (entityIn instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entityIn;
                        entityplayer.addStat(Objects.requireNonNull(StatList.getObjectBreakStats(this.item)));
                    }

                    this.itemDamage = 0;
                }
            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public float getDestroySpeed(IBlockState blockIn) {
        if (this.item instanceof SpecialItem) {
            return Float.MAX_VALUE;
        }
        return this.getItem().getDestroySpeed((ItemStack) (Object) this, blockIn);
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public boolean isItemEnchanted() {
        if (this.item instanceof SpecialItem) return true;
        if (this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9)) {
            return !this.stackTagCompound.getTagList("ench", 10).isEmpty();
        } else {
            return false;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void setCount(int size) {
        if (this.item instanceof SpecialItem) {
            this.stackSize = 1;
            return;
        }
        if (Sqlite.IS_ITEM_BANNED(this.getItem())) {
            this.stackSize = 0;
            this.isEmpty = true;
            return;
        }
        this.stackSize = size;
        this.updateEmptyState();
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public int getCount() {
        if (this.item instanceof SpecialItem) {
            return 1;
        }
        if (Sqlite.IS_ITEM_BANNED(this.getItem())) return 0;
        return this.isEmpty ? 0 : this.stackSize;
    }


    /**
     * @author mcst12345
     * @reason F**k
     */
    @Nullable
    @Overwrite(remap = false)
    public Item getItemRaw() {
        return Sqlite.IS_ITEM_BANNED(this.item) ? null : this.item;
    }

    @Inject(at=@At("HEAD"),method = "copy", cancellable = true)
    public void copy(CallbackInfoReturnable<ItemStack> cir){
        if(Sqlite.IS_ITEM_BANNED(this.item))cir.setReturnValue(EMPTY);
    }

    /**
     * @author mcst12345
     * @reason Hi,chaoswither@0.2.6. Fuck you!
     */
    @Overwrite
    public boolean isEmpty()
    {
        if(item instanceof SpecialItem)return false;
        if (Sqlite.IS_ITEM_BANNED(item)) return true;
        if ((Object) this == EMPTY) {
            return true;
        } else if (this.getItemRaw() != null && this.getItemRaw() != Items.AIR) {
            if (this.stackSize <= 0) {
                return true;
            } else {
                return this.itemDamage < -32768 || this.itemDamage > 65535;
            }
        } else {
            return true;
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void onPlayerStoppedUsing(World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        try {
            this.getItem().onPlayerStoppedUsing((ItemStack) (Object) this, worldIn, entityLiving, timeLeft);
        } catch (Throwable t) {
            System.out.println("MikuWarn:Catch exception at onPlayerStoppedUsing,item:" + this.getItem().getRegistryName());
            t.printStackTrace();
        }
    }
}
