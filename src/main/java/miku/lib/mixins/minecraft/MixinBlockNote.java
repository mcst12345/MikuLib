package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.block.BlockNote;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockNote.class)
public abstract class MixinBlockNote {
    @Shadow
    protected abstract SoundEvent getInstrument(int eventId);

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
        net.minecraftforge.event.world.NoteBlockEvent.Play e = new net.minecraftforge.event.world.NoteBlockEvent.Play(worldIn, pos, state, param, id);
        if (MikuLib.MikuEventBus().post(e)) return false;
        id = e.getInstrument().ordinal();
        param = e.getVanillaNoteId();
        float f = (float) Math.pow(2.0D, (double) (param - 12) / 12.0D);
        worldIn.playSound(null, pos, this.getInstrument(id), SoundCategory.RECORDS, 3.0F, f);
        worldIn.spawnParticle(EnumParticleTypes.NOTE, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.2D, (double) pos.getZ() + 0.5D, (double) param / 24.0D, 0.0D, 0.0D);
        return true;
    }
}
