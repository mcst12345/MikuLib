package miku.lib.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Map;

public interface iEntityLivingBase {

    void TimeStop();
    void Kill();
    void AddPotion(PotionEffect potioneffect);

    int recentlyHit();

    void SetRecentlyHit(int value);

    int idleTime();

    void SetIdleTime(int value);

    int revengeTimer();

    void SetRevengeTimer(int value);

    float landMovementFactor();

    void SetLandMovementFactor(int value);

    Map<Potion, PotionEffect> GetPotion();

    EntityPlayer attackingPlayer();

    void SetAttackingPlayer(EntityPlayer player);

    float absorptionAmount();

    void SetAbsorptionAmount(float value);

    void SetHealth(float value);
}
