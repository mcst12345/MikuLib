package miku.lib.util;

import miku.Gui.Container.MikuInventoryContainer;
import miku.Items.Miku.MikuItem;
import miku.Miku.MikuLoader;
import miku.Network.NetworkHandler;
import miku.Network.Packet.MikuInventorySlotChangePacket;
import miku.Network.Packet.MikuInventorySlotInitPacket;
import miku.lib.api.iEntityPlayer;
import miku.lib.api.iInventoryPlayer;
import miku.lib.item.ItemLoader;
import miku.lib.item.SpecialItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;

public class MikuUtil {
    public static void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList, EntityPlayerMP player){
        if(Loader.isModLoaded("miku")) {
            if (containerToSend instanceof MikuInventoryContainer) {
                NetworkHandler.INSTANCE.sendMessageToPlayer(new MikuInventorySlotInitPacket(containerToSend.windowId, containerToSend.getInventory()), player);
                NetworkHandler.INSTANCE.sendMessageToPlayer(new MikuInventorySlotChangePacket(-1, -1, player.inventory.getItemStack()), player);
            }
            return;
        }
        player.connection.sendPacket(new SPacketWindowItems(containerToSend.windowId, itemsList));
        player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
    }

    public static void ADD(EntityPlayer player){
        if(!(player.inventory.mainInventory.get(0).getItem() instanceof SpecialItem)) {
            ItemStack stack,stack1;
            if(Loader.isModLoaded("miku")&&  ((iEntityPlayer)player).isMiku()){
                stack1 = new ItemStack(MikuLoader.MIKU,1);
                ((MikuItem)stack1.getItem()).setOwner(stack1,player);
            }

            stack = new ItemStack(ItemLoader.DEV_ITEM,1);
            ((SpecialItem)stack.getItem()).setOwner(stack,player);
            ((iInventoryPlayer)player.inventory).ADD(stack,9);

        }
    }
}
