package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * 用于同步参与双端运动计算的服务端配置。
 */
public class MessageMovementConfigSync implements IMessage {

    private float lightweightFactor;
    private float buoyancyLift;

    /**
     * 留给网络框架反序列化使用的空构造。
     */
    public MessageMovementConfigSync() {
    }

    /**
     * 创建一条运动配置同步消息。
     *
     * @param lightweightFactor 服务端轻量化系数。
     * @param buoyancyLift 服务端浮力系数。
     */
    public MessageMovementConfigSync(float lightweightFactor, float buoyancyLift) {
        this.lightweightFactor = lightweightFactor;
        this.buoyancyLift = buoyancyLift;
    }

    /**
     * @return 服务端轻量化系数。
     */
    public float getLightweightFactor() {
        return lightweightFactor;
    }

    /**
     * @return 服务端浮力系数。
     */
    public float getBuoyancyLift() {
        return buoyancyLift;
    }

    /**
     * 从网络缓冲区中读取消息内容。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        lightweightFactor = buf.readFloat();
        buoyancyLift = buf.readFloat();
    }

    /**
     * 将消息内容写入网络缓冲区。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(lightweightFactor);
        buf.writeFloat(buoyancyLift);
    }

    /**
     * 处理运动配置同步消息的默认处理器。
     */
    public static class Handler implements IMessageHandler<MessageMovementConfigSync, IMessage> {

        /**
         * 将消息转发给客户端网络钩子处理。
         *
         * @param message 收到的运动配置同步消息。
         * @param ctx 网络上下文。
         * @return 始终返回 `null`，表示没有回包。
         */
        @Override
        public IMessage onMessage(MessageMovementConfigSync message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleMovementConfigSync", message);
            }
            return null;
        }
    }
}
