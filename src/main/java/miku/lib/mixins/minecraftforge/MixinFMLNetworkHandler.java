package miku.lib.mixins.minecraftforge;

import io.netty.channel.embedded.EmbeddedChannel;
import miku.lib.common.core.MikuLib;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.EnumMap;

@Mixin(value = FMLNetworkHandler.class, remap = false)
public abstract class MixinFMLNetworkHandler {
    @Shadow
    private static EnumMap<Side, FMLEmbeddedChannel> channelPair;

    /**
     * @author mcst12345
     * @reason Fuck!!
     */
    @Overwrite
    public static void openGui(EntityPlayer entityPlayer, Object mod, int modGuiId, World world, int x, int y, int z) {
        ModContainer mc = FMLCommonHandler.instance().findContainerFor(mod);
        if (entityPlayer instanceof EntityPlayerMP && !(entityPlayer instanceof FakePlayer)) {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entityPlayer;
            Container remoteGuiContainer = NetworkRegistry.INSTANCE.getRemoteGuiContainer(mc, entityPlayerMP, modGuiId, world, x, y, z);
            if (remoteGuiContainer != null) {
                entityPlayerMP.getNextWindowId();
                entityPlayerMP.closeContainer();
                int windowId = entityPlayerMP.currentWindowId;
                FMLMessage.OpenGui openGui = new FMLMessage.OpenGui(windowId, mc.getModId(), modGuiId, x, y, z);
                EmbeddedChannel embeddedChannel = channelPair.get(Side.SERVER);
                embeddedChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
                embeddedChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entityPlayerMP);
                embeddedChannel.writeOutbound(openGui);
                entityPlayerMP.openContainer = remoteGuiContainer;
                entityPlayerMP.openContainer.windowId = windowId;
                entityPlayerMP.openContainer.addListener(entityPlayerMP);
                MikuLib.MikuEventBus().post(new net.minecraftforge.event.entity.player.PlayerContainerEvent.Open(entityPlayer, entityPlayer.openContainer));
            }
        } else if (FMLCommonHandler.instance().getSide().equals(Side.CLIENT)) {
            Object guiContainer = NetworkRegistry.INSTANCE.getLocalGuiContainer(mc, entityPlayer, modGuiId, world, x, y, z);
            FMLCommonHandler.instance().showGuiScreen(guiContainer);
        } else {
            FMLLog.log.debug("Invalid attempt to open a local GUI on a dedicated server. This is likely a bug. GUI ID: {},{}", mc.getModId(), modGuiId);
        }

    }
}
