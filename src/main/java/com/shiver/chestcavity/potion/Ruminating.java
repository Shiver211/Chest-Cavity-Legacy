package com.shiver.chestcavity.potion;

import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 表示反刍期间持续恢复饥饿值的效果。
 */
public class Ruminating extends CCPotion {

    /**
     * 创建反刍药水效果。
     */
    public Ruminating() {
        super(false, 0x91B84A);
        setBeneficial();
    }

    /**
     * 按配置的反刍周期定时触发一次恢复。
     *
     * @param duration 剩余持续时间。
     * @param amplifier 当前等级。
     * @return `true` 表示本 tick 应执行效果。
     */
    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % Math.max(1, CCConfig.RUMINATION_TIME) == 1;
    }

    /**
     * 为玩家恢复少量饥饿值和饱和度。
     *
     * @param entityLivingBaseIn 目标实体。
     * @param amplifier 当前等级。
     */
    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        if (!entityLivingBaseIn.world.isRemote && entityLivingBaseIn instanceof EntityPlayer) {
            ((EntityPlayer) entityLivingBaseIn).getFoodStats().addStats(1, 0.1F);
        }
    }
}
