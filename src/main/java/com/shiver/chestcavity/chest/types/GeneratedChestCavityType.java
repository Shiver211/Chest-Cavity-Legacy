package com.shiver.chestcavity.chest.types;

import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 可通过数据文件动态组装的胸腔类型实现。
 */
public class GeneratedChestCavityType implements ChestCavityType {

    private ChestCavityInventory defaultChestCavity = new ChestCavityInventory();
    private final Map<String, Float> baseOrganScores = new LinkedHashMap<>();
    private final List<ExceptionalOrgan> exceptionalOrgans = new ArrayList<>();
    private final List<Integer> forbiddenSlots = new ArrayList<>();
    private Map<String, Float> defaultOrganScores;
    private List<ItemStack> droppableOrgans;
    private float dropRateMultiplier = 1.0F;
    private boolean bossChestCavity;
    private boolean playerChestCavity;

    /**
     * 返回该类型的默认器官分数字典，并在首次访问时构建缓存。
     *
     * @return 默认器官分数字典。
     */
    @Override
    public Map<String, Float> getDefaultOrganScores() {
        if (defaultOrganScores == null) {
            defaultOrganScores = new LinkedHashMap<>();
            loadBaseOrganScores(defaultOrganScores);
            addInventoryOrganScores(defaultOrganScores, defaultChestCavity);
        }
        return defaultOrganScores;
    }

    /**
     * 返回指定分数项的默认值。
     *
     * @param id 分数标识。
     * @return 默认分值。
     */
    @Override
    public float getDefaultOrganScore(String id) {
        Float score = getDefaultOrganScores().get(id);
        return score == null ? 0.0F : score;
    }

    /**
     * 返回默认胸腔布局。
     *
     * @return 默认胸腔物品栏。
     */
    @Override
    public ChestCavityInventory getDefaultChestCavity() {
        return defaultChestCavity;
    }

    /**
     * 设置默认胸腔布局，并清理派生缓存。
     *
     * @param defaultChestCavity 新的默认胸腔布局。
     */
    public void setDefaultChestCavity(ChestCavityInventory defaultChestCavity) {
        this.defaultChestCavity = defaultChestCavity == null ? new ChestCavityInventory() : defaultChestCavity;
        clearDerivedCache();
    }

    /**
     * 返回基础器官分数字典的只读视图。
     *
     * @return 基础器官分数字典。
     */
    public Map<String, Float> getBaseOrganScores() {
        return Collections.unmodifiableMap(baseOrganScores);
    }

    /**
     * 添加或覆盖一个基础器官分数。
     *
     * @param id 分数标识。
     * @param value 分数值。
     */
    public void addBaseOrganScore(String id, float value) {
        if (id != null) {
            baseOrganScores.put(id, value);
            clearDerivedCache();
        }
    }

    /**
     * 移除一个基础器官分数。
     *
     * @param id 分数标识。
     */
    public void removeBaseOrganScore(String id) {
        if (id != null) {
            baseOrganScores.remove(id);
            clearDerivedCache();
        }
    }

    /**
     * 用新的分数字典整体替换基础器官分数。
     *
     * @param scores 新的基础器官分数字典。
     */
    public void setBaseOrganScores(Map<String, Float> scores) {
        baseOrganScores.clear();
        if (scores != null) {
            baseOrganScores.putAll(scores);
        }
        clearDerivedCache();
    }

