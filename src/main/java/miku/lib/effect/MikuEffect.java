package miku.lib.effect;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.EntityLivingBase;

public abstract class MikuEffect {
    protected boolean shouldRemove = false;
    public final EntityLivingBase entity;
    protected final int start;
    protected final int duration;
    protected final int wait_time;
    protected int level;
    public MikuEffect(EntityLivingBase entity,int wait_time,int duration,int level){
        this.entity = entity;
        start = entity.ticksExisted;
        this.wait_time = wait_time;
        this.duration = duration;
        this.level = level;
    }
    public abstract void perform();

    public final boolean shouldPerform(){
        return entity.ticksExisted >= start+wait_time || shouldRemove;
    }

    public final boolean shouldRemove(){
        return EntityUtil.isDEAD(entity) || entity.ticksExisted-start-wait_time>=duration;
    }

    public void level_up(){
        level++;
    }
}
