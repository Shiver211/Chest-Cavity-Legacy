package com.shiver.chestcavity.ui;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 连接游戏逻辑与胸腔界面工厂的桥接层。
 */
public final class ChestCavityUiBridge {

    public static final String FACTORY_ID = "chestcavity:chest_cavity";
    public static final String PANEL_ID = "chest_cavity";
    public static final int CHEST_CAVITY_SLOTS = 27;
    public static final int SLOTS_PER_ROW = 9;
    public static final double MAX_INTERACT_DISTANCE_SQ = 64.0D;

    /**
     * 工具类，不允许外部实例化。
     */
    private ChestCavityUiBridge() {
    }

    /**
     * 判断玩家是否仍满足保持胸腔界面开启的条件。
     *
     * @param player 交互玩家。
     * @param data 当前界面同步数据。
     * @return `true` 表示界面可以继续保持开启。
     */
    public static boolean canKeepOpen(EntityPlayer player, ChestCavityGuiData data) {
        EntityLivingBase target = data.getTarget();
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(target);
        return player != null
                && target != null
                && target.isEntityAlive()
                && player.getDistanceSq(target) <= MAX_INTERACT_DISTANCE_SQ
                && ChestCavityHelper.hasAssignedChestCavityType(chestCavity);
    }

    /**
     * 为指定玩家打开目标实体的胸腔界面。
     *
     * @param player 发起打开请求的玩家。
     * @param target 要查看的目标实体。
     * @return `true` 表示界面成功打开。
     */
    public static boolean open(EntityPlayerMP player, EntityLivingBase target) {
        if (player == null || target == null || player.world.isRemote) {
            return false;
        }

        ChestCavityGuiData data = new ChestCavityGuiData(player, target.getEntityId());
        if (!canKeepOpen(player, data)) {
            return false;
        }

        ChestCavityGuiFactory.open(player, data);
        return true;
    }
}
