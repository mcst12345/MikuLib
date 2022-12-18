package miku.lib.api;

public interface ProtectedEntity {
    boolean CanBeKilled();

    boolean DEAD();

    void SetHealth(int health);

    int GetHealth();
}
