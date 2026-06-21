package com.shiver.chestcavity.organ;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.data.DataLoaders;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class OrganScoreCalculator {

    private OrganScoreCalculator() {
    }

    public static void recalculate(IChestCavity chestCavity) {
        ChestCavityType type = ChestCavityHelper.getChestCavityType(chestCavity);
        Map<String, Float> scores = new LinkedHashMap<>();

        if (!chestCavity.isOpened()) {
            scores.putAll(type.getDefaultOrganScores());
        } else {
            type.loadBaseOrganScores(scores);
            for (ItemStack stack : chestCavity.getOrgans()) {
                if (!stack.isEmpty()) {
                    OrganData data = type.catchExceptionalOrgan(stack);
                    if (data == null) {
                        data = OrganData.fromStack(stack);
                    }
                    if (data != null) {
                        addOrganScores(scores, data, stack);
                        if (!data.isPseudoOrgan() && ChestCavityHelper.getCompatibilityLevel(chestCavity, stack) < 1) {
                            Float old = scores.get(CCOrganScores.INCOMPATIBILITY);
                            scores.put(CCOrganScores.INCOMPATIBILITY, old == null ? 1.0F : old + 1.0F);
                        }
                    }
                }
            }
        }

        chestCavity.replaceOrganScores(scores);
        markScoresClean(chestCavity);
    }

    private static void addOrganScores(Map<String, Float> scores, OrganData data, ItemStack stack) {
        float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
        for (Map.Entry<String, Float> entry : data.getOrganScores().entrySet()) {
            Float old = scores.get(entry.getKey());
            float value = entry.getValue() * stackRatio;
            scores.put(entry.getKey(), old == null ? value : old + value);
        }
    }

    private static void markScoresClean(IChestCavity chestCavity) {
        if (chestCavity instanceof ChestCavityData) {
            ((ChestCavityData) chestCavity).markScoresClean(DataLoaders.getDataVersion());
        }
    }
}
