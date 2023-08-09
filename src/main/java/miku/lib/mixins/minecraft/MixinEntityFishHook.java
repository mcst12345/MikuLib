package miku.lib.mixins.minecraft;

import miku.lib.common.core.MikuLib;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = EntityFishHook.class)
public abstract class MixinEntityFishHook extends Entity {
    @Shadow
    private EntityPlayer angler;

    @Shadow
    public Entity caughtEntity;

    @Shadow
    protected abstract void bringInHookedEntity();

    @Shadow
    private int ticksCatchable;

    @Shadow
    private int luck;

    @Shadow
    private boolean inGround;

    public MixinEntityFishHook(World worldIn) {
        super(worldIn);
    }

    /**
     * @author mcst12345
     * @reason Fuck!
     */
    @Overwrite
    public int handleHookRetraction() {
        if (!this.world.isRemote && this.angler != null) {
            int i = 0;

            net.minecraftforge.event.entity.player.ItemFishedEvent event = null;
            if (this.caughtEntity != null) {
                this.bringInHookedEntity();
                this.world.setEntityState(this, (byte) 31);
                i = this.caughtEntity instanceof EntityItem ? 3 : 5;
            } else if (this.ticksCatchable > 0) {
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.world);
                lootcontext$builder.withLuck(this.luck + this.angler.getLuck()).withPlayer(this.angler).withLootedEntity(this); // Forge: add player & looted entity to LootContext
                List<ItemStack> result = this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, lootcontext$builder.build());
                event = new net.minecraftforge.event.entity.player.ItemFishedEvent(result, this.inGround ? 2 : 1, (EntityFishHook) (Object) this);
                MikuLib.MikuEventBus().post(event);
                if (event.isCanceled()) {
                    this.setDead();
                    return event.getRodDamage();
                }

                for (ItemStack itemstack : result) {
                    EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
                    double d0 = this.angler.posX - this.posX;
                    double d1 = this.angler.posY - this.posY;
                    double d2 = this.angler.posZ - this.posZ;
                    double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    entityitem.motionX = d0 * 0.1D;
                    entityitem.motionY = d1 * 0.1D + MathHelper.sqrt(d3) * 0.08D;
                    entityitem.motionZ = d2 * 0.1D;
                    this.world.spawnEntity(entityitem);
                    this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                    Item item = itemstack.getItem();

                    if (item == Items.FISH || item == Items.COOKED_FISH) {
                        this.angler.addStat(StatList.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.inGround) {
                i = 2;
            }

            this.setDead();
            return event == null ? i : event.getRodDamage();
        } else {
            return 0;
        }
    }
}
