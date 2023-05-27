package miku.lib.mixins;

import miku.lib.api.iEntity;
import miku.lib.api.iEntityLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityLiving.class)
public abstract class MixinEntityLiving extends EntityLivingBase implements iEntityLiving {
    @Final
    @Shadow
    private static final DataParameter<Byte> AI_FLAGS = EntityDataManager.createKey(EntityLiving.class, DataSerializers.BYTE);

    @Final
    @Shadow
    private final NonNullList<ItemStack> inventoryHands = NonNullList.withSize(2, ItemStack.EMPTY);

    public NonNullList<ItemStack> inventoryHands(){
        return inventoryHands;
    }

    @Final
    @Shadow
    private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    public NonNullList<ItemStack> inventoryArmor(){
        return inventoryArmor;
    }

    public MixinEntityLiving(World worldIn) {
        super(worldIn);
    }

    @Override
    public void Kill(){
        ClearAI();
        ClearInventory();
    }

    @Override
    public void ClearAI(){
        byte b0 = ((iEntity) this).GetDataManager().get(AI_FLAGS);
        ((iEntity) this).GetDataManager().set(AI_FLAGS, (byte) (b0 | 1));
    }

    @Override
    public void ClearInventory(){
        inventoryHands.set(0, ItemStack.EMPTY);
        inventoryHands.set(1, ItemStack.EMPTY);
        inventoryArmor.set(0, ItemStack.EMPTY);
        inventoryArmor.set(1, ItemStack.EMPTY);
        inventoryArmor.set(2, ItemStack.EMPTY);
        inventoryArmor.set(3, ItemStack.EMPTY);
        inventoryHands.clear();
        inventoryArmor.clear();
    }

    @Inject(at=@At("HEAD"),method = "onUpdate", cancellable = true)
    public void onUpdate(CallbackInfo ci){
        if(((iEntity)this).isTimeStop()){
            ((iEntity)this).TimeStop();
            this.swingProgressInt=0;
            this.swingProgress=0.0f;
            ci.cancel();
        }
    }
}
