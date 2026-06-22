package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

/**
 * 表示器官排异造成的持续伤害效果。
 */
public class OrganRejection extends CCPotion {

    public static final DamageSource DAMAGE_SOURCE = new DamageSource("cc_organ_rejection").setDamageBypassesArmor();

    /**
     * 创建器官排异药水效果。
     */
    public OrganRejection() {
        super(true, 0xC8FF00);
    }

    /**
     * 仅在效果结束前的最后一刻触发一次伤害。
     *
     * @param duration 剩余持续时间。
     * @param amplifier 当前等级。
     * @return `true` 表示本 tick 应执行效果。
     */
    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration <= 1;
    }

    /**
     * 对目标施加一次无视护甲的排异伤害。
     *
     * @param entityLivingBaseIn 目标实体。
     * @param amplifier 当前等级。
     */
    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!entityLivingBaseIn.world.isRemote) {
            entityLivingBaseIn.attackEntityFrom(DAMAGE_SOURCE, CCConfig.ORGAN_REJECTION_DAMAGE);
        }
    }
}
