package com.shiver.chestcavity.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageScriptAbilityClientActivation implements IMessage {

    private ResourceLocation abilityId = new ResourceLocation("chestcavity", "unknown");

    public MessageScriptAbilityClientActivation() {
    }

    public MessageScriptAbilityClientActivation(ResourceLocation abilityId) {
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

    public static class Handler implements IMessageHandler<MessageScriptAbilityClientActivation, IMessage> {

        @Override
        public IMessage onMessage(MessageScriptAbilityClientActivation message, MessageContext ctx) {
            if (ChestCavityNetwork.isClient(ctx)) {
                ChestCavityNetwork.handleClientMessage("handleScriptAbilityClientActivation", message);
            }
            return null;
        }
    }
}
