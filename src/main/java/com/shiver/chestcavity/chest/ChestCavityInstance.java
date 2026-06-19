package com.shiver.chestcavity.chest;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.chest.types.FallbackChestCavityType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ChestCavityInstance implements IInventoryChangedListener {

    private static final String ROOT_TAG = "ChestCavity";
    private static final ChestCavityType FALLBACK_TYPE = new FallbackChestCavityType();

    private ChestCavityType type;
    private EntityLivingBase owner;
    private UUID compatibilityId;
    private final Map<String, Float> organScores = new LinkedHashMap<>();
    private final Map<String, Float> oldOrganScores = new LinkedHashMap<>();

    public boolean opened;
    public final ChestCavityInventory inventory;
    public int heartBleedTimer;
    public int bloodPoisonTimer;
    public int liverTimer;
    public float metabolismRemainder;
    public float lungRemainder;
    public int furnaceProgress;
    public int photosynthesisProgress;

    public ChestCavityInstance(EntityLivingBase owner) {
        this(FALLBACK_TYPE, owner);
    }

    public ChestCavityInstance(ChestCavityType type, EntityLivingBase owner) {
        this.type = type == null ? FALLBACK_TYPE : type;
        this.owner = owner;
        this.compatibilityId = owner == null ? UUID.randomUUID() : owner.getUniqueID();
        this.inventory = new ChestCavityInventory(ChestCavityInventory.DEFAULT_SIZE, this);
        this.inventory.addInventoryChangeListener(this);
        evaluate();
    }

    public ChestCavityType getChestCavityType() {
        return type;
    }

    public void setChestCavityType(ChestCavityType type) {
        this.type = type == null ? FALLBACK_TYPE : type;
        evaluate();
    }

    public EntityLivingBase getOwner() {
        return owner;
    }

    public void setOwner(EntityLivingBase owner) {
        this.owner = owner;
        if (compatibilityId == null && owner != null) {
            compatibilityId = owner.getUniqueID();
        }
    }

    public UUID getCompatibilityId() {
        return compatibilityId;
    }

    public Map<String, Float> getOrganScores() {
        return organScores;
    }

    public Map<String, Float> getOldOrganScores() {
        return oldOrganScores;
    }

    public float getOrganScore(String id) {
        Float value = organScores.get(id);
        return value == null ? 0.0F : value;
    }

    @Override
    public void onInventoryChanged(IInventory invBasic) {
        evaluate();
    }

    public void evaluate() {
        oldOrganScores.clear();
        oldOrganScores.putAll(organScores);
        organScores.clear();

        if (!opened) {
            organScores.putAll(type.getDefaultOrganScores());
            return;
        }

        type.loadBaseOrganScores(organScores);
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                OrganData data = type.catchExceptionalOrgan(stack);
                if (data == null) {
                    data = OrganData.fromStack(stack);
                }
                if (data != null) {
                    addScores(data, stack);
                }
            }
        }
    }

    public void openFromDefaultIfNeeded() {
        if (!opened) {
            opened = true;
            type.fillChestCavityInventory(inventory);
            evaluate();
        }
    }

    public void readFromNbt(NBTTagCompound tag, EntityLivingBase owner) {
        setOwner(owner);
        if (!tag.hasKey(ROOT_TAG, 10)) {
            evaluate();
            return;
        }

        NBTTagCompound ccTag = tag.getCompoundTag(ROOT_TAG);
        opened = ccTag.getBoolean("Opened");
        heartBleedTimer = ccTag.getInteger("HeartTimer");
        bloodPoisonTimer = ccTag.getInteger("KidneyTimer");
        liverTimer = ccTag.getInteger("LiverTimer");
        metabolismRemainder = ccTag.getFloat("MetabolismRemainder");
        lungRemainder = ccTag.getFloat("LungRemainder");
        furnaceProgress = ccTag.getInteger("FurnaceProgress");
        photosynthesisProgress = ccTag.getInteger("PhotosynthesisProgress");
        compatibilityId = ccTag.hasUniqueId("CompatibilityId")
                ? ccTag.getUniqueId("CompatibilityId")
                : (owner == null ? UUID.randomUUID() : owner.getUniqueID());

        if (ccTag.hasKey("Inventory", 9)) {
            inventory.readTags(ccTag.getTagList("Inventory", 10));
        }
        evaluate();
    }

    public void writeToNbt(NBTTagCompound tag) {
        NBTTagCompound ccTag = new NBTTagCompound();
        ccTag.setBoolean("Opened", opened);
        ccTag.setUniqueId("CompatibilityId", compatibilityId == null ? UUID.randomUUID() : compatibilityId);
        ccTag.setInteger("HeartTimer", heartBleedTimer);
        ccTag.setInteger("KidneyTimer", bloodPoisonTimer);
        ccTag.setInteger("LiverTimer", liverTimer);
        ccTag.setFloat("MetabolismRemainder", metabolismRemainder);
        ccTag.setFloat("LungRemainder", lungRemainder);
        ccTag.setInteger("FurnaceProgress", furnaceProgress);
        ccTag.setInteger("PhotosynthesisProgress", photosynthesisProgress);
        ccTag.setTag("Inventory", inventory.getTags());
        tag.setTag(ROOT_TAG, ccTag);
    }

    public void copyFrom(ChestCavityInstance other) {
        opened = other.opened;
        type = other.type;
        compatibilityId = other.compatibilityId;
        heartBleedTimer = other.heartBleedTimer;
        bloodPoisonTimer = other.bloodPoisonTimer;
        liverTimer = other.liverTimer;
        metabolismRemainder = other.metabolismRemainder;
        lungRemainder = other.lungRemainder;
        furnaceProgress = other.furnaceProgress;
        photosynthesisProgress = other.photosynthesisProgress;
        NBTTagList tags = other.inventory.getTags();
        inventory.readTags(tags);
        evaluate();
    }

    private void addScores(OrganData data, ItemStack stack) {
        float stackRatio = Math.min((float) stack.getCount() / (float) stack.getMaxStackSize(), 1.0F);
        for (Map.Entry<String, Float> entry : data.getOrganScores().entrySet()) {
            Float old = organScores.get(entry.getKey());
            float value = entry.getValue() * stackRatio;
            organScores.put(entry.getKey(), old == null ? value : old + value);
        }
    }
}
