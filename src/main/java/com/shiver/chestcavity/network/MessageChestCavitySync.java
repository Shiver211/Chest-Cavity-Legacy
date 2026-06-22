package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 用于同步某个实体胸腔数据的网络消息。
 */
public class MessageChestCavitySync implements IMessage {

    private int entityId;
    private NBTTagCompound data = new NBTTagCompound();

    /**
     * 留给网络框架反序列化使用的空构造。
     */
    public MessageChestCavitySync() {
    }

    /**
     * 创建一条胸腔数据同步消息。
     *
     * @param entityId 目标实体 ID。
     * @param data 要同步的胸腔 NBT 数据。
     */
    public MessageChestCavitySync(int entityId, NBTTagCompound data) {
        this.entityId = entityId;
        this.data = data == null ? new NBTTagCompound() : data;
    }

    /**
     * 返回需要同步的目标实体 ID。
     *
     * @return 实体 ID。
     */
    public int getEntityId() {
        return entityId;
    }

    /**
     * 返回需要同步的胸腔 NBT 数据。
     *
     * @return 胸腔数据。
     */
    public NBTTagCompound getData() {
        return data;
    }

    /**
     * 从网络缓冲区中读取消息内容。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        data = tag == null ? new NBTTagCompound() : tag;
    }

    /**
     * 将消息内容写入网络缓冲区。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        ByteBufUtils.writeTag(buf, data);
    }

    /**
     * 处理胸腔同步消息的默认处理器。
     */
    public static class Handler implements IMessageHandler<MessageChestCavitySync, IMessage> {

        /**
         * 将消息转发给客户端网络钩子处理。
         *
         * @param message 收到的胸腔同步消息。
         * @param ctx 网络上下文。
         * @return 始终返回 `null`，表示没有回包。
         */
        @Override
        public IMessage onMessage(MessageChestCavitySync message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleChestCavitySync", message);
            }
            return null;
        }
    }
}
