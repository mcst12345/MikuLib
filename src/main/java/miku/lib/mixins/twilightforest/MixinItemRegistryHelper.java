package miku.lib.mixins.twilightforest;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import twilightforest.item.ItemBlockTFMeta;
import twilightforest.item.RegisterItemEvent;

@Mixin(value = RegisterItemEvent.ItemRegistryHelper.class, remap = false)
public class MixinItemRegistryHelper {
    @Shadow
    @Final
    private IForgeRegistry<Item> registry;

    /**
     * @author mcst12345
     * @reason Where does the NPE come?
     */
    @Overwrite
    <T extends ItemBlock> void register(T item) {
        if (item == null) return;
        if (item.getBlockRaw() == null) {
            System.out.println("Cannot get block of item:" + item.getClass());
            return;
        }
        try {
            ResourceLocation tmp = item.getBlockRaw().getRegistryName();
            if (tmp != null) item.setRegistryName(tmp);
            try {
                item.setTranslationKey(item.getBlock().getTranslationKey());
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at item.setTranslationKey");
                t.printStackTrace();
            }
            try {
                this.registry.register(item);
            } catch (Throwable t) {
                System.out.println("MikuWarn:Catch exception at registry.register");
                t.printStackTrace();
            }
        } catch (Throwable t) {
            System.out.println(item.getBlock().getClass());

            t.printStackTrace();
        }
    }

    /**
     * @author mcst12345
     * @reason Where does the null come from?
     */
    @Overwrite
    void registerSubItemBlock(Block block, boolean shouldAppendNumber) {
        if (block == null) {
            System.out.println("The fuck?");
            return;
        }
        this.register((new ItemBlockTFMeta(block)).setAppend(shouldAppendNumber));
    }
}
