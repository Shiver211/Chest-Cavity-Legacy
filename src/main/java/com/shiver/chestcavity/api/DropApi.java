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

public final class DropApi {

    private final Map<ResourceLocation, DropTable> drops = new LinkedHashMap<>();

    DropApi() {
    }

    public void addOrganDrop(ResourceLocation entityId, ItemStack stack, int weight) {
        if (entityId == null || stack == null || stack.isEmpty() || weight <= 0) {
            return;
        }
        table(entityId).entries.add(new DropEntry(stack.copy(), weight));
    }

    public void setDropProbability(ResourceLocation entityId, float value) {
        if (entityId != null) {
            table(entityId).probability = Math.max(0.0F, Math.min(1.0F, value));
        }
    }

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

    public void removeAllOrganDrops(ResourceLocation entityId) {
        if (entityId != null) {
            drops.remove(entityId);
        }
    }

    public List<ItemStack> generateDrops(ResourceLocation entityId, EntityLivingBase entity, Random random) {
        List<ItemStack> result = new ArrayList<>();
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

    private DropTable table(ResourceLocation entityId) {
        DropTable table = drops.get(entityId);
        if (table == null) {
            table = new DropTable();
            drops.put(entityId, table);
        }
        return table;
    }

    private static final class DropTable {
        private final List<DropEntry> entries = new ArrayList<>();
        private float probability = 1.0F;

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

    private static final class DropEntry {
        private final ItemStack stack;
        private final int weight;

        private DropEntry(ItemStack stack, int weight) {
            this.stack = stack;
            this.weight = weight;
        }
    }
}
