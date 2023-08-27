package miku.lib.network.packets;

import io.netty.buffer.ByteBuf;
import miku.lib.client.api.iMinecraft;
import miku.lib.common.api.iWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

public class SummonEntityOnClient implements IMessage {
    private SummonEntityOnClient() {
        this.entity = null;
        this.length = 0;
    }

    private String entity;
    private int length;
    private NBTTagCompound nbt;

    public SummonEntityOnClient(String entity, NBTTagCompound nbt) {
        this.entity = entity;
        this.length = entity.length();
        this.nbt = nbt;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        length = buf.readInt();
        entity = (String) buf.readCharSequence(length, Charset.defaultCharset());
        nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(length);
        buf.writeCharSequence(entity, Charset.defaultCharset());
        ByteBufUtils.writeTag(buf, nbt);
    }

    public static class MessageHandler implements IMessageHandler<SummonEntityOnClient, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SummonEntityOnClient message, MessageContext ctx) {
            WorldClient world = ((iMinecraft) Minecraft.getMinecraft()).MikuWorld();
            try {
                Class<? extends Entity> entity_class = (Class<? extends Entity>) Class.forName(message.entity);
                Constructor<?> constructor = entity_class.getConstructor(World.class);
                Entity e = (Entity) constructor.newInstance(world);
                e.readFromNBT(message.nbt);
                ((iWorld) world).summonEntity(e);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
