package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChestCavitySync implements IMessage {

    private int entityId;
    private NBTTagCompound data = new NBTTagCompound();

    public MessageChestCavitySync() {
    }

    public MessageChestCavitySync(int entityId, NBTTagCompound data) {
        this.entityId = entityId;
        this.data = data == null ? new NBTTagCompound() : data;
    }

    public int getEntityId() {
        return entityId;
    }

    public NBTTagCompound getData() {
        return data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        data = tag == null ? new NBTTagCompound() : tag;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<MessageChestCavitySync, IMessage> {

        @Override
        public IMessage onMessage(MessageChestCavitySync message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleChestCavitySync", message);
            }
            return null;
        }
    }
}
