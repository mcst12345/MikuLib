package miku.lib.common.thread;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TNTThreads extends Thread {
    public static TNTThreads INSTANCE = new TNTThreads();

    private TNTThreads() {
        this.setName("TNT-Thread");
        this.setPriority(3);
    }

    private static final ObjectLinkedOpenHashSet<Explosion> lists = new ObjectLinkedOpenHashSet<>();

    public static void AddExplosion(Explosion explosion) {
        lists.add(explosion);
    }

    @Override
    public void run() {
        while (true) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance().serverIsInRunLoop() && !TimeStopUtil.isTimeStop() && !TimeStopUtil.isSaving()) {
                if (lists.isEmpty()) {
                    return;
                }
                for (Explosion explosion : lists) {
                    explosion.doExplosionA();
                    explosion.doExplosionB(true);
                }
                lists.clear();
            }
        }
    }

    static {
        INSTANCE.start();
    }
}
