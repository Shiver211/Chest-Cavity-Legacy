package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 负责处理胸腔系统相关的战斗事件（攻击闪避、伤害效果与碰撞破坏）。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestCavityCombatEvents {

    private ChestCavityCombatEvents() {
    }

    /**
     * 在攻击判定前尝试处理投射物闪避。
     *
     * @param event 攻击事件。
     */
    @SubscribeEvent
    public static void livingAttack(LivingAttackEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null && ChestCavityHelper.attemptProjectileDodge(event.getEntityLiving(), chestCavity, event.getSource())) {
            event.setCanceled(true);
        }
    }

    /**
     * 在最终伤害结算阶段处理附加效果与碰撞破坏。
     *
     * @param event 实际受伤事件。
     */
    @SubscribeEvent
    public static void livingDamage(LivingDamageEvent event) {
        float amount = ChestCavityHelper.applyFinalDamageEffects(event.getEntityLiving(), event.getSource(), event.getAmount());
        event.setAmount(amount);
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.applyDestructiveCollisions(event.getEntityLiving(), chestCavity, event.getSource(), amount);
        }
    }
}

