package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.content.BodyTypeDef;
import com.shiver.chestcavity.content.ExceptionalOrganDef;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class CompiledChestCavityType implements ChestCavityType {

    private final ChestCavityInventory defaultChestCavity;
    private final Map<String, Float> baseOrganScores;
    private final List<ExceptionalOrganDef> exceptionalOrgans;
    private final List<Integer> forbiddenSlots;
    private final Map<String, Float> defaultOrganScores;
    private final List<ItemStack> droppableOrgans;
    private final float dropRateMultiplier;
    private final boolean bossChestCavity;
    private final boolean playerChestCavity;
    private final ResourceLocation layoutId;
    private final Map<ResourceLocation, OrganData> organs;

    public CompiledChestCavityType(BodyTypeDef def, Map<ResourceLocation, OrganData> organs) {
        BodyTypeDef source = def == null ? new BodyTypeDef("empty") : def.copy();
        this.defaultChestCavity = source.getDefaultChestCavity();
        this.baseOrganScores = Collections.unmodifiableMap(new LinkedHashMap<>(source.getBaseOrganScores()));
        this.exceptionalOrgans = Collections.unmodifiableList(new ArrayList<>(source.getExceptionalOrgans()));
        this.forbiddenSlots = Collections.unmodifiableList(new ArrayList<>(source.getForbiddenSlots()));
        this.dropRateMultiplier = source.getDropRateMultiplier();
        this.bossChestCavity = source.isBossChestCavity();
        this.playerChestCavity = source.isPlayerChestCavity();
        this.layoutId = source.getLayoutId();
        this.organs = copyOrganMap(organs);
        this.defaultOrganScores = Collections.unmodifiableMap(buildDefaultOrganScores());
        this.droppableOrgans = Collections.unmodifiableList(buildDroppableOrgans());
    }

    @Override
    public Map<String, Float> getDefaultOrganScores() {
        return defaultOrganScores;
    }

    @Override
    public float getDefaultOrganScore(String id) {
        Float score = defaultOrganScores.get(id);
        return score == null ? 0.0F : score;
    }

    @Override
    public ChestCavityInventory getDefaultChestCavity() {
        return copyInventory(defaultChestCavity);
    }

    @Override
    public boolean isSlotForbidden(int index) {
        return forbiddenSlots.contains(index);
    }

    @Override
    public void fillChestCavityInventory(ChestCavityInventory chestCavity) {
        if (chestCavity == null) {
            return;
        }
        chestCavity.clear();
        for (int i = 0; i < chestCavity.size() && i < defaultChestCavity.size(); i++) {
            ItemStack stack = defaultChestCavity.getStack(i);
            chestCavity.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    @Override
    public void loadBaseOrganScores(Map<String, Float> organScores) {
        if (organScores != null) {
            organScores.clear();
            organScores.putAll(baseOrganScores);
        }
    }

    @Override
    public OrganData catchExceptionalOrgan(ItemStack stack) {
        for (ExceptionalOrganDef exceptionalOrgan : exceptionalOrgans) {
            if (matches(exceptionalOrgan, stack)) {
                OrganData data = new OrganData();
                data.setPseudoOrgan(true);
                data.setOrganScores(exceptionalOrgan.getScores());
                return data;
            }
        }
        if (isDoorOrTrapdoor(stack)) {
            OrganData data = new OrganData();
            Map<String, Float> scores = new LinkedHashMap<>();
            scores.put(CCOrganScores.EASE_OF_ACCESS, (float) stack.getMaxStackSize());
            data.setPseudoOrgan(true);
            data.setOrganScores(scores);
            return data;
        }
        ResourceLocation itemId = getItemId(stack);
        OrganData data = itemId == null ? null : organs.get(itemId);
        return data == null ? null : copyData(data);
    }

    @Override
    public List<ItemStack> getDroppableOrgans() {
        return copyStacks(droppableOrgans);
    }

    @Override
    public boolean isBossChestCavity() {
        return bossChestCavity;
    }

    @Override
    public boolean isPlayerChestCavity() {
        return playerChestCavity;
    }

    @Override
    public float getDropRateMultiplier() {
        return dropRateMultiplier;
    }

    @Override
    public ResourceLocation getLayoutId() {
        return layoutId;
    }

    private Map<String, Float> buildDefaultOrganScores() {
        Map<String, Float> scores = new LinkedHashMap<>(baseOrganScores);
        for (int i = 0; i < defaultChestCavity.size(); i++) {
            ItemStack stack = defaultChestCavity.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            OrganData data = catchExceptionalOrgan(stack);
            if (data == null) {
                continue;
            }
            float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
            for (Map.Entry<String, Float> entry : data.getOrganScores().entrySet()) {
                Float old = scores.get(entry.getKey());
                float value = entry.getValue() * stackRatio;
                scores.put(entry.getKey(), old == null ? value : old + value);
            }
        }
        return scores;
    }

    private List<ItemStack> buildDroppableOrgans() {
        List<ItemStack> result = new LinkedList<>();
        for (int i = 0; i < defaultChestCavity.size(); i++) {
            ItemStack stack = defaultChestCavity.getStack(i);
            OrganData data = catchExceptionalOrgan(stack);
            if (data != null && !data.isPseudoOrgan()) {
                result.add(stack.copy());
            }
        }
        return result;
    }

    private boolean matches(ExceptionalOrganDef def, ItemStack stack) {
        if (def == null || stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = def.getItemId();
        if (itemId != null && itemId.equals(getItemId(stack))) {
            return true;
        }
        String oreName = def.getOreName();
        if (oreName == null || oreName.isEmpty()) {
            return false;
        }
        for (ItemStack oreStack : OreDictionary.getOres(oreName, false)) {
            if (OreDictionary.itemMatches(oreStack, stack, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDoorOrTrapdoor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof ItemDoor || Block.getBlockFromItem(item) instanceof BlockTrapDoor;
    }

    private static ResourceLocation getItemId(ItemStack stack) {
        return stack == null || stack.isEmpty() || stack.getItem() == null ? null : stack.getItem().getRegistryName();
    }

    private static Map<ResourceLocation, OrganData> copyOrganMap(Map<ResourceLocation, OrganData> source) {
        Map<ResourceLocation, OrganData> copy = new LinkedHashMap<>();
        if (source != null) {
            for (Map.Entry<ResourceLocation, OrganData> entry : source.entrySet()) {
                if (entry.getKey() != null && ForgeRegistries.ITEMS.getValue(entry.getKey()) != null) {
                    copy.put(entry.getKey(), copyData(entry.getValue()));
                }
            }
        }
        return Collections.unmodifiableMap(copy);
    }

    private static OrganData copyData(OrganData source) {
        OrganData copy = new OrganData();
        if (source != null) {
            copy.setPseudoOrgan(source.isPseudoOrgan());
            copy.setOrganScores(source.getOrganScores());
        }
        return copy;
    }

    private static ChestCavityInventory copyInventory(ChestCavityInventory source) {
        ChestCavityInventory copy = new ChestCavityInventory(source == null ? 0 : source.size());
        if (source != null) {
            for (int i = 0; i < source.size(); i++) {
                ItemStack stack = source.getStack(i);
                copy.setStack(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        return copy;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> source) {
        List<ItemStack> copy = new ArrayList<>();
        if (source != null) {
            for (ItemStack stack : source) {
                copy.add(stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        return Collections.unmodifiableList(copy);
    }
}
