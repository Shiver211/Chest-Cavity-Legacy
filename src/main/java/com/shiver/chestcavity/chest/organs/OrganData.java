package com.shiver.chestcavity.chest.organs;

import com.shiver.chestcavity.content.ContentRegistry;
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

    private boolean pseudoOrgan;
    private Map<String, Float> organScores = new LinkedHashMap<>();

    public static OrganData get(ResourceLocation itemId) {
        return ContentRegistry.getCompiled().getOrgan(itemId);
    }

    public static OrganData get(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        return get(stack.getItem().getRegistryName());
    }

    public boolean isPseudoOrgan() {
        return pseudoOrgan;
    }

    public void setPseudoOrgan(boolean pseudoOrgan) {
        this.pseudoOrgan = pseudoOrgan;
    }

    public Map<String, Float> getOrganScores() {
        return organScores;
    }

    public void setOrganScores(Map<String, Float> organScores) {
        this.organScores = new LinkedHashMap<>();
        if (organScores != null) {
            this.organScores.putAll(organScores);
        }
    }

    public Map<String, Float> getOrganScoresView() {
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
        for (Map.Entry<String, Float> entry : organScores.entrySet()) {
            scoresTag.setFloat(entry.getKey(), entry.getValue());
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
            data.organScores.put(key, scoresTag.getFloat(key));
        }
        return data;
    }
}
