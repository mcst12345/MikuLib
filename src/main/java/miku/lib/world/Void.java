package miku.lib.world;

import miku.lib.sqlite.Sqlite;
import miku.lib.world.biome.VoidBiome;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

public class Void {
    public static final Biome VoidBiome = new VoidBiome();
    public static final int ID = -25;
    public static DimensionType Void;

    public static void Init(){
        Void = DimensionType.register("void","new_void",ID, VoidProvider.class,(boolean) Sqlite.GetValueFromTable("void_keep_loaded","CONFIG",0));
    }
}
