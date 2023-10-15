package miku.lib.common.thread;

import miku.lib.common.api.iWorld;
import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.List;

public class ServerTNTThreads extends Thread {
    private static final ServerTNTThreads INSTANCE = new ServerTNTThreads();

    private ServerTNTThreads() {
        this.setName("Server-TNT-Thread");
    }

    private final List<Explosion> lists = new ArrayList<>();

    public static void AddExplosion(Explosion explosion) {
        System.out.println("This is a test message!");
        synchronized (INSTANCE.lists) {
            INSTANCE.lists.add(explosion);
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (lists) {
                if (!lists.isEmpty()) {
                    System.out.println("explode!");
                    List<Explosion> unloads = new ArrayList<>();
                    for (Explosion explosion : lists) {
                        if (!((iWorld) explosion.world).updatingEntities()) {
                            continue;
                        }
                        explosion.doExplosionA();
                        explosion.doExplosionB(false);
                        unloads.add(explosion);
                    }
                    lists.removeAll(unloads);
                }
            }
        }
    }

    static {
        System.out.println("Starting Server TNT Threads.");
        INSTANCE.start();
    }
}