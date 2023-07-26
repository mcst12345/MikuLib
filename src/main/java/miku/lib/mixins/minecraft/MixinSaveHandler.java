package miku.lib.mixins.minecraft;

import miku.lib.common.util.EntityUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.SaveHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;

@Mixin(value = SaveHandler.class)
public class MixinSaveHandler {
    @Shadow @Final private File playersDirectory;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final protected DataFixer dataFixer;

    @Inject(at=@At("HEAD"),method = "readPlayerData", cancellable = true)
    public void readPlayerData(EntityPlayer player, CallbackInfoReturnable<NBTTagCompound> cir){
        if(EntityUtil.isDEAD(player))cir.setReturnValue(null);
        if(EntityUtil.isProtected(player)){
            NBTTagCompound nbttagcompound = null;

            try
            {
                File file1 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat");

                if (file1.exists() && file1.isFile())
                {
                    nbttagcompound = CompressedStreamTools.readCompressed(Files.newInputStream(file1.toPath()));
                }
            }
            catch (Exception var4)
            {
                LOGGER.warn("Failed to load player data for {}", player.getName());
            }

            if (nbttagcompound != null)
            {
                player.readFromNBT(this.dataFixer.process(FixTypes.PLAYER, nbttagcompound));
            }

            cir.setReturnValue(nbttagcompound);
        }
    }

    @Inject(at=@At("HEAD"),method = "writePlayerData", cancellable = true)
    public void writePlayerData(EntityPlayer player, CallbackInfo ci){
        if(EntityUtil.isDEAD(player))ci.cancel();
        if(EntityUtil.isProtected(player)){
            try
            {
                NBTTagCompound nbttagcompound = player.writeToNBT(new NBTTagCompound());
                File file1 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat.tmp");
                File file2 = new File(this.playersDirectory, player.getCachedUniqueIdString() + ".dat");
                CompressedStreamTools.writeCompressed(nbttagcompound, Files.newOutputStream(file1.toPath()));

                if (file2.exists())
                {
                    file2.delete();
                }

                file1.renameTo(file2);
                net.minecraftforge.event.ForgeEventFactory.firePlayerSavingEvent(player, this.playersDirectory, player.getUniqueID().toString());
            }
            catch (Exception var5)
            {
                LOGGER.warn("Failed to save player data for {}", player.getName());
            }
            ci.cancel();
        }
    }
}
