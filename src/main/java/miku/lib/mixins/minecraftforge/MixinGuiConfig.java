package miku.lib.mixins.minecraftforge;

import miku.lib.client.api.iMinecraft;
import miku.lib.common.core.MikuLib;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiMessageDialog;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = GuiConfig.class, remap = false)
public abstract class MixinGuiConfig extends GuiScreen {
    @Mutable
    @Shadow
    @Final
    public boolean isWorldRunning;

    @Shadow
    @Final
    public String modID;

    @Shadow
    @Final
    @Nullable
    public String configID;

    @Shadow
    @Final
    public GuiScreen parentScreen;

    @Shadow
    public GuiConfigEntries entryList;

    @Shadow
    protected GuiCheckBox chkApplyGlobally;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/GuiScreen;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;Ljava/lang/String;)V")
    public void GuiConfig(GuiScreen parentScreen, List configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2, CallbackInfo ci) {
        this.isWorldRunning = ((iMinecraft) mc).MikuWorld() != null;
    }

    /**
     * @author mcst12345
     * @reason FUCK!
     */
    @Overwrite
    public void actionPerformed(GuiButton button) {
        if (button.id == 2000) {
            boolean flag = true;
            try {
                if ((configID != null || this.parentScreen == null || !(this.parentScreen instanceof GuiConfig))
                        && (this.entryList.hasChangedEntry(true))) {
                    boolean requiresMcRestart = this.entryList.saveConfigElements();

                    if (Loader.isModLoaded(modID)) {
                        ConfigChangedEvent event = new ConfigChangedEvent.OnConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart);
                        MikuLib.MikuEventBus().post(event);
                        if (!event.getResult().equals(Event.Result.DENY))
                            MikuLib.MikuEventBus().post(new ConfigChangedEvent.PostConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart));

                        if (requiresMcRestart) {
                            flag = false;
                            mc.displayGuiScreen(new GuiMessageDialog(parentScreen, "fml.configgui.gameRestartTitle",
                                    new TextComponentString(I18n.format("fml.configgui.gameRestartRequired")), "fml.configgui.confirmRestartMessage"));
                        }

                        if (this.parentScreen instanceof GuiConfig)
                            ((GuiConfig) this.parentScreen).needsRefresh = true;
                    }
                }
            } catch (Throwable e) {
                FMLLog.log.error("Error performing GuiConfig action:", e);
            }

            if (flag)
                this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == 2001) {
            this.entryList.setAllToDefault(this.chkApplyGlobally.isChecked());
        } else if (button.id == 2002) {
            this.entryList.undoAllChanges(this.chkApplyGlobally.isChecked());
        }
    }
}
