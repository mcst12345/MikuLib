package miku.lib.effect;

import miku.lib.api.iEntity;
import net.minecraft.entity.EntityLivingBase;

public class MARENOL extends MikuEffect{
    public MARENOL(EntityLivingBase entity,int wait_time,int duration,int level){
        super(entity,wait_time,duration,level);
    }
    public MARENOL(EntityLivingBase entity, int level) {
        super(entity, 0, 99999999,level);
    }

    @Override
    public void perform() {
        switch (level){
            case 1:
            case 2:
                break;
            case 3:
            default:
                ((iEntity)entity).kill();
                shouldRemove = false;
        }
    }
}
