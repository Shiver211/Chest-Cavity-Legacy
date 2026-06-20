package com.shiver.chestcavity.runtime;

import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.score.Score;
import com.shiver.chestcavity.score.ScoreRef;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ChestCavityRuntime {

    private final ItemStack[] slotStacks;
    private final OrganData[] slotOrganData;
    private final int[] occupiedSlots;
    private final Map<ResourceLocation, int[]> slotsByItem;
    private final Map<String, int[]> slotsByScore;
    private final float[] indexedScoreValues;
    private final float[] indexedBaselineScoreValues;
    private final float[] indexedDeltaScoreValues;
    private Map<String, Float> scoreValuesView;
    private Map<String, Float> baselineScoreValuesView;

    private ChestCavityRuntime(Builder builder) {
        this.slotStacks = builder.slotStacks;
        this.slotOrganData = builder.slotOrganData;
        this.occupiedSlots = toIntArray(builder.occupiedSlots);
        this.slotsByItem = builder.slotsByItem == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.slotsByItem));
        this.slotsByScore = freezeSlotMap(builder.slotsByScore);
        this.indexedScoreValues = builder.indexedScoreValues == null ? new float[0] : copy(builder.indexedScoreValues);
        this.indexedBaselineScoreValues = builder.indexedBaselineScoreValues == null ? new float[0] : copy(builder.indexedBaselineScoreValues);
        this.indexedDeltaScoreValues = builder.indexedDeltaScoreValues == null ? new float[0] : copy(builder.indexedDeltaScoreValues);
    }

    public static ChestCavityRuntime rebuild(ChestCavityData chestCavity, ChestCavityType type) {
        if (chestCavity == null || type == null) {
            return new Builder(0).build();
        }

        if (!chestCavity.isOpened()) {
            Builder builder = new Builder(0);
            for (Map.Entry<String, Float> entry : type.getDefaultOrganScores().entrySet()) {
                builder.addBaselineScore(entry.getKey(), entry.getValue());
                builder.addScore(entry.getKey(), entry.getValue());
            }
            return builder.finishScores().build();
        }

        Builder builder = new Builder(chestCavity.getSlotCount());
        for (Map.Entry<String, Float> entry : type.getDefaultOrganScores().entrySet()) {
            builder.addBaselineScore(entry.getKey(), entry.getValue());
        }

        Map<String, Float> intrinsicScores = new LinkedHashMap<>();
        type.loadBaseOrganScores(intrinsicScores);
        for (Map.Entry<String, Float> entry : intrinsicScores.entrySet()) {
            builder.addScore(entry.getKey(), entry.getValue());
        }

        for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
            OrganInstance instance = chestCavity.getOrganInstance(slot, type);
            if (instance.isEmpty()) {
                builder.setSlot(slot, OrganInstance.empty());
                continue;
            }

            OrganData data = instance.getData();
            builder.setSlot(slot, instance);
            if (data == null) {
                continue;
            }

            for (Map.Entry<String, Float> entry : data.getOrganScores().entrySet()) {
                float value = entry.getValue() * instance.getStackRatio();
                builder.addScore(entry.getKey(), value);
                builder.addSlotScore(slot, entry.getKey());
            }
            if (!data.isPseudoOrgan() && instance.getCompatibilityLevel() < 1) {
                builder.addScore(CCOrganScores.INCOMPATIBILITY, 1.0F);
                builder.addSlotScore(slot, CCOrganScores.INCOMPATIBILITY);
            }
        }

        return builder.finishScores().build();
    }

    public Map<String, Float> getScoreValues() {
        if (scoreValuesView == null) {
            scoreValuesView = buildScoreView(indexedScoreValues);
        }
        return scoreValuesView;
    }

    public Map<String, Float> getBaselineScoreValues() {
        if (baselineScoreValuesView == null) {
            baselineScoreValuesView = buildScoreView(indexedBaselineScoreValues);
        }
        return baselineScoreValuesView;
    }

    public float getScoreValue(String scoreId) {
        return getScoreValue(ChestCavityApis.SCORES.getIndex(scoreId));
    }

    public float getScoreValue(int scoreIndex) {
        return scoreIndex < 0 || scoreIndex >= indexedScoreValues.length ? 0.0F : indexedScoreValues[scoreIndex];
    }

    public float getScoreValue(ScoreRef scoreRef) {
        return scoreRef == null ? 0.0F : getScoreValue(ChestCavityApis.SCORES.getIndex(scoreRef));
    }

    public float getBaselineScoreValue(String scoreId) {
        return getBaselineScoreValue(ChestCavityApis.SCORES.getIndex(scoreId));
    }

    public float getBaselineScoreValue(int scoreIndex) {
        return scoreIndex < 0 || scoreIndex >= indexedBaselineScoreValues.length ? 0.0F : indexedBaselineScoreValues[scoreIndex];
    }

    public float getBaselineScoreValue(ScoreRef scoreRef) {
        return scoreRef == null ? 0.0F : getBaselineScoreValue(ChestCavityApis.SCORES.getIndex(scoreRef));
    }

    public float getDeltaScoreValue(String scoreId) {
        return getDeltaScoreValue(ChestCavityApis.SCORES.getIndex(scoreId));
    }

    public float getDeltaScoreValue(int scoreIndex) {
        return scoreIndex < 0 || scoreIndex >= indexedDeltaScoreValues.length ? 0.0F : indexedDeltaScoreValues[scoreIndex];
    }

    public float getDeltaScoreValue(ScoreRef scoreRef) {
        return scoreRef == null ? 0.0F : getDeltaScoreValue(ChestCavityApis.SCORES.getIndex(scoreRef));
    }

    public ItemStack getOrgan(int slot) {
        if (slot < 0 || slot >= slotStacks.length) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slotStacks[slot];
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public OrganData getOrganData(int slot) {
        if (slot < 0 || slot >= slotOrganData.length) {
            return null;
        }
        return slotOrganData[slot];
    }

    public int[] getOccupiedSlots() {
        return copy(occupiedSlots);
    }

    public int[] getSlotsByItem(ResourceLocation itemId) {
        int[] slots = slotsByItem.get(itemId);
        return slots == null ? new int[0] : copy(slots);
    }

    public int[] getSlotsByScore(String scoreId) {
        int[] slots = slotsByScore.get(scoreId);
        return slots == null ? new int[0] : copy(slots);
    }

    public int getOrganCount(ResourceLocation itemId) {
        int count = 0;
        int[] slots = slotsByItem.get(itemId);
        if (slots == null) {
            return 0;
        }
        for (int slot : slots) {
            ItemStack stack = slotStacks[slot];
            if (!stack.isEmpty()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static Map<String, Float> buildScoreView(float[] values) {
        if (values == null || values.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Float> result = new LinkedHashMap<>();
        for (Score score : ChestCavityApis.SCORES.getScores().values()) {
            int index = score.getIndex();
            if (index >= 0 && index < values.length && values[index] != 0.0F) {
                result.put(score.getId(), values[index]);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static int[] copy(int[] source) {
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static float[] copy(float[] source) {
        float[] copy = new float[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    private static int[] toIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static Map<String, int[]> freezeSlotMap(Map<String, Set<Integer>> source) {
        Map<String, int[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : source.entrySet()) {
            result.put(entry.getKey(), toIntArray(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<ResourceLocation, int[]> freezeSlotMapResource(Map<ResourceLocation, Set<Integer>> source) {
        Map<ResourceLocation, int[]> result = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, Set<Integer>> entry : source.entrySet()) {
            result.put(entry.getKey(), toIntArray(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(result);
    }

    private static final class Builder {
        private final ItemStack[] slotStacks;
        private final OrganData[] slotOrganData;
        private final List<Integer> occupiedSlots = new ArrayList<>();
        private final Map<ResourceLocation, Set<Integer>> itemSlots = new LinkedHashMap<>();
        private final Map<String, Set<Integer>> slotsByScore = new LinkedHashMap<>();
        private float[] indexedScoreValues = new float[Math.max(0, ChestCavityApis.SCORES.getScoreCount())];
        private float[] indexedBaselineScoreValues = new float[Math.max(0, ChestCavityApis.SCORES.getScoreCount())];
        private float[] indexedDeltaScoreValues;
        private Map<ResourceLocation, int[]> slotsByItem;

        private Builder(int slotCount) {
            slotStacks = new ItemStack[slotCount];
            slotOrganData = new OrganData[slotCount];
            for (int i = 0; i < slotCount; i++) {
                slotStacks[i] = ItemStack.EMPTY;
            }
        }

        private void setSlot(int slot, OrganInstance instance) {
            if (slot < 0 || slot >= slotStacks.length) {
                return;
            }
            OrganInstance safeInstance = instance == null ? OrganInstance.empty() : instance;
            ItemStack copy = safeInstance.getStack();
            slotStacks[slot] = copy;
            slotOrganData[slot] = safeInstance.getData();
            if (!copy.isEmpty()) {
                occupiedSlots.add(slot);
                ResourceLocation id = safeInstance.getItemId();
                if (id != null) {
                    itemSlots.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(slot);
                }
            }
        }

        private void addSlotScore(int slot, String scoreId) {
            if (scoreId != null && slot >= 0 && slot < slotStacks.length) {
                slotsByScore.computeIfAbsent(scoreId, k -> new LinkedHashSet<>()).add(slot);
            }
        }

        private void addScore(String scoreId, float value) {
            if (scoreId == null || value == 0.0F) {
                return;
            }
            Score score = ChestCavityApis.SCORES.getOrCreateScore(scoreId);
            if (score != null) {
                indexedScoreValues = ensureFloatCapacity(indexedScoreValues, score.getIndex());
                indexedScoreValues[score.getIndex()] += value;
            }
        }

        private void addBaselineScore(String scoreId, float value) {
            if (scoreId == null || value == 0.0F) {
                return;
            }
            Score score = ChestCavityApis.SCORES.getOrCreateScore(scoreId);
            if (score != null) {
                indexedBaselineScoreValues = ensureFloatCapacity(indexedBaselineScoreValues, score.getIndex());
                indexedBaselineScoreValues[score.getIndex()] += value;
            }
        }

        private Builder finishScores() {
            int deltaLength = Math.max(indexedScoreValues.length, indexedBaselineScoreValues.length);
            indexedDeltaScoreValues = new float[deltaLength];
            for (int i = 0; i < deltaLength; i++) {
                float value = i < indexedScoreValues.length ? indexedScoreValues[i] : 0.0F;
                float baseline = i < indexedBaselineScoreValues.length ? indexedBaselineScoreValues[i] : 0.0F;
                indexedDeltaScoreValues[i] = value - baseline;
            }
            slotsByItem = freezeSlotMapResource(itemSlots);
            return this;
        }

        private ChestCavityRuntime build() {
            ChestCavityRuntime runtime = new ChestCavityRuntime(this);
            return runtime;
        }

        private static float[] ensureFloatCapacity(float[] source, int index) {
            if (index < 0) {
                return source;
            }
            if (index < source.length) {
                return source;
            }
            float[] grown = new float[index + 1];
            System.arraycopy(source, 0, grown, 0, source.length);
            return grown;
        }
    }
}
