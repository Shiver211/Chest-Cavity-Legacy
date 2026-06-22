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

/**
 * 负责胸腔系统的客户端与服务端网络同步。
 */
public final class ChestCavityNetwork {

    private static final String CHANNEL_NAME = "chestcavity";
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);

    private static boolean registered;

    /**
     * 工具类，不允许外部实例化。
     */
    private ChestCavityNetwork() {
    }

    /**
     * 注册胸腔系统使用的网络消息类型。
     *
     * @return 始终返回 `true`，便于在调用链中直接使用。
     */
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

    /**
     * 向正在追踪该实体的客户端同步胸腔数据。
     *
     * @param entity 需要同步胸腔数据的实体。
     */
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

    /**
     * 仅向指定玩家发送某个实体的胸腔数据。
     *
     * @param entity 需要同步胸腔数据的实体。
     * @param player 接收同步数据的玩家。
     */
    public static void sendChestCavitySyncTo(EntityLivingBase entity, EntityPlayerMP player) {
        register();
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
        if (chestCavity != null) {
            CHANNEL.sendTo(new MessageChestCavitySync(entity.getEntityId(), chestCavity.serializeNBT()), player);
        }
    }

    /**
     * 向指定玩家发送完整的器官注册表数据。
     *
     * @param player 接收同步数据的玩家。
     * @param organData 要发送的器官数据。
     */
    public static void sendOrganDataSync(EntityPlayerMP player, NBTTagCompound organData) {
        register();
        CHANNEL.sendTo(new MessageOrganDataSync(organData), player);
    }

    /**
     * 向指定玩家发送当前服务器保存的器官注册表数据。
     *
     * @param player 接收同步数据的玩家。
     */
    public static void sendOrganDataSync(EntityPlayerMP player) {
        sendOrganDataSync(player, OrganManager.writeRegistryToNbt());
    }

    /**
     * 向服务端发送一次快捷键能力激活请求。
     *
     * @param abilityId 要激活的能力标识。
     */
    public static void sendHotkeyActivation(String abilityId) {
        register();
        CHANNEL.sendToServer(new MessageHotkeyActivation(abilityId));
    }

    /**
     * 将客户端消息分发给仅客户端存在的网络钩子实现。
     *
     * @param methodName 目标钩子方法名。
     * @param message 收到的网络消息。
     */
    static void handleClientMessage(String methodName, IMessage message) {
        try {
            Class<?> hooks = Class.forName("com.shiver.chestcavity.network.ClientNetworkHooks");
            hooks.getMethod(methodName, message.getClass()).invoke(null, message);
        } catch (ReflectiveOperationException ignored) {
            // Dedicated server never loads client hooks. Client failures should not crash the network thread.
        }
    }

    /**
     * 在服务端处理玩家发来的快捷键激活请求。
     *
     * @param player 发起请求的玩家。
     * @param abilityId 要激活的能力标识。
     */
    static void handleHotkeyActivation(EntityPlayerMP player, String abilityId) {
        ChestCavityHelper.get(player).ifPresent(chestCavity -> {
            ChestCavityHelper.recalculateOrganScores(chestCavity);
            ActiveOrganAbilities.activate(player, chestCavity, abilityId);
        });
    }

    /**
     * 判断当前消息上下文是否位于客户端一侧。
     *
     * @param context 网络消息上下文。
     * @return `true` 表示消息正在客户端处理。
     */
    static boolean isClient(MessageContext context) {
        return context.side == Side.CLIENT;
    }
}