    /**
     * 设置默认胸腔中的某个槽位。
     *
     * @param index 槽位索引。
     * @param stack 要放入的物品。
     */
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < defaultChestCavity.size()) {
            defaultChestCavity.setStack(index, stack == null ? ItemStack.EMPTY : stack.copy());
            clearDerivedCache();
        }
    }

    /**
     * 清空默认胸腔中的全部槽位。
     */
    public void clearSlots() {
        defaultChestCavity.clear();
        clearDerivedCache();
    }

    /**
     * 返回禁止使用的槽位列表。
     *
     * @return 禁用槽位列表。
     */
    public List<Integer> getForbiddenSlots() {
        return Collections.unmodifiableList(forbiddenSlots);
    }

    /**
     * 整体设置禁止使用的槽位列表。
     *
     * @param slots 新的禁用槽位列表。
     */
    public void setForbiddenSlots(List<Integer> slots) {
        forbiddenSlots.clear();
        if (slots != null) {
            forbiddenSlots.addAll(slots);
        }
        clearDerivedCache();
    }

    /**
     * 添加一个禁止使用的槽位。
     *
     * @param slot 槽位索引。
     */
    public void addForbiddenSlot(int slot) {
        if (!forbiddenSlots.contains(slot)) {
            forbiddenSlots.add(slot);
            clearDerivedCache();
        }
    }

    /**
     * 移除一个禁止使用的槽位。
     *
     * @param slot 槽位索引。
     */
    public void removeForbiddenSlot(int slot) {
        if (forbiddenSlots.remove(Integer.valueOf(slot))) {
            clearDerivedCache();
        }
    }

    /**
     * 判断指定槽位是否被禁用。
     *
     * @param index 槽位索引。
     * @return `true` 表示该槽位不可用。
     */
    @Override
    public boolean isSlotForbidden(int index) {
        return forbiddenSlots.contains(index);
    }

    /**
     * 用默认布局填充目标胸腔物品栏。
     *
     * @param chestCavity 要填充的胸腔物品栏。
     */
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

    /**
     * 将基础器官分数写入目标字典。
     *
     * @param organScores 要写入的分数字典。
     */
    @Override
    public void loadBaseOrganScores(Map<String, Float> organScores) {
        organScores.clear();
        organScores.putAll(baseOrganScores);
    }

    /**
     * 尝试把普通物品识别为特殊器官数据。
     *
     * @param stack 要检查的物品堆。
     * @return 特殊器官数据，或注册表中的普通器官数据。
     */
    @Override
    public OrganData catchExceptionalOrgan(ItemStack stack) {
        for (ExceptionalOrgan exceptionalOrgan : exceptionalOrgans) {
            if (exceptionalOrgan.matches(stack)) {
                OrganData data = new OrganData();
                data.setPseudoOrgan(true);
                data.setOrganScores(exceptionalOrgan.scores);
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
        return OrganManager.get(stack);
    }

    /**
     * 返回默认胸腔布局中可掉落的真实器官列表。
     *
     * @return 可掉落器官列表。
     */
    @Override
    public List<ItemStack> getDroppableOrgans() {
        if (droppableOrgans == null) {
            droppableOrgans = new LinkedList<>();
            for (int i = 0; i < defaultChestCavity.size(); i++) {
                ItemStack stack = defaultChestCavity.getStack(i);
                if (OrganManager.isTrueOrgan(stack)) {
                    droppableOrgans.add(stack.copy());
                }
            }
        }
        return Collections.unmodifiableList(droppableOrgans);
    }

    /**
     * 判断该胸腔类型是否属于 Boss。
     *
     * @return `true` 表示这是 Boss 胸腔。
     */
    @Override
    public boolean isBossChestCavity() {
        return bossChestCavity;
    }

    /**
     * 设置该胸腔类型是否属于 Boss。
     *
     * @param bossChestCavity 是否属于 Boss。
     */
    public void setBossChestCavity(boolean bossChestCavity) {
        this.bossChestCavity = bossChestCavity;
    }

    /**
     * 判断该胸腔类型是否属于玩家。
     *
     * @return `true` 表示这是玩家胸腔。
     */
    @Override
    public boolean isPlayerChestCavity() {
        return playerChestCavity;
    }

    /**
     * 设置该胸腔类型是否属于玩家。
     *
     * @param playerChestCavity 是否属于玩家。
     */
    public void setPlayerChestCavity(boolean playerChestCavity) {
        this.playerChestCavity = playerChestCavity;
    }

    /**
     * 返回该胸腔类型的器官掉率倍率。
     *
     * @return 器官掉率倍率。
     */
    @Override
    public float getDropRateMultiplier() {
        return dropRateMultiplier;
    }

    /**
     * 设置该胸腔类型的器官掉率倍率。
     *
     * @param dropRateMultiplier 器官掉率倍率。
     */
    public void setDropRateMultiplier(float dropRateMultiplier) {
        this.dropRateMultiplier = dropRateMultiplier;
    }

    /**
     * 清空根据默认布局推导出来的缓存数据。
     */
    public void clearDerivedCache() {
        defaultOrganScores = null;
        droppableOrgans = null;
    }

    /**
     * 整体设置特殊器官匹配规则。
     *
     * @param organs 特殊器官规则列表。
     */
    public void setExceptionalOrgans(List<ExceptionalOrgan> organs) {
        exceptionalOrgans.clear();
        if (organs != null) {
            exceptionalOrgans.addAll(organs);
        }
        clearDerivedCache();
    }

    /**
     * 添加一条特殊器官匹配规则。
     *
     * @param item 要匹配的具体物品，可为 `null`。
     * @param oreName 要匹配的矿辞名称，可为 `null`。
     * @param scores 匹配成功后附加的器官分数。
     */
    public void addExceptionalOrgan(Item item, String oreName, Map<String, Float> scores) {
        exceptionalOrgans.add(new ExceptionalOrgan(item, oreName, scores));
        clearDerivedCache();
    }

    /**
     * 清空全部特殊器官匹配规则。
     */
    public void clearExceptionalOrgans() {
        exceptionalOrgans.clear();
        clearDerivedCache();
    }

    /**
     * 将默认胸腔布局中的器官分数累加到目标字典中。
     *
     * @param scores 目标分数字典。
     * @param inventory 提供默认布局的胸腔物品栏。
     */
    private void addInventoryOrganScores(Map<String, Float> scores, ChestCavityInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
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
    }

    /**
     * 判断一个物品是否属于门或活板门，从而可被视作特殊器官。
     *
     * @param stack 要检查的物品堆。
     * @return `true` 表示它是门类物品。
     */
    private boolean isDoorOrTrapdoor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof ItemDoor || Block.getBlockFromItem(item) instanceof BlockTrapDoor;
    }

    /**
     * 描述一条“普通物品可视作器官”的特殊匹配规则。
     */
    public static final class ExceptionalOrgan {
        private final Item item;
        private final String oreName;
        private final Map<String, Float> scores;

        /**
         * 创建一条特殊器官匹配规则。
         *
         * @param item 要匹配的具体物品，可为 `null`。
         * @param oreName 要匹配的矿辞名称，可为 `null`。
         * @param scores 匹配成功后使用的器官分数。
         */
        public ExceptionalOrgan(Item item, String oreName, Map<String, Float> scores) {
            this.item = item;
            this.oreName = oreName;
            this.scores = new LinkedHashMap<>();
            if (scores != null) {
                this.scores.putAll(scores);
            }
        }

        /**
         * 判断给定物品是否命中当前特殊器官规则。
         *
         * @param stack 要检查的物品堆。
         * @return `true` 表示匹配成功。
         */
        private boolean matches(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return false;
            }
            if (item != null && stack.getItem() == item) {
                return true;
            }
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
    }
}
