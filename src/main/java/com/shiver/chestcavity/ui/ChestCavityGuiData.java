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

    /**
     * 创建一份胸腔界面同步数据。
     *
     * @param player 打开界面的玩家。
     * @param targetEntityId 目标实体 ID。
     */
    public ChestCavityGuiData(EntityPlayer player, int targetEntityId) {
        super(player);
        this.targetEntityId = targetEntityId;
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
