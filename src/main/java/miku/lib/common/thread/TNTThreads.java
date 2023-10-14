package miku.lib.common.thread;

import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

public class TNTThreads extends Thread {
    private static short tmp = 0;
    private static final TNTThreads INSTANCE1 = new TNTThreads(0);
    private static final TNTThreads INSTANCE2 = new TNTThreads(1);
    private static final TNTThreads INSTANCE3 = new TNTThreads(2);
    private static final TNTThreads INSTANCE4 = new TNTThreads(3);
    private boolean running;

    private TNTThreads(int id) {
        this.setName("TNT-Thread-" + id);
    }

    private final List<Explosion> lists = new ArrayList<>();

    public static synchronized void AddExplosion(Explosion explosion) {
        System.out.println("This is a test message! current:" + tmp);
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
                break;
            default:
                INSTANCE1.lists.add(explosion);
                tmp = 1;
        }
    }

    @Override
    public synchronized void run() {
        System.out.println("TNTThread is running.");
        while (true) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning() && !TimeStopUtil.isTimeStop() && !TimeStopUtil.isSaving()) {
                if (lists.isEmpty()) {
                    return;
                }
                running = true;
                for (Explosion explosion : lists) {
                    boolean flag = explosion.world.isRemote;
                    if (!flag) {
                        explosion.doExplosionA();
                    }
                    explosion.doExplosionB(flag);
                }
                lists.clear();
                System.out.println("Completed a list of explosions.");
                running = false;
            }
        }
    }

    static {
        System.out.println("Starting TNT Threads.");
        INSTANCE1.start();
        INSTANCE2.start();
        INSTANCE3.start();
        INSTANCE4.start();
    }

    public static boolean isRunning() {
        return INSTANCE1.running || INSTANCE2.running || INSTANCE3.running || INSTANCE4.running;
    }
}
