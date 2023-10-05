package miku.lib.common.thread;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TNTThreads extends Thread {
    private static short tmp = 0;
    public static TNTThreads INSTANCE1 = new TNTThreads(0);
    public static TNTThreads INSTANCE2 = new TNTThreads(1);
    public static TNTThreads INSTANCE3 = new TNTThreads(2);
    public static TNTThreads INSTANCE4 = new TNTThreads(3);

    private TNTThreads(int id) {
        this.setName("TNT-Thread-" + id);
    }

    private final ObjectLinkedOpenHashSet<Explosion> lists = new ObjectLinkedOpenHashSet<>();

    public static synchronized void AddExplosion(Explosion explosion) {
        switch (tmp) {
            case 0:
                INSTANCE1.lists.add(explosion);
                break;
            case 1:
                INSTANCE2.lists.add(explosion);
                break;
            case 2:
                INSTANCE3.lists.add(explosion);
                break;
            case 3:
                INSTANCE4.lists.add(explosion);
            default:
                throw new RuntimeException("The fuck?");
        }
        tmp = tmp > 3 ? 0 : ++tmp;
    }

    @Override
    public void run() {
        System.out.println("TNTThread is running.");
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

    public static void init() {
        System.out.println("Starting TNT Threads.");
        INSTANCE1.start();
        INSTANCE2.start();
        INSTANCE3.start();
        INSTANCE4.start();
    }
}
