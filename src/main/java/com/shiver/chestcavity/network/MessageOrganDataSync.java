package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOrganDataSync implements IMessage {

    private NBTTagCompound organData = new NBTTagCompound();

    public MessageOrganDataSync() {
    }

    public MessageOrganDataSync(NBTTagCompound organData) {
        this.organData = organData == null ? new NBTTagCompound() : organData.copy();
    }

    public NBTTagCompound getOrganData() {
        return organData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        organData = tag == null ? new NBTTagCompound() : tag;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, organData);
    }

    public static class Handler implements IMessageHandler<MessageOrganDataSync, IMessage> {

        @Override
        public IMessage onMessage(MessageOrganDataSync message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleOrganDataSync", message);
            }
            return null;
        }
    }
}
