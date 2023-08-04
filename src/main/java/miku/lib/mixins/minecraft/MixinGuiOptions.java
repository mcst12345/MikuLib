package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiOptions.class)
public abstract class MixinGuiOptions extends GuiScreen {
    @Shadow
    protected String title;

    @Shadow
    @Final
    private static GameSettings.Options[] SCREEN_OPTIONS;

    @Shadow
    private GuiButton difficultyButton;

    @Shadow
    private GuiLockIconButton lockButton;

    @Shadow
    @Final
    private GameSettings settings;

    @Shadow
    public abstract String getDifficultyText(EnumDifficulty p_175355_1_);

    @Shadow
    @Final
    private GuiScreen lastScreen;

    /**
     * @author mcst12345
     * @reason Fuck
     */
    @Overwrite
    public void initGui() {
        this.title = I18n.format("options.title");
        int i = 0;

        for (GameSettings.Options gamesettings$options : SCREEN_OPTIONS) {
            if (gamesettings$options.isFloat()) {
                this.buttonList.add(new GuiOptionSlider(gamesettings$options.getOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), gamesettings$options));
            } else {
                GuiOptionButton guioptionbutton = new GuiOptionButton(gamesettings$options.getOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), gamesettings$options, this.settings.getKeyBinding(gamesettings$options));
                this.buttonList.add(guioptionbutton);
            }

            ++i;
        }

        if (((iMinecraft) (this.mc)).MikuWorld() != null) {
            EnumDifficulty enumdifficulty = ((iMinecraft) (this.mc)).MikuWorld().getDifficulty();
            this.difficultyButton = new GuiButton(108, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), 150, 20, this.getDifficultyText(enumdifficulty));
            this.buttonList.add(this.difficultyButton);

            if (this.mc.isSingleplayer() && !((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isHardcoreModeEnabled()) {
                this.difficultyButton.setWidth(this.difficultyButton.getButtonWidth() - 20);
                this.lockButton = new GuiLockIconButton(109, this.difficultyButton.x + this.difficultyButton.getButtonWidth(), this.difficultyButton.y);
                this.buttonList.add(this.lockButton);
                this.lockButton.setLocked(((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().isDifficultyLocked());
                this.lockButton.enabled = !this.lockButton.isLocked();
                this.difficultyButton.enabled = !this.lockButton.isLocked();
            } else {
                this.difficultyButton.enabled = false;
            }
        } else {
            this.buttonList.add(new GuiOptionButton(GameSettings.Options.REALMS_NOTIFICATIONS.getOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), GameSettings.Options.REALMS_NOTIFICATIONS, this.settings.getKeyBinding(GameSettings.Options.REALMS_NOTIFICATIONS)));
        }

        this.buttonList.add(new GuiButton(110, this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, I18n.format("options.skinCustomisation")));
        this.buttonList.add(new GuiButton(106, this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, I18n.format("options.sounds")));
        this.buttonList.add(new GuiButton(101, this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.video")));
        this.buttonList.add(new GuiButton(100, this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, I18n.format("options.controls")));
        this.buttonList.add(new GuiButton(102, this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.language")));
        this.buttonList.add(new GuiButton(103, this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, I18n.format("options.chat.title")));
        this.buttonList.add(new GuiButton(105, this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.resourcepack")));
        this.buttonList.add(new GuiButton(104, this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, I18n.format("options.snooper.view")));
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
    }

    /**
     * @author mcst12345
     * @reason HolyFuck
     */
    @Overwrite
    public void confirmClicked(boolean result, int id) {
        this.mc.displayGuiScreen(this);

        if (id == 109 && result && ((iMinecraft) (this.mc)).MikuWorld() != null) {
            ((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().setDifficultyLocked(true);
            this.lockButton.setLocked(true);
            this.lockButton.enabled = false;
            this.difficultyButton.enabled = false;
        }
    }

    /**
     * @author mcst12345
     * @reason FUCK
     */
    @Overwrite
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id < 100 && button instanceof GuiOptionButton) {
                GameSettings.Options gamesettings$options = ((GuiOptionButton) button).getOption();
                this.settings.setOptionValue(gamesettings$options, 1);
                button.displayString = this.settings.getKeyBinding(GameSettings.Options.byOrdinal(button.id));
            }

            if (button.id == 108) {
                ((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().setDifficulty(EnumDifficulty.byId(((iMinecraft) (this.mc)).MikuWorld().getDifficulty().getId() + 1));
                this.difficultyButton.displayString = this.getDifficultyText(((iMinecraft) (this.mc)).MikuWorld().getDifficulty());
            }

            if (button.id == 109) {
                this.mc.displayGuiScreen(new GuiYesNo(this, (new TextComponentTranslation("difficulty.lock.title")).getFormattedText(), (new TextComponentTranslation("difficulty.lock.question", new TextComponentTranslation(((iMinecraft) (this.mc)).MikuWorld().getWorldInfo().getDifficulty().getTranslationKey()))).getFormattedText(), 109));
            }

            if (button.id == 110) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiCustomizeSkin(this));
            }

            if (button.id == 101) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiVideoSettings(this, this.settings));
            }

            if (button.id == 100) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiControls(this, this.settings));
            }

            if (button.id == 102) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiLanguage(this, this.settings, this.mc.getLanguageManager()));
            }

            if (button.id == 103) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new ScreenChatOptions(this, this.settings));
            }

            if (button.id == 104) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiSnooper(this, this.settings));
            }

            if (button.id == 200) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(this.lastScreen);
            }

            if (button.id == 105) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiScreenResourcePacks(this));
            }

            if (button.id == 106) {
                this.mc.gameSettings.saveOptions();
                this.mc.displayGuiScreen(new GuiScreenOptionsSounds(this, this.settings));
            }
        }
    }
}
