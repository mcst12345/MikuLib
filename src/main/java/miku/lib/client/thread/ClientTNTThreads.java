package miku.lib.client.thread;

import miku.lib.common.api.iWorld;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.Explosion;

import java.util.ArrayList;
import java.util.List;

public class ClientTNTThreads extends Thread {
    private static final ClientTNTThreads INSTANCE = new ClientTNTThreads();

    private ClientTNTThreads() {
        this.setName("Client-TNT-Thread");
        this.setPriority(3);
    }

    private final List<Explosion> lists = new ArrayList<>();

    public static void AddExplosion(Explosion explosion) {
        if (!Launch.Client) {
            return;
        }
        synchronized (INSTANCE.lists) {
            INSTANCE.lists.add(explosion);
        }
    }

    @Override
    public void run() {
        if (!Launch.Client) {
            return;
        }
        while (true) {
            synchronized (lists) {
                if (!lists.isEmpty()) {
                    List<Explosion> unloads = new ArrayList<>();
                    for (Explosion explosion : lists) {
                        if (!((iWorld) explosion.world).updatingEntities()) {
                            continue;
                        }
                        explosion.doExplosionA();
                        explosion.doExplosionB(true);
                        unloads.add(explosion);
                    }
                    lists.removeAll(unloads);
                }
            }
        }
    }

    static {
        if (Launch.Client) {
            System.out.println("Starting Client TNT Threads.");
            INSTANCE.start();
        }
    }
}
