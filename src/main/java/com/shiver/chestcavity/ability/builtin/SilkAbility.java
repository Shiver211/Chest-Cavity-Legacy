package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SilkAbility implements ActiveOrganAbility {

    static final SilkAbility INSTANCE = new SilkAbility();

    private SilkAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float silk = chestCavity.getOrganScore(CCOrganScores.SILK);
        if (silk <= 0.0F || player.isPotionActive(CCPotions.SILK_COOLDOWN)) {
            return false;
        }
        if (player.getFoodStats().getFoodLevel() < 6) {
            return false;
        }

        int exhaustionCost = 0;
        float remainingSilk = silk;
        BlockPos pos = new BlockPos(player).offset(player.getHorizontalFacing().getOpposite());
        if (remainingSilk >= 2.0F && player.world.isAirBlock(pos)) {
            if (remainingSilk >= 3.0F && placeSilkBlock(player, Blocks.WOOL, pos)) {
                remainingSilk -= 3.0F;
                exhaustionCost += 16;
            } else if (placeSilkBlock(player, Blocks.WEB, pos)) {
                remainingSilk -= 2.0F;
                exhaustionCost += 8;
            }
        }

        int strings = 0;
        while (remainingSilk >= 1.0F) {
            remainingSilk -= 1.0F;
            strings++;
            exhaustionCost += 4;
        }
        if (strings > 0) {
            player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY + 0.5D, player.posZ,
                    new ItemStack(Items.STRING, strings)));
        }
        if (exhaustionCost <= 0) {
            return false;
        }

        player.addExhaustion(exhaustionCost);
        player.addPotionEffect(new PotionEffect(CCPotions.SILK_COOLDOWN,
                CCConfig.SILK_COOLDOWN, 0, false, false));
        return true;
    }

    private static boolean placeSilkBlock(EntityPlayerMP player, Block block, BlockPos pos) {
        World world = player.world;
        ItemStack blockStack = new ItemStack(Item.getItemFromBlock(block));
        if (!player.canPlayerEdit(pos, EnumFacing.UP, blockStack)) {
            return false;
        }
        if (!world.mayPlace(block, pos, false, EnumFacing.UP, player)) {
            return false;
        }
        return world.setBlockState(pos, block.getDefaultState(), 3);
    }
}
