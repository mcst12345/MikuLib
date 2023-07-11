package miku.lib.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import miku.lib.item.SpecialItem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

@Mixin(value = MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow private int tickCounter;

    @Shadow private boolean startProfiling;

    @Shadow @Final public Profiler profiler;

    @Shadow public abstract void updateTimeLightAndEntities();

    @Shadow private long nanoTimeSinceStatusRefresh;

    @Shadow @Final private ServerStatusResponse statusResponse;

    @Shadow @Final private Random random;

    @Shadow public abstract int getMaxPlayers();

    @Shadow public abstract int getCurrentPlayerCount();

    @Shadow private PlayerList playerList;

    @Shadow public abstract void saveAllWorlds(boolean isSilent);

    @Shadow @Final public long[] tickTimeArray;

    @Shadow @Final private Snooper usageSnooper;

    /**
     * @author mcst12345
     * @reason F
     */
    @Overwrite
    public void tick()
    {
        long i = System.nanoTime();
        if(!SpecialItem.isTimeStop())net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreServerTick();
        if(!SpecialItem.isTimeStop())++this.tickCounter;

        if (this.startProfiling)
        {
            this.startProfiling = false;
            this.profiler.profilingEnabled = true;
            this.profiler.clearProfiling();
        }

        this.profiler.startSection("root");
        if(!SpecialItem.isTimeStop())this.updateTimeLightAndEntities();

        if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L && !SpecialItem.isTimeStop())
        {
            this.nanoTimeSinceStatusRefresh = i;
            this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
            int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k)
            {
                agameprofile[k] = this.playerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.statusResponse.getPlayers().setPlayers(agameprofile);
            this.statusResponse.invalidateJson();
        }

        if (this.tickCounter % 900 == 0)
        {
            this.profiler.startSection("save");
            this.playerList.saveAllPlayerData();
            this.saveAllWorlds(true);
            this.profiler.endSection();
        }

        this.profiler.startSection("tallying");
        if(!SpecialItem.isTimeStop())this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
        this.profiler.endSection();
        this.profiler.startSection("snooper");

        if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100 && !SpecialItem.isTimeStop())
        {
            this.usageSnooper.startSnooper();
        }

        if (this.tickCounter % 6000 == 0 && !SpecialItem.isTimeStop())
        {
            this.usageSnooper.addMemoryStatsToSnooper();
        }

        this.profiler.endSection();
        this.profiler.endSection();
        if(!SpecialItem.isTimeStop())net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostServerTick();
    }
}
