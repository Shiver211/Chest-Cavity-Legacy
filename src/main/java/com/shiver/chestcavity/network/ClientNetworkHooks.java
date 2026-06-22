package com.shiver.chestcavity.network;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 仅客户端使用的网络消息处理钩子。
 */
@SideOnly(Side.CLIENT)
public final class ClientNetworkHooks {

    /**
     * 工具类，不允许外部实例化。
     */
    private ClientNetworkHooks() {
    }

    /**
     * 在客户端主线程中应用实体胸腔数据同步。
     *
     * @param message 服务端发来的胸腔同步消息。
     */
    public static void handleChestCavitySync(MessageChestCavitySync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(() -> {
            if (minecraft.world == null) {
                return;
            }

            Entity entity = minecraft.world.getEntityByID(message.getEntityId());
            IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
            if (chestCavity != null) {
                chestCavity.deserializeNBT(message.getData());
            }
        });
    }

    /**
     * 在客户端主线程中刷新器官注册表数据。
     *
     * @param message 服务端发来的器官数据同步消息。
     */
    public static void handleOrganDataSync(MessageOrganDataSync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        NBTTagCompound organData = message.getOrganData().copy();
        minecraft.addScheduledTask(() -> OrganManager.readRegistryFromNbt(organData));
    }
}
