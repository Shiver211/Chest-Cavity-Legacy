package com.shiver.chestcavity.integration.crafttweaker.runtime;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CtUtil {

    public static final int CHEST_CAVITY_SLOT_COUNT = 27;

    private CtUtil() {
    }

    public static ResourceLocation id(String raw) {
        return raw == null || raw.trim().isEmpty() ? null : new ResourceLocation(raw);
    }

    public static ResourceLocation requireId(String raw, String kind) {
        ResourceLocation id = id(raw);
        if (id == null) {
            throw new IllegalArgumentException(kind + " id 不能为空");
        }
        return id;
    }

    public static ResourceLocation itemId(IItemStack stack) {
        ItemStack mcStack = toMcStack(stack);
        if (mcStack.isEmpty() || mcStack.getItem() == null) {
            return null;
        }
        return mcStack.getItem().getRegistryName();
    }

    public static ItemStack toMcStack(IItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return CraftTweakerMC.getItemStack(stack).copy();
    }

    public static ItemStack toMcStack(IItemStack stack, int count) {
        ItemStack mcStack = toMcStack(stack);
        if (mcStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        mcStack.setCount(count);
        return mcStack;
    }

    public static Item resolveItem(ResourceLocation itemId) {
        return itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
    }

    public static void validateSlot(int slot) {
        if (slot < 0 || slot >= CHEST_CAVITY_SLOT_COUNT) {
            throw new IllegalArgumentException("slot 必须在 0..26 之间");
        }
    }

    public static void validateCount(int count, ItemStack stack) {
        if (count <= 0) {
            throw new IllegalArgumentException("count 必须大于 0");
        }
        if (!stack.isEmpty() && count > stack.getMaxStackSize()) {
            throw new IllegalArgumentException("count 不能超过物品自身 maxStackSize");
        }
    }

    public static Map<ResourceLocation, Float> singletonScore(ResourceLocation scoreId, float value) {
        Map<ResourceLocation, Float> scores = new LinkedHashMap<ResourceLocation, Float>();
        scores.put(scoreId, value);
        return scores;
    }

    public static void logRegistration(String message, Object... args) {
        CraftTweakerAPI.logInfo(String.format(message, args));
    }

    public static Object unwrapIngredient(IIngredient ingredient) {
        return ingredient == null ? null : ingredient.getInternal();
    }
}
