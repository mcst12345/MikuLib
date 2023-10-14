package miku.lib.common.thread;

import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

public class ClientTNTThreads extends Thread {
    private static short tmp = 0;
    private static final ClientTNTThreads INSTANCE1 = new ClientTNTThreads(0);
    private static final ClientTNTThreads INSTANCE2 = new ClientTNTThreads(1);
    private static final ClientTNTThreads INSTANCE3 = new ClientTNTThreads(2);
    private static final ClientTNTThreads INSTANCE4 = new ClientTNTThreads(3);

    private ClientTNTThreads(int id) {
        this.setName("TNT-Thread-" + id);
    }

    private final List<Explosion> lists = new ArrayList<>();

    public static void AddExplosion(Explosion explosion) {
        if (!Launch.Client) {
            return;
        }
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
    public synchronized void run() {
        if (!Launch.Client) {
            return;
        }
        System.out.println("TNTThread is running.");
        while (true) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning() && !TimeStopUtil.isTimeStop() && !TimeStopUtil.isSaving()) {
                if (lists.isEmpty()) {
                    return;
                }
                for (Explosion explosion : lists) {
                    //explosion.doExplosionA();
                    explosion.doExplosionB(true);
                }
                lists.clear();
                System.out.println("Completed a list of explosions.");
            }
        }
    }

    static {
        if (Launch.Client) {
            System.out.println("Starting Client TNT Threads.");
            INSTANCE1.start();
            INSTANCE2.start();
            INSTANCE3.start();
            INSTANCE4.start();
        }
    }
}
