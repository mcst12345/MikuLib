package miku.lib.item;

import miku.lib.util.Register;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class ItemLoader {
    public static final Item DEV_ITEM = new SpecialItem();
    public static final Item MARENOL = new MARENOL();


    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event){
        Register.RegisterItem(event,DEV_ITEM,"developer");
        Register.RegisterItem(event,MARENOL,"marenol");
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerItemModel(ModelRegistryEvent event){
        Register.RegisterItemModel(DEV_ITEM);
        Register.RegisterItemModel(MARENOL);
    }
}
