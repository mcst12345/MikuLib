package miku.lib.mixins.minecraft;

import miku.lib.client.api.iMinecraft;
import miku.lib.client.util.TeamSelectionObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.categories.TeleportToTeam;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TeleportToTeam.class)
public abstract class MixinTeleportToTeam {
    @Shadow
    @Final
    private List<ISpectatorMenuObject> items;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        this.items.clear();
        for (ScorePlayerTeam scoreplayerteam : ((iMinecraft) mc).MikuWorld().getScoreboard().getTeams()) {
            this.items.add(new TeamSelectionObject(scoreplayerteam));
        }
    }


}
