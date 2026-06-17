package com.shiver.chestcavity.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class ChestCavityData implements IChestCavity {

    public static final int DEFAULT_SLOT_COUNT = 27;

    private EntityLivingBase owner;
    private boolean opened;
    private UUID compatibilityId = UUID.randomUUID();
    private NonNullList<ItemStack> organs = NonNullList.withSize(DEFAULT_SLOT_COUNT, ItemStack.EMPTY);
    private final Map<ResourceLocation, Float> organScores = new HashMap<>();
    private final Map<ResourceLocation, Float> oldOrganScores = new HashMap<>();
    private final IItemHandlerModifiable organInventory = new OrganItemHandler();

    private int heartBleedTimer;
    private int bloodPoisonTimer;
    private int liverTimer;
    private float metabolismRemainder;
    private float lungRemainder;
    private int furnaceProgress;
    private int photosynthesisProgress;
    private int connectedCrystalId = -1;
    private final Queue<ResourceLocation> projectileQueue = new LinkedList<>();

    @Override
    public EntityLivingBase getOwner() {
        return owner;
    }

    @Override
    public void setOwner(EntityLivingBase owner) {
        this.owner = owner;
        if (owner != null && compatibilityId == null) {
            compatibilityId = owner.getUniqueID();
        }
    }

    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public UUID getCompatibilityId() {
        if (compatibilityId == null) {
            compatibilityId = owner == null ? UUID.randomUUID() : owner.getUniqueID();
        }
        return compatibilityId;
    }

    @Override
    public void setCompatibilityId(UUID compatibilityId) {
        this.compatibilityId = compatibilityId;
    }

    @Override
    public int getSlotCount() {
        return organs.size();
    }

    @Override
    public NonNullList<ItemStack> getOrgans() {
        return organs;
    }

    @Override
    public IItemHandlerModifiable getOrganInventory() {
        return organInventory;
    }

    @Override
    public ItemStack getOrgan(int slot) {
        return organs.get(slot);
    }

    @Override
    public void setOrgan(int slot, ItemStack stack) {
        setOrganInternal(slot, stack, true);
    }

    @Override
    public Map<ResourceLocation, Float> getOrganScores() {
        return organScores;
    }

    @Override
    public Map<ResourceLocation, Float> getOldOrganScores() {
        return oldOrganScores;
    }

    @Override
    public float getOrganScore(ResourceLocation id) {
        Float value = organScores.get(id);
        return value == null ? 0.0F : value;
    }

    @Override
    public float getOldOrganScore(ResourceLocation id) {
        Float value = oldOrganScores.get(id);
        return value == null ? 0.0F : value;
    }

    @Override
    public void setOrganScore(ResourceLocation id, float value) {
        organScores.put(id, value);
    }

    @Override
    public void addOrganScore(ResourceLocation id, float value) {
        setOrganScore(id, getOrganScore(id) + value);
    }

    @Override
    public void clearOrganScores() {
        organScores.clear();
    }

    @Override
    public void replaceOrganScores(Map<ResourceLocation, Float> scores) {
        organScores.clear();
        organScores.putAll(scores);
    }

    @Override
    public void copyCurrentScoresToOld() {
        oldOrganScores.clear();
        oldOrganScores.putAll(organScores);
    }

    @Override
    public int getHeartBleedTimer() {
        return heartBleedTimer;
    }

    @Override
    public void setHeartBleedTimer(int value) {
        heartBleedTimer = value;
    }

    @Override
    public int getBloodPoisonTimer() {
        return bloodPoisonTimer;
    }

    @Override
    public void setBloodPoisonTimer(int value) {
        bloodPoisonTimer = value;
    }

    @Override
    public int getLiverTimer() {
        return liverTimer;
    }

    @Override
    public void setLiverTimer(int value) {
        liverTimer = value;
    }

    @Override
    public float getMetabolismRemainder() {
        return metabolismRemainder;
    }

    @Override
    public void setMetabolismRemainder(float value) {
        metabolismRemainder = value;
    }

    @Override
    public float getLungRemainder() {
        return lungRemainder;
    }

    @Override
    public void setLungRemainder(float value) {
        lungRemainder = value;
    }

    @Override
    public int getFurnaceProgress() {
        return furnaceProgress;
    }

    @Override
    public void setFurnaceProgress(int value) {
        furnaceProgress = value;
    }

    @Override
    public int getPhotosynthesisProgress() {
        return photosynthesisProgress;
    }

    @Override
    public void setPhotosynthesisProgress(int value) {
        photosynthesisProgress = value;
    }

    @Override
    public int getConnectedCrystalId() {
        return connectedCrystalId;
    }

    @Override
    public void setConnectedCrystalId(int entityId) {
        connectedCrystalId = entityId;
    }

    @Override
    public void enqueueProjectileAbility(ResourceLocation abilityId) {
        if (abilityId != null) {
            projectileQueue.add(abilityId);
        }
    }

    @Override
    public ResourceLocation pollProjectileAbility() {
        return projectileQueue.poll();
    }

    @Override
    public void clearProjectileQueue() {
        projectileQueue.clear();
    }

    @Override
    public void copyFrom(IChestCavity other) {
        deserializeNBT(other.serializeNBT());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Opened", opened);
        tag.setUniqueId("CompatibilityId", getCompatibilityId());
        tag.setInteger("HeartTimer", heartBleedTimer);
        tag.setInteger("KidneyTimer", bloodPoisonTimer);
        tag.setInteger("LiverTimer", liverTimer);
        tag.setFloat("MetabolismRemainder", metabolismRemainder);
        tag.setFloat("LungRemainder", lungRemainder);
        tag.setInteger("FurnaceProgress", furnaceProgress);
        tag.setInteger("PhotosynthesisProgress", photosynthesisProgress);
        tag.setTag("Inventory", writeInventory());
        tag.setTag("OrganScores", writeScores(organScores));
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        opened = tag.getBoolean("Opened");
        if (tag.hasKey("opened", Constants.NBT.TAG_BYTE)) {
            opened = tag.getBoolean("opened");
        }

        if (tag.hasKey("CompatibilityIdMost", Constants.NBT.TAG_LONG) && tag.hasKey("CompatibilityIdLeast", Constants.NBT.TAG_LONG)) {
            compatibilityId = tag.getUniqueId("CompatibilityId");
        } else if (tag.hasKey("compatibility_idMost", Constants.NBT.TAG_LONG) && tag.hasKey("compatibility_idLeast", Constants.NBT.TAG_LONG)) {
            compatibilityId = tag.getUniqueId("compatibility_id");
        }

        heartBleedTimer = readInt(tag, "HeartTimer", 0);
        bloodPoisonTimer = readInt(tag, "KidneyTimer", 0);
        liverTimer = readInt(tag, "LiverTimer", 0);
        metabolismRemainder = readFloat(tag, "MetabolismRemainder", 0.0F);
        lungRemainder = readFloat(tag, "LungRemainder", 0.0F);
        furnaceProgress = readInt(tag, "FurnaceProgress", 0);
        photosynthesisProgress = readInt(tag, "PhotosynthesisProgress", 0);

        if (tag.hasKey("Inventory", Constants.NBT.TAG_LIST)) {
            readInventory(tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
        }

        organScores.clear();
        if (tag.hasKey("OrganScores", Constants.NBT.TAG_LIST)) {
            readScores(tag.getTagList("OrganScores", Constants.NBT.TAG_COMPOUND), organScores);
        }
        copyCurrentScoresToOld();
    }

    private int readInt(NBTTagCompound tag, String key, int fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_INT) ? tag.getInteger(key) : fallback;
    }

    private float readFloat(NBTTagCompound tag, String key, float fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_FLOAT) ? tag.getFloat(key) : fallback;
    }

    private NBTTagList writeInventory() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < organs.size(); i++) {
            ItemStack stack = organs.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setByte("Slot", (byte) i);
                stack.writeToNBT(stackTag);
                list.appendTag(stackTag);
            }
        }
        return list;
    }

    private void readInventory(NBTTagList list) {
        organs = NonNullList.withSize(DEFAULT_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound stackTag = list.getCompoundTagAt(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < organs.size()) {
                organs.set(slot, new ItemStack(stackTag));
            }
        }
    }

    private NBTTagList writeScores(Map<ResourceLocation, Float> scores) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<ResourceLocation, Float> entry : scores.entrySet()) {
            NBTTagCompound scoreTag = new NBTTagCompound();
            scoreTag.setString("Id", entry.getKey().toString());
            scoreTag.setFloat("Value", entry.getValue());
            list.appendTag(scoreTag);
        }
        return list;
    }

    private void readScores(NBTTagList list, Map<ResourceLocation, Float> scores) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound scoreTag = list.getCompoundTagAt(i);
            scores.put(new ResourceLocation(scoreTag.getString("Id")), scoreTag.getFloat("Value"));
        }
    }

    private void setOrganInternal(int slot, ItemStack stack, boolean recalculate) {
        validateSlot(slot);
        organs.set(slot, stack == null ? ItemStack.EMPTY : stack);
        if (recalculate) {
            ChestCavityHelper.recalculateOrganScores(this);
        }
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= organs.size()) {
            throw new IndexOutOfBoundsException("Organ slot " + slot + " outside 0-" + (organs.size() - 1));
        }
    }

    private final class OrganItemHandler implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            validateSlot(slot);
            if (ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot) && stack != null && !stack.isEmpty()) {
                return;
            }
            ChestCavityHelper.setOrganAndRecalculate(ChestCavityData.this, slot, stack);
        }

        @Override
        public int getSlots() {
            return DEFAULT_SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            return organs.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            validateSlot(slot);
            if (stack == null || stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }

            ItemStack existing = organs.get(slot);
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (!existing.isEmpty()) {
                if (!ItemStack.areItemsEqual(existing, stack) || !ItemStack.areItemStackTagsEqual(existing, stack)) {
                    return stack;
                }
                limit -= existing.getCount();
            }

            if (limit <= 0) {
                return stack;
            }

            int inserted = Math.min(limit, stack.getCount());
            if (!simulate) {
                ItemStack result = existing.isEmpty() ? stack.copy() : existing.copy();
                result.setCount(existing.isEmpty() ? inserted : existing.getCount() + inserted);
                ChestCavityHelper.setOrganAndRecalculate(ChestCavityData.this, slot, result);
            }

            if (inserted >= stack.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            validateSlot(slot);
            if (amount <= 0) {
                return ItemStack.EMPTY;
            }

            ItemStack existing = organs.get(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int extracted = Math.min(amount, existing.getCount());
            ItemStack result = existing.copy();
            result.setCount(extracted);

            if (!simulate) {
                ItemStack remaining = existing.copy();
                remaining.shrink(extracted);
                ChestCavityHelper.setOrganAndRecalculate(ChestCavityData.this, slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }

            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            if (ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot)) {
                return 0;
            }
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return !ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot);
        }
    }
}
