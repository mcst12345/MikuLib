package miku.lib.effect;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

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

    public boolean shouldPerform(){
        return entity.ticksExisted >= start+wait_time || shouldRemove;
    }

    public boolean shouldRemove(){
        return EntityUtil.isDEAD(entity) || entity.ticksExisted-start-wait_time>=duration;
    }

    public void level_up(){
        level++;
    }

    public NBTTagCompound toNBT(){
        NBTTagCompound result = new NBTTagCompound();
        result.setString("class",this.getClass().toString().substring(5).trim());
        result.setInteger("start",start);
        result.setInteger("duration",duration);
        result.setInteger("wait",wait_time);
        result.setInteger("level",level);
        result.setUniqueId("entity",entity.getUniqueID());
        result.setBoolean("remove",shouldRemove);
        return result;
    }
}
