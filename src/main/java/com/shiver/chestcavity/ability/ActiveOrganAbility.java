package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 定义一个可由玩家主动触发的器官能力。
 */
public interface ActiveOrganAbility {

    /**
     * 尝试执行一次主动器官能力。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    boolean activate(EntityPlayerMP player, IChestCavity chestCavity);
}
