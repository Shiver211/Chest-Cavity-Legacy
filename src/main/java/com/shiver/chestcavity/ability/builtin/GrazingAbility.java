package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 啃食脚下草地并转化为反刍持续时间的主动能力。
 */
final class GrazingAbility implements ActiveOrganAbility {

    static final GrazingAbility INSTANCE = new GrazingAbility();

    /**
     * 单例能力实现，不允许外部实例化。
     */
    private GrazingAbility() {
    }

    /**
     * 将脚下的草方块或菌丝转化为反刍效果时长。
     *
     * @param player 发动能力的玩家。
     * @param chestCavity 玩家当前胸腔数据。
     * @return `true` 表示能力成功发动。
     */
    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float grazing = chestCavity.getOrganScore(CCOrganScores.GRAZING);
        if (grazing <= 0.0F) {
            return false;
        }

        World world = player.world;
        BlockPos blockPos = player.getPosition().down();
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        if (block != Blocks.GRASS && block != Blocks.MYCELIUM) {
            return false;
        }

        world.playEvent(2001, blockPos, Block.getStateId(state));
        world.setBlockState(blockPos, Blocks.DIRT.getDefaultState(), 2);

        PotionEffect current = player.getActivePotionEffect(CCPotions.RUMINATING);
        int grassDuration = Math.max(1, CCConfig.RUMINATION_TIME * CCConfig.RUMINATION_GRASS_PER_SQUARE);
        int maxDuration = Math.max(grassDuration,
                Math.round(grazing * grassDuration * CCConfig.RUMINATION_SQUARES_PER_STOMACH));
        int duration = current == null ? grassDuration : Math.min(maxDuration, current.getDuration() + grassDuration);
        player.addPotionEffect(new PotionEffect(CCPotions.RUMINATING, duration, 0, false, true));
        return true;
    }
}
