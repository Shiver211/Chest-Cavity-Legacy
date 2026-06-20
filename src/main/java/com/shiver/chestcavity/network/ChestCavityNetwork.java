package com.shiver.chestcavity.network;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.content.ContentSync;
import com.shiver.chestcavity.ui.BodyUiSnapshot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
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
            CHANNEL.registerMessage(MessageOrganDataSync.Handler.class, MessageOrganDataSync.class, id, Side.CLIENT);
            registered = true;
        }
        return true;
    }

    public static void sendChestCavitySync(EntityLivingBase entity) {
        register();
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity == null) {
            return;
        }
        chestCavity.refreshRuntimeIfDirty();

        MessageChestCavitySync message = new MessageChestCavitySync(entity.getEntityId(), BodyUiSnapshot.create(chestCavity));
        CHANNEL.sendToAllTracking(message, entity);
        if (entity instanceof EntityPlayerMP) {
            CHANNEL.sendTo(message, (EntityPlayerMP) entity);
        }
    }

    public static void sendChestCavitySyncTo(EntityLivingBase entity, EntityPlayerMP player) {
        register();
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity != null) {
            chestCavity.refreshRuntimeIfDirty();
            CHANNEL.sendTo(new MessageChestCavitySync(entity.getEntityId(), BodyUiSnapshot.create(chestCavity)), player);
        }
    }

    public static void sendOrganDataSync(EntityPlayerMP player, NBTTagCompound organData) {
        register();
        CHANNEL.sendTo(new MessageOrganDataSync(organData), player);
    }

    public static void sendOrganDataSync(EntityPlayerMP player) {
        sendOrganDataSync(player, ContentSync.writeOrgansToNbt());
    }

    public static void sendHotkeyActivation(String abilityId) {
        register();
        CHANNEL.sendToServer(new MessageHotkeyActivation(abilityId));
    }

    static void handleClientChestCavitySync(MessageChestCavitySync message) {
        ChestCavityLegacy.proxy.handleChestCavitySync(message);
    }

    static void handleClientOrganDataSync(MessageOrganDataSync message) {
        ChestCavityLegacy.proxy.handleOrganDataSync(message);
    }

    static void handleHotkeyActivation(EntityPlayerMP player, String abilityId) {
        ChestCavityHelper.get(player).ifPresent(chestCavity -> {
            chestCavity.refreshRuntimeIfDirty();
            ChestCavityHelper.activateScore(player, chestCavity, abilityId);
        });
    }

    static boolean isClient(MessageContext context) {
        return context.side == Side.CLIENT;
    }
}
