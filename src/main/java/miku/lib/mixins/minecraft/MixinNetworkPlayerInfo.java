package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(value = NetworkPlayerInfo.class)
public abstract class MixinNetworkPlayerInfo {
    @Shadow
    public abstract GameProfile getGameProfile();

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    @Nullable
    public ScorePlayerTeam getPlayerTeam() {
        return ((iMinecraft) Minecraft.getMinecraft()).MikuWorld().getScoreboard().getPlayersTeam(this.getGameProfile().getName());
    }
}
