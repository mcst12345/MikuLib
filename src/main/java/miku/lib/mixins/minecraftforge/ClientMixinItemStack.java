package miku.lib.mixins.minecraftforge;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import miku.lib.common.core.MikuLib;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.item.ItemStack.DECIMALFORMAT;

@Mixin(value = ItemStack.class)
public abstract class ClientMixinItemStack {
    @Shadow
    public abstract String getDisplayName();

    @Shadow
    public abstract boolean hasDisplayName();

    @Shadow
    @Final
    private Item item;

    @Shadow
    public abstract boolean getHasSubtypes();

    @Shadow
    int itemDamage;

    @Shadow
    public abstract boolean hasTagCompound();

    @Shadow
    private NBTTagCompound stackTagCompound;

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract NBTTagList getEnchantmentTagList();

    @Shadow
    public abstract Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot);

    @Shadow
    @Nullable
    public abstract NBTTagCompound getTagCompound();

    @Shadow
    public abstract boolean isItemDamaged();

    @Shadow
    public abstract int getMaxDamage();

    @Shadow
    public abstract int getItemDamage();

    /**
     * @author mcst12345
     * @reason Let's all love lain
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public List<String> getTooltip(@Nullable EntityPlayer playerIn, ITooltipFlag advanced) {
        List<String> list = Lists.newArrayList();
        String s = this.getDisplayName();

        if (this.hasDisplayName()) {
            s = TextFormatting.ITALIC + s;
        }

        s = s + TextFormatting.RESET;

        if (advanced.isAdvanced()) {
            String s1 = "";

            if (!s.isEmpty()) {
                s = s + " (";
                s1 = ")";
            }

            int i = Item.getIdFromItem(this.item);

            if (this.getHasSubtypes()) {
                s = s + String.format("#%04d/%d%s", i, this.itemDamage, s1);
            } else {
                s = s + String.format("#%04d%s", i, s1);
            }
        } else if (!this.hasDisplayName() && this.item == Items.FILLED_MAP) {
            s = s + " #" + this.itemDamage;
        }

        list.add(s);
        int i1 = 0;

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("HideFlags", 99)) {
            i1 = this.stackTagCompound.getInteger("HideFlags");
        }

        if ((i1 & 32) == 0) {
            this.getItem().addInformation((ItemStack) (Object) this, playerIn == null ? null : playerIn.world, list, advanced);
        }

        if (this.hasTagCompound()) {
            if ((i1 & 1) == 0) {
                NBTTagList nbttaglist = this.getEnchantmentTagList();

                for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                    NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);
                    int k = nbttagcompound.getShort("id");
                    int l = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.getEnchantmentByID(k);

                    if (enchantment != null) {
                        list.add(enchantment.getTranslatedName(l));
                    }
                }
            }

            if (this.stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound1 = this.stackTagCompound.getCompoundTag("display");

                if (nbttagcompound1.hasKey("color", 3)) {
                    if (advanced.isAdvanced()) {
                        list.add(I18n.translateToLocalFormatted("item.color", String.format("#%06X", nbttagcompound1.getInteger("color"))));
                    } else {
                        list.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound1.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist3 = nbttagcompound1.getTagList("Lore", 8);

                    if (!nbttaglist3.isEmpty()) {
                        for (int l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
                            list.add(TextFormatting.DARK_PURPLE + String.valueOf(TextFormatting.ITALIC) + nbttaglist3.getStringTagAt(l1));
                        }
                    }
                }
            }
        }

        for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
            Multimap<String, AttributeModifier> multimap = this.getAttributeModifiers(entityequipmentslot);

            if (!multimap.isEmpty() && (i1 & 2) == 0) {
                list.add("");
                list.add(I18n.translateToLocal("item.modifiers." + entityequipmentslot.getName()));

                for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    double d0 = attributemodifier.getAmount();
                    boolean flag = false;

                    if (playerIn != null) {
                        if (attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                            d0 = d0 + playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            d0 = d0 + (double) EnchantmentHelper.getModifierForCreature((ItemStack) (Object) this, EnumCreatureAttribute.UNDEFINED);
                            flag = true;
                        } else if (attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER) {
                            d0 += playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                            flag = true;
                        }
                    }

                    double d1;

                    if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                        d1 = d0;
                    } else {
                        d1 = d0 * 100.0D;
                    }

                    if (flag) {
                        list.add(" " + I18n.translateToLocalFormatted("attribute.modifier.equals." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    } else if (d0 > 0.0D) {
                        list.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    } else if (d0 < 0.0D) {
                        d1 = d1 * -1.0D;
                        list.add(TextFormatting.RED + " " + I18n.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    }
                }
            }
        }

        if (this.hasTagCompound() && this.getTagCompound().getBoolean("Unbreakable") && (i1 & 4) == 0) {
            list.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));
        }

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
            NBTTagList nbttaglist1 = this.stackTagCompound.getTagList("CanDestroy", 8);

            if (!nbttaglist1.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

                for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
                    Block block = Block.getBlockFromName(nbttaglist1.getStringTagAt(j1));

                    if (block != null) {
                        list.add(TextFormatting.DARK_GRAY + block.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
            NBTTagList nbttaglist2 = this.stackTagCompound.getTagList("CanPlaceOn", 8);

            if (!nbttaglist2.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

                for (int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    Block block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));

                    if (block1 != null) {
                        list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (advanced.isAdvanced()) {
            if (this.isItemDamaged()) {
                list.add(I18n.translateToLocalFormatted("item.durability", this.getMaxDamage() - this.getItemDamage(), this.getMaxDamage()));
            }

            list.add(TextFormatting.DARK_GRAY + Item.REGISTRY.getNameForObject(this.item).toString());

            if (this.hasTagCompound()) {
                list.add(TextFormatting.DARK_GRAY + I18n.translateToLocalFormatted("item.nbt_tags", this.getTagCompound().getKeySet().size()));
            }
        }

        net.minecraftforge.event.ForgeEventFactory.onItemTooltip((ItemStack) (Object) this, playerIn, list, advanced);
        if (MikuLib.isLAIN()) {
            List<String> lain = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                lain.add("Let's all love Lain");
            }
            return lain;
        }
        return list;
    }
}
