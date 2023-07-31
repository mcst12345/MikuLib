package miku.lib.mixins.minecraft;

import miku.lib.common.api.iEntity;
import miku.lib.common.api.iEntityLiving;
import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

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
    public void ClearAI() {
        try {
            Field field = Entity.class.getDeclaredField("field_70180_af");
            long tmp = Launch.UNSAFE.objectFieldOffset(field);
            EntityDataManager manager = (EntityDataManager) Launch.UNSAFE.getObjectVolatile(this, tmp);
            byte b0 = manager.get(AI_FLAGS);
            manager.set(AI_FLAGS, (byte) (b0 | 1));
        } catch (NoSuchFieldException e) {
            System.out.println(e.getLocalizedMessage());
        }
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

    @Inject(at=@At("HEAD"),method = "handleStatusUpdate", cancellable = true)
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id, CallbackInfo ci){
        if(EntityUtil.isProtected(this)){
            if(id == (byte) 3 || id == (byte) 30 || id == (byte) 29 || id == (byte) 37 || id == (byte) 33 || id == (byte) 36 || id == (byte) 20 || id == (byte) 2 || id == (byte) 35)ci.cancel();
        }
    }
}
