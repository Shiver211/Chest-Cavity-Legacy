package com.shiver.chestcavity.network;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ChestCavityNetwork {

    private static final String CHANNEL_NAME = "chestcavity";
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);

    private static boolean registered;

    private ChestCavityNetwork() {
    }

    public static synchronized boolean register() {
        if (!registered) {
            int id = 0;
            CHANNEL.registerMessage(MessageChestCavitySync.Handler.class, MessageChestCavitySync.class, id++, Side.CLIENT);
            CHANNEL.registerMessage(MessageHotkeyActivation.Handler.class, MessageHotkeyActivation.class, id++, Side.SERVER);
            CHANNEL.registerMessage(MessageOrganDataSync.Handler.class, MessageOrganDataSync.class, id++, Side.CLIENT);
            CHANNEL.registerMessage(MessageScriptAbilityClientActivation.Handler.class, MessageScriptAbilityClientActivation.class, id, Side.CLIENT);
            registered = true;
        }
        return true;
    }

    public static void sendChestCavitySync(EntityLivingBase entity) {
        register();
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null) {
            return;
        }

        MessageChestCavitySync message = new MessageChestCavitySync(entity.getEntityId(), chestCavity.serializeNBT());
        CHANNEL.sendToAllTracking(message, entity);
        if (entity instanceof EntityPlayerMP) {
            CHANNEL.sendTo(message, (EntityPlayerMP) entity);
        }
    }

    public static void sendChestCavitySyncTo(EntityLivingBase entity, EntityPlayerMP player) {
        register();
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity != null) {
            CHANNEL.sendTo(new MessageChestCavitySync(entity.getEntityId(), chestCavity.serializeNBT()), player);
        }
    }

    public static void sendOrganDataSync(EntityPlayerMP player, NBTTagCompound organData) {
        register();
        CHANNEL.sendTo(new MessageOrganDataSync(organData), player);
    }

    public static void sendOrganDataSync(EntityPlayerMP player) {
        sendOrganDataSync(player, OrganManager.writeRegistryToNbt());
    }

    public static void sendHotkeyActivation(ResourceLocation abilityId) {
        register();
        CHANNEL.sendToServer(new MessageHotkeyActivation(abilityId));
    }

    public static void sendScriptAbilityClientActivation(EntityPlayerMP player, ResourceLocation abilityId) {
        if (player == null || abilityId == null) {
            return;
        }
        register();
        CHANNEL.sendTo(new MessageScriptAbilityClientActivation(abilityId), player);
    }

    static void handleClientMessage(String methodName, IMessage message) {
        try {
            Class<?> hooks = Class.forName("com.shiver.chestcavity.network.ClientNetworkHooks");
            hooks.getMethod(methodName, message.getClass()).invoke(null, message);
        } catch (ReflectiveOperationException ignored) {
            // Dedicated server never loads client hooks. Client failures should not crash the network thread.
        }
    }

    static void handleHotkeyActivation(EntityPlayerMP player, ResourceLocation abilityId) {
        ChestCavityHelper.get(player).ifPresent(chestCavity -> {
            ChestCavityHelper.recalculateOrganScores(chestCavity);
            ActiveOrganAbilities.activate(player, chestCavity, abilityId);
        });
    }

    static boolean isClient(MessageContext context) {
        return context.side == Side.CLIENT;
    }
}
