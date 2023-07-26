package miku.lib.common.api;

public interface ProtectedEntity {
    boolean CanBeKilled();

    boolean DEAD();

    void SetHealth(int health);

    int GetHealth();

    void Hurt(int amount);
}
