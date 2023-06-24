package miku.lib.util;

import miku.Miku.Miku;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class Register {
    public static void RegisterItem(RegistryEvent.Register<Item> event, Item item, String name) {
        RegisterItem(event,item,"miku",name);
    }

    public static void RegisterItem(RegistryEvent.Register<Item> event, Item item,String mod, String name){
        event.getRegistry().register(item.setRegistryName(mod + ":" + name));
    }

    @SideOnly(Side.CLIENT)
    public static void RegisterItemModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory"));
    }

    public static void RegisterEnchantment(RegistryEvent.Register<Enchantment> event, Enchantment enchantment, String name) {
        RegisterEnchantment(event,enchantment,"miku",name);
    }

    public static void RegisterEnchantment(RegistryEvent.Register<Enchantment> event, Enchantment enchantment, String mod, String name){
        event.getRegistry().register(enchantment.setName(name).setRegistryName(mod + ":" + name));
    }
    public static void RegisterBlock(RegistryEvent.Register<Block> event, Block block, String name) {
        RegisterBlock(event,block,"miku",name);
    }

    public static void RegisterBlock(RegistryEvent.Register<Block> event, Block block, String mod, String name){
        event.getRegistry().register(block.setRegistryName(mod+ ":" + name));
    }

    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event, String inside_name, String name, int network, Class<? extends Entity> c) {
        RegisterEntity(event,"miku",inside_name,name,network,c);
    }

    public static void RegisterEntity(RegistryEvent.Register<EntityEntry> event,String mod, String inside_name, String name, int network, Class<? extends Entity> c){
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(c)
                .id(new ResourceLocation(mod, inside_name), network)
                .name(name)
                .tracker(80, 3, false)
                .build()
        );
    }

    public static void RegisterEntity(String entity,Class<? extends Entity> c,String name,int id){
        EntityRegistry.registerModEntity(new ResourceLocation("miku",entity), c,name,id, Miku.INSTANCE,80,3,true);
    }
    public static void RegisterEntity(String mod,String entity,Class<? extends Entity> c,String name,int id,Object MOD){
        EntityRegistry.registerModEntity(new ResourceLocation(mod,entity), c,name,id, MOD,80,3,true);
    }
}
