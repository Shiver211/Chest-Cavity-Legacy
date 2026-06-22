package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.ability.builtin.BuiltinOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 负责注册和触发所有主动器官能力。
 */
public final class ActiveOrganAbilities {

    private static final Map<String, ActiveOrganAbility> ABILITIES = new LinkedHashMap<String, ActiveOrganAbility>();

    static {
        BuiltinOrganAbilities.registerAll();
    }

    /**
     * 工具类，不允许外部实例化。
     */
    private ActiveOrganAbilities() {
    }

    /**
     * 注册一个主动器官能力实现。
     *
     * @param id 能力对应的分数标识。
     * @param ability 能力实现。
     */
    public static void register(String id, ActiveOrganAbility ability) {
        ABILITIES.put(id, ability);
    }

    /**
     * 尝试为玩家触发指定的主动器官能力。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @param abilityId 要触发的能力标识。
     * @return `true` 表示能力成功发动。
     */
    public static boolean activate(EntityPlayerMP player, IChestCavity chestCavity, String abilityId) {
        ActiveOrganAbility ability = ABILITIES.get(abilityId);
        if (ability == null) {
            ChestCavityLegacy.LOGGER.debug("Ignoring unknown active organ ability {}.", abilityId);
            return false;
        }
        if (chestCavity.getOrganScore(abilityId) <= 0.0F) {
            ChestCavityLegacy.LOGGER.debug("Ignoring inactive organ ability {} for {}.", abilityId, player.getName());
            return false;
        }
        boolean activated = ability.activate(player, chestCavity);
        if (activated) {
            CrTChestCavityEvents.publishAbilityActivated(player, abilityId, chestCavity.getOrganScore(abilityId));
        }
        return activated;
    }

    /**
     * 触发一个延迟到队列中执行的投射物能力。
     *
     * @param entity 发动能力的实体。
     * @param abilityId 要触发的能力标识。
     * @return `true` 表示投射物成功生成。
     */
    public static boolean fireQueuedProjectile(EntityLivingBase entity, String abilityId) {
        return QueuedProjectileAbilities.fire(entity, abilityId);
    }
}
