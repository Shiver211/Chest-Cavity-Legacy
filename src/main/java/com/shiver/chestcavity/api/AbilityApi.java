package com.shiver.chestcavity.api;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对外暴露主动能力注册和轮盘展示配置接口。
 */
public final class AbilityApi {

    private static final String COOLDOWNS_TAG = "ChestCavityAbilityCooldowns";

    private final Map<String, AbilityWheelEntry> wheelEntries = new LinkedHashMap<String, AbilityWheelEntry>();

    /**
     * 仅允许通过统一 API 单例创建。
     */
    AbilityApi() {
    }

    /**
     * 注册一个不带显示名和冷却的主动能力。
     *
     * @param scoreId 对应的器官分数标识。
     * @param handler 能力触发处理器。
     */
    public void registerAbility(String scoreId, ActiveOrganAbilityHandler handler) {
        registerAbility(scoreId, null, 0, handler);
    }

    /**
     * 注册一个带显示名但不带冷却的主动能力。
     *
     * @param scoreId 对应的器官分数标识。
     * @param displayName 轮盘上显示的名称。
     * @param handler 能力触发处理器。
     */
    public void registerAbility(String scoreId, String displayName, ActiveOrganAbilityHandler handler) {
        registerAbility(scoreId, displayName, 0, handler);
    }

    /**
     * 注册一个主动能力，并可同时配置轮盘名称与冷却时间。
     *
     * @param scoreId 对应的器官分数标识。
     * @param displayName 轮盘上显示的名称。
     * @param cooldownTicks 能力冷却时长。
     * @param handler 能力触发处理器。
     */
    public void registerAbility(String scoreId, String displayName, int cooldownTicks, ActiveOrganAbilityHandler handler) {
        if (scoreId != null && handler != null) {
            ChestCavityApis.SCORES.addScore(scoreId, displayName);
            int cooldown = Math.max(0, cooldownTicks);
            wheelEntries.put(scoreId, new AbilityWheelEntry(scoreId, cooldown));
            ActiveOrganAbilities.register(scoreId, (player, chestCavity) -> activateWithCooldown(player, chestCavity, scoreId, cooldown, handler));
        }
    }

    /**
     * 返回能力轮盘使用的只读条目表。
     *
     * @return 轮盘条目表。
     */
    public Map<String, AbilityWheelEntry> getWheelEntries() {
        return Collections.unmodifiableMap(wheelEntries);
    }

    /**
     * 在触发处理器前后统一处理冷却判定和设置。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔视图。
     * @param scoreId 对应的能力分数标识。
     * @param cooldownTicks 冷却时长。
     * @param handler 实际处理能力逻辑的回调。
     * @return `true` 表示能力成功发动。
     */
    private boolean activateWithCooldown(EntityPlayerMP player, IChestCavity chestCavity, String scoreId, int cooldownTicks, ActiveOrganAbilityHandler handler) {
        if (player == null || isOnCooldown(player, scoreId)) {
            return false;
        }
        boolean activated = handler.activate(player, new ChestCavityView(chestCavity));
        if (activated) {
            setCooldown(player, scoreId, cooldownTicks);
        }
        return activated;
    }

    /**
     * 判断玩家的指定能力是否仍在冷却中。
     *
     * @param player 要检查的玩家。
     * @param scoreId 能力分数标识。
     * @return `true` 表示仍在冷却中。
     */
    private boolean isOnCooldown(EntityPlayerMP player, String scoreId) {
        NBTTagCompound cooldowns = player.getEntityData().getCompoundTag(COOLDOWNS_TAG);
        long expiresAt = cooldowns.getLong(scoreId);
        long now = player.world.getTotalWorldTime();
        if (expiresAt > now) {
            return true;
        }
        if (expiresAt > 0L) {
            cooldowns.removeTag(scoreId);
            player.getEntityData().setTag(COOLDOWNS_TAG, cooldowns);
        }
        return false;
    }

    /**
     * 为玩家记录一段新的能力冷却。
     *
     * @param player 要写入冷却的玩家。
     * @param scoreId 能力分数标识。
     * @param cooldownTicks 冷却时长。
     */
    private void setCooldown(EntityPlayerMP player, String scoreId, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return;
        }
        NBTTagCompound cooldowns = player.getEntityData().getCompoundTag(COOLDOWNS_TAG);
        cooldowns.setLong(scoreId, player.world.getTotalWorldTime() + cooldownTicks);
        player.getEntityData().setTag(COOLDOWNS_TAG, cooldowns);
    }

    /**
     * 表示外部注册的主动能力回调。
     */
    public interface ActiveOrganAbilityHandler {
        /**
         * 执行一次主动能力逻辑。
         *
         * @param player 发动能力的玩家。
         * @param chestCavity 玩家当前胸腔视图。
         * @return `true` 表示能力成功发动。
         */
        boolean activate(EntityPlayerMP player, ChestCavityView chestCavity);
    }

    /**
     * 描述一个能力轮盘条目。
     */
    public static final class AbilityWheelEntry {
        private final String scoreId;
        private final int cooldownTicks;

        /**
         * 创建一个轮盘条目。
         *
         * @param scoreId 对应的能力分数标识。
         * @param cooldownTicks 冷却时长。
         */
        private AbilityWheelEntry(String scoreId, int cooldownTicks) {
            this.scoreId = scoreId;
            this.cooldownTicks = cooldownTicks;
        }

        /**
         * 返回条目对应的分数标识。
         *
         * @return 分数标识。
         */
        public String getScoreId() {
            return scoreId;
        }

        /**
         * 返回条目对应的冷却时长。
         *
         * @return 冷却时长。
         */
        public int getCooldownTicks() {
            return cooldownTicks;
        }
    }
}
