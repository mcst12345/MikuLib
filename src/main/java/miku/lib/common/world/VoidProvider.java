package miku.lib.common.world;

import miku.lib.common.world.biome.VoidBiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VoidProvider extends WorldProvider {
    @Override
    @Nonnull
    public DimensionType getDimensionType() {
        return Void.Void;
    }

    @Override
    protected void generateLightBrightnessTable(){}

    @Override
    protected void init(){
        this.biomeProvider = new VoidBiomeProvider();
        this.hasSkyLight = false;
    }

    @Override
    @Nonnull
    public IChunkGenerator createChunkGenerator(){
        return new VoidChunkGenerator(world);
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z){
        return false;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks){
        return 0.0f;
    }

    @Override
    public int getMoonPhase(long worldTime){
        return 0;
    }

    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks)
    {
        return new float[5];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_){
        return new Vec3d(0.0d,0.0d,0.0d);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getCloudHeight(){
        return Float.MIN_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isSkyColored(){
        return false;
    }

    @Override
    public int getAverageGroundLevel(){
        return 0;
    }

    @Override
    public boolean isNether(){
        return false;
    }

    @Override
    @Nullable
    public String getSaveFolder(){
        return null;
    }

    @Override
    public double getMovementFactor(){
        return Double.MAX_VALUE;
    }

    @Override
    public boolean shouldClientCheckLighting(){
        return false;
    }

    @Override
    @Nonnull
    public BlockPos getRandomizedSpawnPoint(){
        return new BlockPos(0,0,0);
    }

    @Override
    public boolean shouldMapSpin(@Nonnull String entity, double x, double z, double rotation){
        return false;
    }

    @Override
    public int getRespawnDimension(@Nonnull net.minecraft.entity.player.EntityPlayerMP player){
        return -25;
    }

    @Override
    @Nonnull
    public WorldSleepResult canSleepAt(@Nonnull net.minecraft.entity.player.EntityPlayer player,@Nonnull BlockPos pos){
        return WorldSleepResult.BED_EXPLODES;
    }

    @Override
    @Nonnull
    public Biome getBiomeForCoords(@Nonnull BlockPos pos){
        return Void.VoidBiome;
    }

    @Override
    public boolean isDaytime(){
        return false;
    }

    @Override
    public float getSunBrightnessFactor(float par1){
        return 0.0f;
    }

    @Override
    public float getCurrentMoonPhaseFactor(){
        return 0.0f;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(@Nonnull net.minecraft.entity.Entity cameraEntity, float partialTicks){
        return new Vec3d(0.0d,0.0d,0.0d);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nonnull
    public Vec3d getCloudColor(float partialTicks)
    {
        return new Vec3d(0.0d,0.0d,0.0d);
    }

    /**
     * Gets the Sun Brightness for rendering sky.
     * */
    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1)
    {
        return 0.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getStarBrightness(float par1){
        return 0.0f;
    }

    @Override
    public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful){}

    @Override
    public void calculateInitialWeather(){}

    @Override
    public void updateWeather(){}

    @Override
    public boolean canBlockFreeze(@Nonnull BlockPos pos, boolean byWater){
        return true;
    }

    @Override
    public boolean canSnowAt(@Nonnull BlockPos pos, boolean checkLight){
        return false;
    }

    @Override
    public void setWorldTime(long time){}

    @Override
    public long getSeed(){
        return 0;
    }

    @Override
    public long getWorldTime(){
        return 0;
    }

    @Override
    @Nonnull
    public BlockPos getSpawnPoint(){
        return new BlockPos(0,0,0);
    }

    @Override
    public void setSpawnPoint(@Nonnull BlockPos pos){}

    @Override
    public boolean canMineBlock(@Nonnull net.minecraft.entity.player.EntityPlayer player,@Nonnull BlockPos pos){return false;}

    @Override
    public boolean isBlockHighHumidity(@Nonnull BlockPos pos){
        return false;
    }

    @Override
    public int getHeight(){
        return 0;
    }

    @Override
    public int getActualHeight(){
        return 0;
    }

    @Override
    public double getHorizon(){
        return 0.0d;
    }

    @Override
    public void resetRainAndThunder(){

    }

    @Override
    public boolean canDoLightning(@Nonnull net.minecraft.world.chunk.Chunk chunk){
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(@Nonnull net.minecraft.world.chunk.Chunk chunk){
        return false;
    }
}
