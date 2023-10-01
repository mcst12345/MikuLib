package miku.lib.mixins.minecraft;

import com.google.common.collect.Lists;
import miku.lib.common.util.EntityUtil;
import miku.lib.common.util.timestop.TimeStopUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(value = EntityTracker.class)
public class MixinEntityTracker {

    @Shadow
    @Final
    private Set<EntityTrackerEntry> entries;

    @Shadow
    @Final
    private IntHashMap<EntityTrackerEntry> trackedEntityHashTable;

    @Shadow
    private int maxTrackingDistanceThreshold;

    @Shadow
    @Final
    private WorldServer world;

    @Shadow
    @Final
    private static Logger LOGGER;

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void track(Entity entityIn, int trackingRange, final int updateFrequency, boolean sendVelocityUpdates) {
        if (EntityUtil.isKilling())
            return;
        try {
            if (this.trackedEntityHashTable.containsItem(entityIn.getEntityId())) {
                System.out.println("MikuWarn:entity is already tracked,retracking it.");
                this.trackedEntityHashTable.removeObject(entityIn.getEntityId());
                this.entries.removeIf(entityTrackerEntry -> entityTrackerEntry.getTrackedEntity().getEntityId() == entityIn.getEntityId());
            }

            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entityIn, trackingRange, this.maxTrackingDistanceThreshold, updateFrequency, sendVelocityUpdates);
            this.entries.add(entitytrackerentry);
            this.trackedEntityHashTable.addKey(entityIn.getEntityId(), entitytrackerentry);
            entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding entity to track");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
            crashreportcategory.addCrashSection("Tracking range", trackingRange + " blocks");
            crashreportcategory.addDetail("Update interval", () -> {
                String s = "Once per " + updateFrequency + " ticks";

                if (updateFrequency == Integer.MAX_VALUE) {
                    s = "Maximum (" + s + ")";
                }

                return s;
            });
            entityIn.addEntityCrashInfo(crashreportcategory);
            this.trackedEntityHashTable.lookup(entityIn.getEntityId()).getTrackedEntity().addEntityCrashInfo(crashreport.makeCategory("Entity That Is Already Tracked"));

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                LOGGER.error("\"Silently\" catching entity tracking error.", reportedexception);
            }
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void track(Entity entityIn) {
        if (EntityUtil.isKilling())
            return;
        if (net.minecraftforge.fml.common.registry.EntityRegistry.instance().tryTrackingEntity((EntityTracker) (Object) this, entityIn))
            return;

        if (entityIn instanceof EntityPlayerMP) {
            this.track(entityIn, 512, 2);
            EntityPlayerMP entityplayermp = (EntityPlayerMP) entityIn;

            for (EntityTrackerEntry entitytrackerentry : this.entries) {
                if (entitytrackerentry.getTrackedEntity() != entityplayermp) {
                    entitytrackerentry.updatePlayerEntity(entityplayermp);
                }
            }
        } else if (entityIn instanceof EntityFishHook) {
            this.track(entityIn, 64, 5, true);
        } else if (entityIn instanceof EntityArrow) {
            this.track(entityIn, 64, 20, false);
        } else if (entityIn instanceof EntitySmallFireball) {
            this.track(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntityFireball) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntitySnowball) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityLlamaSpit) {
            this.track(entityIn, 64, 10, false);
        } else if (entityIn instanceof EntityEnderPearl) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityEnderEye) {
            this.track(entityIn, 64, 4, true);
        } else if (entityIn instanceof EntityEgg) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityPotion) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityExpBottle) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityFireworkRocket) {
            this.track(entityIn, 64, 10, true);
        } else if (entityIn instanceof EntityItem) {
            this.track(entityIn, 64, 20, true);
        } else if (entityIn instanceof EntityMinecart) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityBoat) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntitySquid) {
            this.track(entityIn, 64, 3, true);
        } else if (entityIn instanceof EntityWither) {
            this.track(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityShulkerBullet) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityBat) {
            this.track(entityIn, 80, 3, false);
        } else if (entityIn instanceof EntityDragon) {
            this.track(entityIn, 160, 3, true);
        } else if (entityIn instanceof IAnimals) {
            this.track(entityIn, 80, 3, true);
        } else if (entityIn instanceof EntityTNTPrimed) {
            this.track(entityIn, 160, 10, true);
        } else if (entityIn instanceof EntityFallingBlock) {
            this.track(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityHanging) {
            this.track(entityIn, 160, Integer.MAX_VALUE, false);
        } else if (entityIn instanceof EntityArmorStand) {
            this.track(entityIn, 160, 3, true);
        } else if (entityIn instanceof EntityXPOrb) {
            this.track(entityIn, 160, 20, true);
        } else if (entityIn instanceof EntityAreaEffectCloud) {
            this.track(entityIn, 160, Integer.MAX_VALUE, true);
        } else if (entityIn instanceof EntityEnderCrystal) {
            this.track(entityIn, 256, Integer.MAX_VALUE, false);
        } else if (entityIn instanceof EntityEvokerFangs) {
            this.track(entityIn, 160, 2, false);
        }
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public void track(Entity entityIn, int trackingRange, int updateFrequency) {
        if (TimeStopUtil.isTimeStop() || EntityUtil.isKilling())
            return;
        this.track(entityIn, trackingRange, updateFrequency, false);
    }

    /**
     * @author mcst12345
     * @reason Fuck!!!
     */
    @Overwrite
    public void tick() {
        if (EntityUtil.isKilling()) return;
        List<EntityPlayerMP> list = Lists.newArrayList();

        for (EntityTrackerEntry entitytrackerentry : this.entries) {
            entitytrackerentry.updatePlayerList(this.world.playerEntities);

            if (entitytrackerentry.playerEntitiesUpdated) {
                Entity entity = entitytrackerentry.getTrackedEntity();

                if (entity instanceof EntityPlayerMP) {
                    list.add((EntityPlayerMP) entity);
                }
            }
        }

        for (EntityPlayerMP entityplayermp : list) {
            for (EntityTrackerEntry entitytrackerentry1 : this.entries) {
                if (entitytrackerentry1.getTrackedEntity() != entityplayermp) {
                    entitytrackerentry1.updatePlayerEntity(entityplayermp);
                }
            }
        }
    }
}
