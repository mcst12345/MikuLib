package miku.lib.mixins;

import com.mojang.authlib.GameProfile;
import miku.lib.api.iEntityPlayerMP;
import miku.lib.util.EntityUtil;
import miku.lib.util.MikuUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer implements iEntityPlayerMP {

    @Shadow private boolean disconnected;

    public MixinEntityPlayerMP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Override
    public boolean disconnected(){
        return disconnected;
    }

    @Override
    public void SetDisconnected(boolean value){
        disconnected = value;
    }


    @Inject(at=@At("HEAD"),method = "canAttackPlayer", cancellable = true)
    public void canAttackPlayer(EntityPlayer other, CallbackInfoReturnable<Boolean> cir){
        if(EntityUtil.isProtected(other))cir.setReturnValue(false);
        if(EntityUtil.isProtected(this))cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void onDeath(DamageSource cause, CallbackInfo ci){
        if (EntityUtil.isProtected(this)) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "attackEntityFrom", cancellable = true)
    public void attackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        if (EntityUtil.isProtected(this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "setGameType", cancellable = true)
    public void setGameType(GameType gameType, CallbackInfo ci) {
        if (EntityUtil.isProtected(this)) {
            //TODO
            ((EntityPlayerMP) (Object) this).interactionManager.setGameType(GameType.CREATIVE);
            ((EntityPlayerMP) (Object) this).connection.sendPacket(new SPacketChangeGameState(3, (float) GameType.CREATIVE.getID()));
            ((EntityPlayerMP) (Object) this).sendPlayerAbilities();
            ci.cancel();
        }
    }

    /**
     * @author mcst12345
     * @reason MikuMod
     */
    @Overwrite
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList)
    {
        MikuUtil.sendAllContents(containerToSend,itemsList,(EntityPlayerMP)(Object)this);
    }
}
