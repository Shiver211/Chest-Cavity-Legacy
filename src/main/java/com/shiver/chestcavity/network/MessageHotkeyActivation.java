package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageHotkeyActivation implements IMessage {

    private ResourceLocation abilityId = new ResourceLocation("chestcavity", "unknown");

    public MessageHotkeyActivation() {
    }

    public MessageHotkeyActivation(ResourceLocation abilityId) {
        this.abilityId = abilityId;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = new ResourceLocation(new PacketBuffer(buf).readString(32767));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeString(abilityId.toString());
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
