package com.shiver.chestcavity.ui;

import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * 表示胸腔界面在客户端与服务端之间同步的最小数据集。
 */
public class ChestCavityGuiData extends GuiData {

    private final int targetEntityId;
    private final int columns;
    private final int rows;

    /**
     * 创建一份胸腔界面同步数据（使用默认 9x3 尺寸）。
     *
     * @param player 打开界面的玩家。
     * @param targetEntityId 目标实体 ID。
     */
    public ChestCavityGuiData(EntityPlayer player, int targetEntityId) {
        this(player, targetEntityId, 9, 3);
    }

    /**
     * 创建一份指定网格尺寸的胸腔界面同步数据。
     *
     * @param player 打开界面的玩家。
     * @param targetEntityId 目标实体 ID。
     * @param columns 网格列数。
     * @param rows 网格行数。
     */
    public ChestCavityGuiData(EntityPlayer player, int targetEntityId, int columns, int rows) {
        super(player);
        this.targetEntityId = targetEntityId;
        this.columns = columns > 0 ? columns : 9;
        this.rows = rows > 0 ? rows : 3;
    }

    /**
     * 返回当前界面操作目标的实体 ID。
     *
     * @return 目标实体 ID。
     */
    public int getTargetEntityId() {
        return targetEntityId;
    }

    /**
     * 返回当前界面的网格列数。
     *
     * @return 列数。
     */
    public int getColumns() {
        return columns;
    }

    /**
     * 返回当前界面的网格行数。
     *
     * @return 行数。
     */
    public int getRows() {
        return rows;
    }

    /**
     * 返回当前界面的槽位总数。
     *
     * @return 槽位总数。
     */
    public int getSlotCount() {
        return columns * rows;
    }

    /**
     * 按保存的实体 ID 在当前世界中查找目标实体。
     *
     * @return 目标实体；如果不存在或不是活体则返回 `null`。
     */
    public EntityLivingBase getTarget() {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        Entity entity = world.getEntityByID(targetEntityId);
        return entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;
    }
}
