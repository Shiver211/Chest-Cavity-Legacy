package com.shiver.chestcavity.ui;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接游戏逻辑与胸腔界面工厂的桥接层。
 */
public final class ChestCavityUiBridge {

    public static final String FACTORY_ID = com.shiver.chestcavity.Tags.MOD_ID + ":chest_cavity";
    public static final String PANEL_ID = "chest_cavity";
    public static final int DEFAULT_CHEST_CAVITY_SLOTS = 27;
    public static final int DEFAULT_SLOTS_PER_ROW = 9;
    public static final int DEFAULT_ROWS = 3;
    public static final int CHEST_CAVITY_SLOTS = DEFAULT_CHEST_CAVITY_SLOTS;
    public static final int SLOTS_PER_ROW = DEFAULT_SLOTS_PER_ROW;
    public static final double MAX_INTERACT_DISTANCE_SQ = 64.0D;

    private static final Map<Integer, Set<EntityPlayerMP>> ACTIVE_VIEWERS = new ConcurrentHashMap<>();

    private ChestCavityUiBridge() {
    }

    /**
     * 注册正在查看目标实体的玩家会话。
     *
     * @param player 查看玩家。
     * @param targetEntityId 目标实体 ID。
     */
    public static void registerViewer(EntityPlayerMP player, int targetEntityId) {
        if (player != null) {
            ACTIVE_VIEWERS.computeIfAbsent(targetEntityId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(player);
        }
    }

    /**
     * 注销正在查看目标实体的玩家会话。
     *
     * @param player 查看玩家。
     * @param targetEntityId 目标实体 ID。
     */
    public static void unregisterViewer(EntityPlayerMP player, int targetEntityId) {
        if (player != null) {
            Set<EntityPlayerMP> viewers = ACTIVE_VIEWERS.get(targetEntityId);
            if (viewers != null) {
                viewers.remove(player);
                if (viewers.isEmpty()) {
                    ACTIVE_VIEWERS.remove(targetEntityId);
                }
            }
        }
    }

    /**
     * 主动关闭所有正在查看指定目标实体的胸腔界面。
     *
     * @param target 目标实体。
     */
    public static void closeViewers(EntityLivingBase target) {
        if (target == null) {
            return;
        }
        int targetId = target.getEntityId();
        Set<EntityPlayerMP> viewers = ACTIVE_VIEWERS.remove(targetId);
        if (viewers != null) {
            for (EntityPlayerMP player : viewers) {
                if (player != null && player.isEntityAlive()) {
                    player.closeScreen();
                }
            }
        }
        if (target instanceof EntityPlayerMP) {
            EntityPlayerMP playerTarget = (EntityPlayerMP) target;
            if (playerTarget.isEntityAlive() && playerTarget.openContainer != null && playerTarget.openContainer != playerTarget.inventoryContainer) {
                playerTarget.closeScreen();
            }
        }
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
        boolean dimensionsMatch = chestCavity == null
                || (data.getColumns() == chestCavity.getColumns() && data.getRows() == chestCavity.getRows());
        boolean canOpen = player != null
                && target != null
                && target.isEntityAlive()
                && player.getDistanceSq(target) <= MAX_INTERACT_DISTANCE_SQ
                && dimensionsMatch
                && (ChestCavityHelper.hasAssignedChestCavityType(chestCavity) || (chestCavity != null && chestCavity.hasCustomDimensions()));
        if (!canOpen && player instanceof EntityPlayerMP) {
            unregisterViewer((EntityPlayerMP) player, data.getTargetEntityId());
        }
        return canOpen;
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

        IChestCavity chestCavity = ChestCavityHelper.getOrNull(target);
        int columns = chestCavity != null ? chestCavity.getColumns() : DEFAULT_SLOTS_PER_ROW;
        int rows = chestCavity != null ? chestCavity.getRows() : DEFAULT_ROWS;
        if (chestCavity != null) {
            chestCavity.ensureSlotCount(columns * rows);
        }

        ChestCavityGuiData data = new ChestCavityGuiData(player, target.getEntityId(), columns, rows);
        if (!canKeepOpen(player, data)) {
            return false;
        }

        registerViewer(player, target.getEntityId());
        ChestCavityGuiFactory.open(player, data);
        return true;
    }
}
