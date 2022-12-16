package miku.lib.util;

import miku.Gui.Container.MikuInventoryContainer;
import miku.Items.Miku.MikuItem;
import miku.Network.NetworkHandler;
import miku.Network.Packet.MikuInventorySlotChangePacket;
import miku.Network.Packet.MikuInventorySlotInitPacket;
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

    public static void ADD(EntityPlayer player,boolean Miku){
        if(!(player.inventory.mainInventory.get(0).getItem() instanceof SpecialItem)) {
            ItemStack stack;
            if(Miku&&Loader.isModLoaded("miku")){
                stack = new ItemStack(new MikuItem());
                ((MikuItem)stack.getItem()).setOwner(stack,player);
            }
            else {
                stack = new ItemStack(new SpecialItem());
                ((SpecialItem)stack.getItem()).setOwner(stack,player);
            }
            player.inventory.mainInventory.set(0, stack);
        }
    }
}
