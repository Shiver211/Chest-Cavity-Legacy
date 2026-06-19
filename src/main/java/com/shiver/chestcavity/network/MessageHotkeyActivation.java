package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageHotkeyActivation implements IMessage {

    private String abilityId = "unknown";

    public MessageHotkeyActivation() {
    }

    public MessageHotkeyActivation(String abilityId) {
        this.abilityId = abilityId;
    }

    public String getAbilityId() {
        return abilityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = new PacketBuffer(buf).readString(32767);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeString(abilityId);
    }

    public static class Handler implements IMessageHandler<MessageHotkeyActivation, IMessage> {

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
