package miku.lib.common.thread;

import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.List;

public class ServerTNTThreads extends Thread {
    private static short tmp = 0;
    private static final ServerTNTThreads INSTANCE1 = new ServerTNTThreads(0);
    private static final ServerTNTThreads INSTANCE2 = new ServerTNTThreads(1);
    private static final ServerTNTThreads INSTANCE3 = new ServerTNTThreads(2);
    private static final ServerTNTThreads INSTANCE4 = new ServerTNTThreads(3);

    private ServerTNTThreads(int id) {
        this.setName("Server-TNT-Thread-" + id);
    }

    private final List<Explosion> lists = new ArrayList<>();

    public static void AddExplosion(Explosion explosion) {
        System.out.println("This is a test message! current:" + tmp);
        switch (tmp) {
            case 0:
                synchronized (INSTANCE1.lists) {
                    INSTANCE1.lists.add(explosion);
                }
                break;
            case 1:
                synchronized (INSTANCE2.lists) {
                    INSTANCE2.lists.add(explosion);
                }
                break;
            case 2:
                synchronized (INSTANCE3.lists) {
                    INSTANCE3.lists.add(explosion);
                }
                break;
            case 3:
                synchronized (INSTANCE4.lists) {
                    INSTANCE4.lists.add(explosion);
                }
                break;
            default:
                synchronized (INSTANCE1.lists) {
                    INSTANCE1.lists.add(explosion);
                }
                tmp = 1;
                return;
        }
        tmp++;
    }

    @Override
    public void run() {
        System.out.println("TNTThread is running.");
        while (true) {
            if (!lists.isEmpty()) {
                for (Explosion explosion : lists) {
                    explosion.doExplosionA();
                    explosion.doExplosionB(false);
                }
                lists.clear();
            }
        }
    }

    static {
        System.out.println("Starting Server TNT Threads.");
        INSTANCE1.start();
        INSTANCE2.start();
        INSTANCE3.start();
        INSTANCE4.start();
    }
}
