package miku.lib.effect;

import miku.lib.util.EntityUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

//What the fuck did I write?
//Well,at least it runs.
public abstract class MikuEffect {//You don't have to register.Just write a class that is extends from this.
    protected boolean shouldRemove = false;//should the effect be removed.
    public final EntityLivingBase entity;//the entity who suffers the effect.
    protected int start;//when the effect started
    protected final int duration;//effect's duration.
    protected final int wait_time;//time the effect will wait before it performs.
    protected int level;//effect level.

    //WARN:Any class extends this should have the constructor below.
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
        result.setString("class",this.getClass().toString().substring(5).trim());//The worst part.
        result.setInteger("start",start);
        result.setInteger("duration",duration);
        result.setInteger("wait",wait_time);
        result.setInteger("level",level);
        result.setBoolean("remove",shouldRemove);
        return result;
    }

    public void FromNBT(NBTTagCompound nbt){
        this.start = nbt.getInteger("start");
        this.shouldRemove = nbt.getBoolean("remove");
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public abstract ResourceLocation getTEXTURE();//72width,18height.
}
