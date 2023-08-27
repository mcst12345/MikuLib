package miku.lib.common.effect;

import miku.lib.common.api.iEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class MARENOL extends MikuEffect{//https://www.youtube.com/watch?v=BKl4gZDWP34
    private static final ResourceLocation TEXTURE = new ResourceLocation("miku","textures/gui/effect/marenol.png");
    public MARENOL(EntityLivingBase entity,int wait_time,int duration,int level){
        super(entity,wait_time,duration,level);
    }
    public MARENOL(EntityLivingBase entity, int level) {
        super(entity, 0, 99999999,level);
    }

    @Override
    public void perform() {
        switch (level){
            //TODO:Add special render effect.
            case 1:
            case 2:
                break;
            case 3:
            default:
                ((iEntity)entity).kill();
                shouldRemove = false;
        }
    }

    @Override
    public ResourceLocation getTEXTURE() {
        return TEXTURE;
    }
}
