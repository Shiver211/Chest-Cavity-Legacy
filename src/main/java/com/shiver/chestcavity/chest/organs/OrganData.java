package com.shiver.chestcavity.chest.organs;

import com.shiver.chestcavity.integration.crafttweaker.runtime.IOrganDefinitionProvider;
import com.shiver.chestcavity.integration.crafttweaker.runtime.OrganRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrganData {

    public static final String ORGAN_TAG = "ChestCavityOrganData";
    private static final String PSEUDO_TAG = "PseudoOrgan";
    private static final String SCORES_TAG = "OrganScores";
    private static final Map<ResourceLocation, OrganData> ORGAN_DATA = new LinkedHashMap<>();

    private boolean pseudoOrgan;
    private Map<ResourceLocation, Float> organScores = new LinkedHashMap<>();

    public static void clearRegistry() {
        ORGAN_DATA.clear();
    }

    public static void register(ResourceLocation itemId, OrganData data) {
        if (itemId != null && data != null) {
            ORGAN_DATA.put(itemId, data);
        }
    }

    public static OrganData get(ResourceLocation itemId) {
        return ORGAN_DATA.get(itemId);
    }

    public static OrganData fromRegistry(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        if (stack.getItem() instanceof IOrganDefinitionProvider) {
            OrganData data = ((IOrganDefinitionProvider) stack.getItem()).getOrganData();
            if (data != null) {
                return data;
            }
        }
        OrganData data = OrganRegistry.getOrganData(stack);
        return data == null ? get(stack.getItem().getRegistryName()) : data;
    }

    public static Map<ResourceLocation, OrganData> getRegistry() {
        return Collections.unmodifiableMap(ORGAN_DATA);
    }

    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    public void setPseudoOrgan(boolean pseudoOrgan) {
        this.pseudoOrgan = pseudoOrgan;
    }

    public Map<ResourceLocation, Float> getOrganScores() {
        return organScores;
    }

    public void setOrganScores(Map<ResourceLocation, Float> organScores) {
        this.organScores = new LinkedHashMap<>();
        if (organScores != null) {
            this.organScores.putAll(organScores);
        }
    }

    public Map<ResourceLocation, Float> getOrganScoresView() {
        return Collections.unmodifiableMap(organScores);
    }

    public void writeToStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        root.setTag(ORGAN_TAG, toNbt());
    }

    public NBTTagCompound toNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(PSEUDO_TAG, pseudoOrgan);
        NBTTagCompound scoresTag = new NBTTagCompound();
        for (Map.Entry<ResourceLocation, Float> entry : organScores.entrySet()) {
            scoresTag.setFloat(entry.getKey().toString(), entry.getValue());
        }
        tag.setTag(SCORES_TAG, scoresTag);
        return tag;
    }

    public static OrganData fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(ORGAN_TAG, 10)) {
            return null;
        }
        return fromNbt(root.getCompoundTag(ORGAN_TAG));
    }

    public static OrganData fromNbt(NBTTagCompound tag) {
        OrganData data = new OrganData();
        data.pseudoOrgan = tag.getBoolean(PSEUDO_TAG);
        NBTTagCompound scoresTag = tag.getCompoundTag(SCORES_TAG);
        for (String key : scoresTag.getKeySet()) {
            ResourceLocation id = new ResourceLocation(key);
            data.organScores.put(id, scoresTag.getFloat(key));
        }
        return data;
    }
}
