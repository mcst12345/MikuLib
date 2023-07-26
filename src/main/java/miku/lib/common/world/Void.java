package miku.lib.common.world;

import miku.lib.common.sqlite.Sqlite;
import miku.lib.common.world.biome.VoidBiome;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;

public class Void {
    public static final Biome VoidBiome = new VoidBiome();
    public static final int ID = -25;
    public static DimensionType Void;

    public static void Init(){
        Void = DimensionType.register("void","new_void",ID, VoidProvider.class,(boolean) Sqlite.GetValueFromTable("void_keep_loaded","CONFIG",0));
        DimensionManager.registerDimension(ID,Void);
    }
}
