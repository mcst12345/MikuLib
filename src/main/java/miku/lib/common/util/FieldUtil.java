package miku.lib.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.lang.reflect.Field;

//shitmountain

public class FieldUtil {
    private static boolean loaded = false;
    public static Field field_70134_J;
    public static Field field_190534_ay;
    public static Field isAddedToWorld;
    public static Field field_70128_L;
    public static Field field_70180_af;
    public static Field field_70133_I;
    public static Field field_70721_aZ;
    public static Field field_70708_bq;
    public static Field field_110153_bc;
    public static Field field_70718_bc;
    public static Field field_70755_b;
    public static Field field_70756_c;
    public static Field field_70746_aG;
    public static Field field_70713_bf;
    public static Field field_110155_d;
    public static Field field_111154_a;
    public static Field field_70729_aU;
    public static Field field_70725_aQ;
    public static Field field_70717_bb;
    public static Field field_110150_bn;
    public static Field field_142016_bo;
    public static Field field_110151_bq;
    public static Field field_94063_bt;
    public static Field field_71078_a;
    public static Field field_71071_by;
    public static Field field_72996_f;
    public static Field field_73007_j;
    public static Field field_73021_x;

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void Init() {
        if (loaded) return;
        loaded = true;
        try {
            field_70134_J = Entity.class.getDeclaredField("field_70134_J");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_190534_ay = Entity.class.getDeclaredField("field_190534_ay");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            isAddedToWorld = Entity.class.getDeclaredField("isAddedToWorld");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70128_L = Entity.class.getDeclaredField("field_70128_L");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70180_af = Entity.class.getDeclaredField("field_70180_af");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70133_I = Entity.class.getDeclaredField("field_70133_I");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70721_aZ = EntityLivingBase.class.getDeclaredField("field_70721_aZ");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70708_bq = EntityLivingBase.class.getDeclaredField("field_70708_bq");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_110153_bc = EntityLivingBase.class.getDeclaredField("field_110153_bc");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70718_bc = EntityLivingBase.class.getDeclaredField("field_70718_bc");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70755_b = EntityLivingBase.class.getDeclaredField("field_70755_b");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70756_c = EntityLivingBase.class.getDeclaredField("field_70756_c");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70746_aG = EntityLivingBase.class.getDeclaredField("field_70746_aG");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70713_bf = EntityLivingBase.class.getDeclaredField("field_70713_bf");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            field_110155_d = EntityLivingBase.class.getDeclaredField("field_110155_d");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_111154_a = AbstractAttributeMap.class.getDeclaredField("field_111154_a");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70729_aU = EntityLivingBase.class.getDeclaredField("field_70729_aU");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70725_aQ = EntityLivingBase.class.getDeclaredField("field_70725_aQ");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_70717_bb = EntityLivingBase.class.getDeclaredField("field_70717_bb");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_110150_bn = EntityLivingBase.class.getDeclaredField("field_110150_bn");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_142016_bo = EntityLivingBase.class.getDeclaredField("field_142016_bo");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_110151_bq = EntityLivingBase.class.getDeclaredField("field_110151_bq");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_94063_bt = EntityLivingBase.class.getDeclaredField("field_94063_bt");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_71078_a = EntityPlayer.class.getDeclaredField("field_71078_a");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_71071_by = EntityPlayer.class.getDeclaredField("field_71071_by");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_72996_f = World.class.getDeclaredField("field_72996_f");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_73007_j = World.class.getDeclaredField("field_73007_j");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            field_73021_x = World.class.getDeclaredField("field_73021_x");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
