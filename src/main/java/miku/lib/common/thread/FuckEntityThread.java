package miku.lib.common.thread;

import miku.lib.client.api.iMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;

public class FuckEntityThread extends Thread {
    private final Entity toBeFucked;
    private short count1 = 0;
    private short count2 = 0;

    public FuckEntityThread(Entity toBeFucked) {
        this.toBeFucked = toBeFucked;
    }

    @Override
    public void run() {
        while (count1 <= 10 && (count2 <= 10 || count2 < 0)) {
            World world1 = toBeFucked.world;
            if (world1.loadedEntityList.contains(toBeFucked)) world1.loadedEntityList.remove(toBeFucked);
            else count1++;
            if (Launch.Client) {
                World world2 = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
                if (world2.loadedEntityList.contains(toBeFucked)) world2.loadedEntityList.remove(toBeFucked);
                else count2++;
            } else count2 = -1;
            //TODO (MinecraftServer)
        }
    }
}
