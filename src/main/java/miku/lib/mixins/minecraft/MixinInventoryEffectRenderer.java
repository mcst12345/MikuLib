package miku.lib.mixins.minecraft;

import com.google.common.collect.Ordering;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.api.iWorld;
import miku.lib.common.core.MikuLib;
import miku.lib.common.effect.MikuEffect;
import miku.lib.common.util.EntityUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends GuiContainer {
    @Shadow protected boolean hasActivePotionEffects;

    public MixinInventoryEffectRenderer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Inject(at=@At("TAIL"),method = "updateActivePotionEffects")
    protected void updateActivePotionEffects(CallbackInfo ci){
        if (((iWorld) ((iMinecraft) this.mc).MikuPlayer().world).HasEffect(((iMinecraft) this.mc).MikuPlayer()))
            this.hasActivePotionEffects = true;
    }


    /**
     * @author mcst12345
     * @reason For MikuEffect
     */
    @Overwrite
    public void drawActivePotionEffects() {
        int i = this.guiLeft - 124;
        int j = this.guiTop;
        Collection<PotionEffect> collection = ((iMinecraft) this.mc).MikuPlayer().getActivePotionEffects();

        if (!collection.isEmpty()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int l = 33;

            if (collection.size() > 5)
            {
                l = 132 / (collection.size() - 1);
            }

            for (PotionEffect potioneffect : Ordering.natural().sortedCopy(collection))
            {
                Potion potion = potioneffect.getPotion();
                if(!potion.shouldRender(potioneffect)) continue;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
                this.drawTexturedModalRect(i, j, 0, 166, 140, 32);

                if (potion.hasStatusIcon())
                {
                    int i1 = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(i + 6, j + 7, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }

                potion.renderInventoryEffect(potioneffect, this, i, j, this.zLevel);
                if (!potion.shouldRenderInvText(potioneffect)) { j += l; continue; }
                String s1 = I18n.format(potion.getName());

                if (potioneffect.getAmplifier() == 1)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.2");
                }
                else if (potioneffect.getAmplifier() == 2)
                {
                    s1 = s1 + " " + I18n.format("enchantment.level.3");
                }
                else if (potioneffect.getAmplifier() == 3) {
                    s1 = s1 + " " + I18n.format("enchantment.level.4");
                }

                this.fontRenderer.drawStringWithShadow(s1, (float) (i + 10 + 18), (float) (j + 6), 16777215);
                String s = Potion.getPotionDurationString(potioneffect, 1.0F);
                this.fontRenderer.drawStringWithShadow(s, (float) (i + 10 + 18), (float) (j + 6 + 10), 8355711);
                j += l;
            }
        }
        if (((iWorld) ((iMinecraft) this.mc).MikuPlayer().world).HasEffect(((iMinecraft) this.mc).MikuPlayer())) {
            for (MikuEffect effect : ((iWorld) ((iMinecraft) this.mc).MikuPlayer().world).GetEntityEffects(((iMinecraft) this.mc).MikuPlayer())) {
                if (effect.getTEXTURE() == null) continue;
                this.mc.getTextureManager().bindTexture(effect.getTEXTURE());
                this.drawTexturedModalRect(i + 6, j + 7, 0, 0, 72, 18);

            }
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK!!!!!
     */
    @Overwrite
    public void updateActivePotionEffects() {
        if (EntityUtil.isProtected(this.mc)) {
            return;
        }
        boolean hasVisibleEffect = false;
        for (PotionEffect potioneffect : ((iMinecraft) this.mc).MikuPlayer().getActivePotionEffects()) {
            Potion potion = potioneffect.getPotion();
            if (potion.shouldRender(potioneffect)) {
                hasVisibleEffect = true;
                break;
            }
        }
        if (((iMinecraft) this.mc).MikuPlayer().getActivePotionEffects().isEmpty() || !hasVisibleEffect) {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        } else {
            if (MikuLib.MikuEventBus.post(new net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent(this)))
                this.guiLeft = (this.width - this.xSize) / 2;
            else
                this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
            this.hasActivePotionEffects = true;
        }
    }
}
