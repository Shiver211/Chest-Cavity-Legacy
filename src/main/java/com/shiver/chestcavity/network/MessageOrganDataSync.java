package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 用于同步器官注册表数据的网络消息。
 */
public class MessageOrganDataSync implements IMessage {

    private NBTTagCompound organData = new NBTTagCompound();

    /**
     * 留给网络框架反序列化使用的空构造。
     */
    public MessageOrganDataSync() {
    }

    /**
     * 创建一条器官注册表同步消息。
     *
     * @param organData 要同步的器官数据。
     */
    public MessageOrganDataSync(NBTTagCompound organData) {
        this.organData = organData == null ? new NBTTagCompound() : organData.copy();
    }

    /**
     * 返回需要同步的器官注册表数据。
     *
     * @return 器官注册表数据。
     */
    public NBTTagCompound getOrganData() {
        return organData;
    }

    /**
     * 从网络缓冲区中读取消息内容。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        organData = tag == null ? new NBTTagCompound() : tag;
    }

    /**
     * 将消息内容写入网络缓冲区。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, organData);
    }

    /**
     * 处理器官数据同步消息的默认处理器。
     */
    public static class Handler implements IMessageHandler<MessageOrganDataSync, IMessage> {

        /**
         * 将消息转发给客户端网络钩子处理。
         *
         * @param message 收到的器官数据同步消息。
         * @param ctx 网络上下文。
         * @return 始终返回 `null`，表示没有回包。
         */
        @Override
        public IMessage onMessage(MessageOrganDataSync message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleOrganDataSync", message);
            }
            return null;
        }
    }
}
