package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Lists;
import miku.lib.common.core.MikuLib;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@Mixin(value = OreDictionary.class, remap = false)
public abstract class MixinOreDictionary {
    @Shadow
    public static int getOreID(String name) {
        return 0;
    }

    @Shadow
    @Final
    public static int WILDCARD_VALUE;

    @Shadow
    private static Map<Integer, List<Integer>> stackToId;

    @Shadow
    private static List<NonNullList<ItemStack>> idToStack;

    /**
     * @author mcst12345
     * @reason FUCK!!!
     */
    @Overwrite
    private static void registerOreImpl(String name, @Nonnull ItemStack ore) {
        if ("Unknown".equals(name)) return; //prevent bad IDs.
        if (ore.isEmpty()) {
            FMLLog.bigWarning("Invalid registration attempt for an Ore Dictionary item with name {} has occurred. The registration has been denied to prevent crashes. The mod responsible for the registration needs to correct this.", name);
            return; //prevent bad ItemStacks.
        }

        int oreID = getOreID(name);
        // HACK: use the registry name's ID. It is unique and it knows about substitutions. Fallback to a -1 value (what Item.getIDForItem would have returned) in the case where the registry is not aware of the item yet
        // IT should be noted that -1 will fail the gate further down, if an entry already exists with value -1 for this name. This is what is broken and being warned about.
        // APPARENTLY it's quite common to do this. OreDictionary should be considered alongside Recipes - you can't make them properly until you've registered with the game.
        ResourceLocation registryName = ore.getItem().delegate.name();
        int hash;
        if (registryName == null) {
            ModContainer modContainer = Loader.instance().activeModContainer();
            String modContainerName = modContainer == null ? null : modContainer.getName();
            FMLLog.bigWarning("A broken ore dictionary registration with name {} has occurred. It adds an item (type: {}) which is currently unknown to the game registry. This dictionary item can only support a single value when"
                    + " registered with ores like this, and NO I am not going to turn this spam off. Just register your ore dictionary entries after the GameRegistry.\n"
                    + "TO USERS: YES this is a BUG in the mod " + modContainerName + " report it to them!", name, ore.getItem().getClass());
            hash = -1;
        } else {
            hash = Item.REGISTRY.getIDForObject(ore.getItem().delegate.get());
        }
        if (ore.getItemDamage() != WILDCARD_VALUE) {
            hash |= ((ore.getItemDamage() + 1) << 16); // +1 so 0 is significant
        }

        //Add things to the baked version, and prevent duplicates
        List<Integer> ids = stackToId.get(hash);
        if (ids != null && ids.contains(oreID)) return;
        if (ids == null) {
            ids = Lists.newArrayList();
            stackToId.put(hash, ids);
        }
        ids.add(oreID);

        //Add to the unbaked version
        ore = ore.copy();
        idToStack.get(oreID).add(ore);
        MikuLib.MikuEventBus().post(new OreDictionary.OreRegisterEvent(name, ore));
    }
}
