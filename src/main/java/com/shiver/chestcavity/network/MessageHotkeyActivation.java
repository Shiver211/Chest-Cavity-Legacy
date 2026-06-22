package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 用于向服务端请求激活快捷键能力的网络消息。
 */
public class MessageHotkeyActivation implements IMessage {

    private String abilityId = "unknown";

    /**
     * 留给网络框架反序列化使用的空构造。
     */
    public MessageHotkeyActivation() {
    }

    /**
     * 创建一条快捷键激活请求消息。
     *
     * @param abilityId 要激活的能力标识。
     */
    public MessageHotkeyActivation(String abilityId) {
        this.abilityId = abilityId;
    }

    /**
     * 返回请求激活的能力标识。
     *
     * @return 能力标识。
     */
    public String getAbilityId() {
        return abilityId;
    }

    /**
     * 从网络缓冲区中读取消息内容。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = new PacketBuffer(buf).readString(32767);
    }

    /**
     * 将消息内容写入网络缓冲区。
     *
     * @param buf 网络缓冲区。
     */
    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeString(abilityId);
    }

    /**
     * 处理快捷键激活消息的默认处理器。
     */
    public static class Handler implements IMessageHandler<MessageHotkeyActivation, IMessage> {

        /**
         * 将消息调度到服务端主线程中执行。
         *
         * @param message 收到的快捷键激活消息。
         * @param ctx 网络上下文。
         * @return 始终返回 `null`，表示没有回包。
         */
        @Override
        public IMessage onMessage(MessageHotkeyActivation message, MessageContext ctx) {
            if (ctx.side != Side.SERVER) {
                return null;
            }

            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> ChestCavityNetwork.handleHotkeyActivation(player, message.getAbilityId()));
            return null;
        }
    }
}
