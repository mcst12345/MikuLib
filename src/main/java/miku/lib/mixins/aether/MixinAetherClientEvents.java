package miku.lib.mixins.aether;

import com.gildedgames.the_aether.AetherConfig;
import com.gildedgames.the_aether.api.AetherAPI;
import com.gildedgames.the_aether.client.AetherClientEvents;
import com.gildedgames.the_aether.client.gui.button.*;
import com.gildedgames.the_aether.client.gui.inventory.GuiAccessories;
import com.gildedgames.the_aether.client.gui.menu.AetherMainMenu;
import com.gildedgames.the_aether.client.gui.menu.GuiMenuToggleButton;
import com.gildedgames.the_aether.networking.AetherNetworkingManager;
import com.gildedgames.the_aether.networking.packets.*;
import com.gildedgames.the_aether.player.PlayerAether;
import com.gildedgames.the_aether.player.perks.AetherRankings;
import com.gildedgames.the_aether.player.perks.util.EnumAetherPerkType;
import com.gildedgames.the_aether.universal.fastcrafting.FastCraftingUtil;
import com.gildedgames.the_aether.universal.pixelmon.PixelmonUtil;
import miku.lib.client.api.iGuiContainer;
import miku.lib.client.api.iGuiScreen;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(value = AetherClientEvents.class, remap = false)
public abstract class MixinAetherClientEvents {
    @Shadow
    @Final
    private static GuiAccessoryButton ACCESSORY_BUTTON;

    @Shadow
    private static int previousSelectedTabIndex;

    @Shadow
    private static boolean shouldRemoveButton;

    @Shadow
    @Final
    private static GuiMenuToggleButton MAIN_MENU_BUTTON;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private static boolean canOpenAccessories;

    /**
     * @author mcst12345
     * @reason No reflection
     */
    @SubscribeEvent
    @Overwrite
    public void onScreenOpened(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiContainer) {
            EntityPlayer player = ((iMinecraft) Minecraft.getMinecraft()).MikuPlayer();
            Class<?> clazz = event.getGui().getClass();
            int guiLeft = ((iGuiContainer) event.getGui()).guiLeft();
            int guiTop = ((iGuiContainer) event.getGui()).guiTop();
            if (player.capabilities.isCreativeMode) {
                if (event.getGui() instanceof GuiContainerCreative && ((GuiContainerCreative) event.getGui()).getSelectedTabIndex() == CreativeTabs.INVENTORY.getIndex()) {
                    event.getButtonList().add(ACCESSORY_BUTTON.setPosition(guiLeft + 73, guiTop + 38));
                    previousSelectedTabIndex = CreativeTabs.INVENTORY.getIndex();
                }
            } else if (clazz == GuiInventory.class || FastCraftingUtil.isOverridenGUI(clazz) || PixelmonUtil.isOverridenInventoryGUI(clazz)) {
                event.getButtonList().add(ACCESSORY_BUTTON.setPosition(guiLeft + 26, guiTop + 65));
            }

            if (clazz == GuiAccessories.class) {
                if (!shouldRemoveButton) {
                    event.getButtonList().add(ACCESSORY_BUTTON.setPosition(guiLeft + 8, guiTop + 65));
                } else {
                    shouldRemoveButton = false;
                }
            }
        }

        if (AetherConfig.visual_options.menu_button && event.getGui() instanceof GuiMainMenu) {
            event.getButtonList().add(MAIN_MENU_BUTTON.setPosition(event.getGui().width - 24, 4));
        }

        if (AetherConfig.visual_options.menu_enabled && event.getGui().getClass() == GuiMainMenu.class) {
            Minecraft.getMinecraft().displayGuiScreen(new AetherMainMenu());
        }

