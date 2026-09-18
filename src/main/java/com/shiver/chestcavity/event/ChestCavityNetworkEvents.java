package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 负责处理胸腔系统相关的网络会话与追踪事件（玩家登录同步、断开清理、追踪同步）。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestCavityNetworkEvents {

    private ChestCavityNetworkEvents() {
    }

    /**
     * 玩家登录后立即向客户端下发胸腔与器官注册表数据。
     *
     * @param event 玩家登录事件。
     */
    @SubscribeEvent
    public static void playerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            ChestCavityNetwork.sendChestCavitySyncTo(player, player);
            ChestCavityNetwork.sendOrganDataSync(player);
            ChestCavityNetwork.sendMovementConfig(player);
        }
    }

    /**
     * 离开服务器后恢复本地运动配置，避免把上一服务器的系数带到单人世界。
     *
     * @param event 客户端断开连接事件。
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void clientDisconnected(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        CCConfig.restoreLocalMovementConfig();
    }

    /**
     * 当玩家开始追踪实体时，同步目标实体的胸腔数据。
     *
     * @param event 开始追踪事件。
     */
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityLivingBase) {
            ChestCavityNetwork.sendChestCavitySyncTo((EntityLivingBase) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
        }
    }
}

