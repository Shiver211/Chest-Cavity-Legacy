package com.shiver.chestcavity.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 对外暴露自定义器官掉落表的维护与生成接口。
 */
public final class DropApi {

    private final Map<ResourceLocation, DropTable> drops = new LinkedHashMap<ResourceLocation, DropTable>();

    /**
     * 仅允许通过统一 API 单例创建。
     */
    DropApi() {
    }

    /**
     * 为指定实体添加一个器官掉落候选项。
     *
     * @param entityId 实体注册名。
     * @param stack 掉落物品。
     * @param weight 抽取权重。
     */
    public void addOrganDrop(ResourceLocation entityId, ItemStack stack, int weight) {
        if (entityId == null || stack == null || stack.isEmpty() || weight <= 0) {
            return;
        }
        table(entityId).entries.add(new DropEntry(stack.copy(), weight));
    }

    /**
     * 设置指定实体掉落表被触发的总概率。
     *
     * @param entityId 实体注册名。
     * @param value 掉落概率。
     */
    public void setDropProbability(ResourceLocation entityId, float value) {
        if (entityId != null) {
            table(entityId).probability = Math.max(0.0F, Math.min(1.0F, value));
        }
    }

    /**
     * 从指定实体的掉落表中移除一个器官候选项。
     *
     * @param entityId 实体注册名。
     * @param stack 要移除的掉落物品。
     */
    public void removeOrganDrop(ResourceLocation entityId, ItemStack stack) {
        DropTable table = drops.get(entityId);
        if (table == null || stack == null || stack.isEmpty()) {
            return;
        }
        for (Iterator<DropEntry> iterator = table.entries.iterator(); iterator.hasNext();) {
            ItemStack registered = iterator.next().stack;
            if (ItemStack.areItemsEqual(registered, stack) && ItemStack.areItemStackTagsEqual(registered, stack)) {
                iterator.remove();
            }
        }
    }

    /**
     * 清空指定实体的全部器官掉落配置。
     *
     * @param entityId 实体注册名。
     */
    public void removeAllOrganDrops(ResourceLocation entityId) {
        if (entityId != null) {
            drops.remove(entityId);
        }
    }

    /**
     * 根据掉落表为指定实体生成器官掉落结果。
     *
     * @param entityId 实体注册名。
     * @param entity 掉落来源实体。
     * @param random 随机源。
     * @return 生成出的掉落列表。
     */
    public List<ItemStack> generateDrops(ResourceLocation entityId, EntityLivingBase entity, Random random) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        DropTable table = drops.get(entityId);
        if (table == null || table.entries.isEmpty() || random == null) {
            return result;
        }
        if (random.nextFloat() > table.probability) {
            return result;
        }
        DropEntry entry = table.roll(random);
        if (entry != null) {
            result.add(entry.stack.copy());
        }
        return result;
    }

    /**
     * 返回指定实体对应的掉落表；如果不存在则自动创建。
     *
     * @param entityId 实体注册名。
     * @return 对应的掉落表。
     */
    private DropTable table(ResourceLocation entityId) {
        DropTable table = drops.get(entityId);
        if (table == null) {
            table = new DropTable();
            drops.put(entityId, table);
        }
        return table;
    }

    /**
     * 表示一个实体的掉落表。
     */
    private static final class DropTable {
        private final List<DropEntry> entries = new ArrayList<DropEntry>();
        private float probability = 1.0F;

        /**
         * 按权重从掉落表中抽取一个结果。
         *
         * @param random 随机源。
         * @return 抽中的掉落项；如果无法抽取则返回 `null`。
         */
        private DropEntry roll(Random random) {
            int totalWeight = 0;
            for (DropEntry entry : entries) {
                totalWeight += entry.weight;
            }
            if (totalWeight <= 0) {
                return null;
            }
            int roll = random.nextInt(totalWeight);
            for (DropEntry entry : entries) {
                roll -= entry.weight;
                if (roll < 0) {
                    return entry;
                }
            }
            return null;
        }
    }

    /**
     * 表示掉落表中的一个候选项。
     */
    private static final class DropEntry {
        private final ItemStack stack;
        private final int weight;

        /**
         * 创建一个掉落候选项。
         *
         * @param stack 掉落物品。
         * @param weight 抽取权重。
         */
        private DropEntry(ItemStack stack, int weight) {
            this.stack = stack;
            this.weight = weight;
        }
    }
}