        if (event.getGui().getClass() == GuiCustomizeSkin.class && ((iMinecraft) Minecraft.getMinecraft()).MikuPlayer() != null) {
            int i = 8;
            Iterator<GuiButton> var7 = event.getButtonList().iterator();

            GuiButton button;
            while (var7.hasNext()) {
                button = var7.next();
                if (button.id == 200) {
                    button.y += 48;
                }
            }

            event.getButtonList().add(new GuiGlovesButton(event.getGui().width / 2 - 155, event.getGui().height / 6 + 24 * (i >> 1)));
            ++i;
            event.getButtonList().add(new GuiGloveSizeButton(event.getGui().width / 2 - 155 + i % 2 * 160, event.getGui().height / 6 + 24 * (i >> 1)));
            ++i;
            event.getButtonList().add(new GuiCapeButton(event.getGui().width / 2 - 155, event.getGui().height / 6 + 24 * (i >> 1)));
            if (AetherRankings.isRankedPlayer(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer().getUniqueID())) {
                ++i;
                event.getButtonList().add(new GuiHaloButton(event.getGui().width / 2 - 155 + i % 2 * 160, event.getGui().height / 6 + 24 * (i >> 1)));
                if (AetherRankings.isDeveloper(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer().getUniqueID())) {
                    var7 = event.getButtonList().iterator();

                    while (var7.hasNext()) {
                        button = var7.next();
                        if (button.id == 200) {
                            button.y += 24;
                        }
                    }

                    ++i;
                    event.getButtonList().add(new GuiGlowButton(event.getGui().width / 2 - 155 + i % 2 * 160, event.getGui().height / 6 + 24 * (i >> 1)));
                }
            }
        }

    }

    /**
     * @author mcst12345
     * @reason No reflection
     */
    @SubscribeEvent
    @Overwrite
    public void onMouseClicked(GuiScreenEvent.MouseInputEvent.Post event) {
        if (event.getGui() instanceof GuiContainerCreative) {
            GuiContainerCreative guiScreen = (GuiContainerCreative) event.getGui();
            List<GuiButton> buttonList = ((iGuiScreen) event.getGui()).buttonList();
            if (previousSelectedTabIndex != guiScreen.getSelectedTabIndex()) {
                if (guiScreen.getSelectedTabIndex() == CreativeTabs.INVENTORY.getIndex() && !buttonList.contains(ACCESSORY_BUTTON)) {
                    int guiLeft = ((iGuiContainer) event.getGui()).guiLeft();
                    int guiTop = ((iGuiContainer) event.getGui()).guiTop();
                    buttonList.add(ACCESSORY_BUTTON.setPosition(guiLeft + 73, guiTop + 38));
                } else if (previousSelectedTabIndex == CreativeTabs.INVENTORY.getIndex()) {
                    buttonList.remove(ACCESSORY_BUTTON);
                }

                previousSelectedTabIndex = guiScreen.getSelectedTabIndex();
            }
        }

    }

    /**
     * @author mcst12345
     * @reason No reflection
     */
    @Overwrite
    @SubscribeEvent
    public void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        Class<?> clazz = event.getGui().getClass();
        if (clazz == GuiInventory.class && event.getButton().getClass() == GuiButtonImage.class && event.getButton().id == 10) {
            int guiLeft = ((iGuiContainer) event.getGui()).guiLeft();
            int guiTop = ((iGuiContainer) event.getGui()).guiTop();
            if (this.mc.currentScreen != null) {
                ACCESSORY_BUTTON.setPosition(this.mc.currentScreen.width - guiLeft - 74, guiTop + 65);
                canOpenAccessories = false;
            }
        }

        if (clazz != GuiAccessories.class && event.getButton().id == 18067 && canOpenAccessories) {
            AetherNetworkingManager.sendToServer(new PacketOpenContainer(1));
        }

        PlayerAether player;
        boolean gloveSize;
        if (event.getButton().getClass() == GuiHaloButton.class) {
            player = (PlayerAether) AetherAPI.getInstance().get(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer());
            gloveSize = !player.shouldRenderHalo;
            player.shouldRenderHalo = gloveSize;
            AetherNetworkingManager.sendToServer(new PacketPerkChanged(player.getEntity().getEntityId(), EnumAetherPerkType.Halo, player.shouldRenderHalo));
        }

        if (event.getButton().getClass() == GuiGlowButton.class) {
            player = (PlayerAether) AetherAPI.getInstance().get(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer());
            gloveSize = !player.shouldRenderGlow;
            player.shouldRenderGlow = gloveSize;
            AetherNetworkingManager.sendToServer(new PacketPerkChanged(player.getEntity().getEntityId(), EnumAetherPerkType.Glow, player.shouldRenderGlow));
        }

        if (event.getButton().getClass() == GuiCapeButton.class) {
            player = (PlayerAether) AetherAPI.getInstance().get(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer());
            gloveSize = !player.shouldRenderCape;
            player.shouldRenderCape = gloveSize;
            AetherNetworkingManager.sendToServer(new PacketCapeChanged(player.getEntity().getEntityId(), player.shouldRenderCape));
        }

        if (event.getButton().getClass() == GuiGlovesButton.class) {
            player = (PlayerAether) AetherAPI.getInstance().get(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer());
            gloveSize = !player.shouldRenderGloves;
            player.shouldRenderGloves = gloveSize;
            AetherNetworkingManager.sendToServer(new PacketGlovesChanged(player.getEntity().getEntityId(), player.shouldRenderGloves));
        }

        if (event.getButton().getClass() == GuiGloveSizeButton.class) {
            player = (PlayerAether) AetherAPI.getInstance().get(((iMinecraft) Minecraft.getMinecraft()).MikuPlayer());
            gloveSize = !player.gloveSize;
            player.gloveSize = gloveSize;
            AetherNetworkingManager.sendToServer(new PacketGloveSizeChanged(player.getEntity().getEntityId(), player.gloveSize));
        }

    }
}
