package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import com.shiver.chestcavity.layout.LayoutMigrationStrategy;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.runtime.OrganInstance;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import com.shiver.chestcavity.util.OrganCompatibilityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class ChestCavityData {

    public static final int DEFAULT_SLOT_COUNT = 27;

    private EntityLivingBase owner;
    private boolean opened;
    private UUID compatibilityId = UUID.randomUUID();
    private NonNullList<ItemStack> organs;
    private OrganInstance[] organInstances;
    private final IItemHandlerModifiable organInventory = new OrganItemHandler();
    private ChestCavityRuntime runtime = ChestCavityRuntime.rebuild(null, null);
    private boolean runtimeDirty = true;
    private long organVersion;
    private long runtimeVersion;
    private long scoreVersion;

    private int heartBleedTimer;
    private int bloodPoisonTimer;
    private int liverTimer;
    private float metabolismRemainder;
    private float lungRemainder;
    private int furnaceProgress;
    private int photosynthesisProgress;
    private int connectedCrystalId = -1;
    private final Queue<String> projectileQueue = new LinkedList<>();

    public EntityLivingBase getOwner() {
        return owner;
    }

    public void setOwner(EntityLivingBase owner) {
        this.owner = owner;
        if (owner != null && compatibilityId == null) {
            compatibilityId = owner.getUniqueID();
        }
    }

    public boolean isOpened() {
        return opened;
    }

    public UUID getCompatibilityId() {
        if (compatibilityId == null) {
            compatibilityId = owner == null ? UUID.randomUUID() : owner.getUniqueID();
        }
        return compatibilityId;
    }

    public int getSlotCount() {
        return getLayoutSlotCount();
    }

    public NonNullList<ItemStack> getOrgans() {
        ensureOrganStorageMatchesLayout();
        return organs == null ? NonNullList.withSize(getSlotCount(), ItemStack.EMPTY) : organs;
    }

    public IItemHandlerModifiable getOrganInventory() {
        return organInventory;
    }

    public ItemStack getOrgan(int slot) {
        validateSlot(slot);
        ensureOrganStorageMatchesLayout();
        return organs == null ? ItemStack.EMPTY : organs.get(slot);
    }

    public OrganInstance getOrganInstance(int slot, ChestCavityType type) {
        validateSlot(slot);
        ensureOrganStorageMatchesLayout();
        if (organs == null) {
            return OrganInstance.empty();
        }
        ensureOrganInstanceCache();
        OrganInstance instance = organInstances[slot];
        if (instance == null) {
            instance = resolveOrganInstance(type, organs.get(slot));
            organInstances[slot] = instance;
        }
        return instance;
    }

    public Map<String, Float> getOrganScores() {
        refreshRuntimeIfDirty();
        return runtime.getScoreValues();
    }

    public float getOrganScore(String id) {
        refreshRuntimeIfDirty();
        return runtime.getScoreValue(id);
    }

    public ChestCavityRuntime getRuntime() {
        refreshRuntimeIfDirty();
        return runtime;
    }

    public ChestCavityRuntime peekRuntime() {
        return runtime;
    }

    void setRuntimeCommitted(ChestCavityRuntime runtime) {
        Map<String, Float> oldScores = this.runtime.getScoreValues();
        this.runtime = runtime == null ? ChestCavityRuntime.rebuild(null, null) : runtime;
        runtimeDirty = false;
        runtimeVersion++;
        if (!oldScores.equals(this.runtime.getScoreValues())) {
            scoreVersion++;
        }
    }

    public boolean isRuntimeDirty() {
        return runtimeDirty;
    }

    public long getOrganVersion() {
        return organVersion;
    }

    public long getRuntimeVersion() {
        refreshRuntimeIfDirty();
        return runtimeVersion;
    }

    public long getScoreVersion() {
        refreshRuntimeIfDirty();
        return scoreVersion;
    }

    public int getHeartBleedTimer() {
        return heartBleedTimer;
    }

    public void setHeartBleedTimer(int value) {
        heartBleedTimer = value;
    }

    public int getBloodPoisonTimer() {
        return bloodPoisonTimer;
    }

    public void setBloodPoisonTimer(int value) {
        bloodPoisonTimer = value;
    }

    public int getLiverTimer() {
        return liverTimer;
    }

    public void setLiverTimer(int value) {
        liverTimer = value;
    }

    public float getMetabolismRemainder() {
        return metabolismRemainder;
    }

    public void setMetabolismRemainder(float value) {
        metabolismRemainder = value;
    }

    public float getLungRemainder() {
        return lungRemainder;
    }

    public void setLungRemainder(float value) {
        lungRemainder = value;
    }

    public int getFurnaceProgress() {
        return furnaceProgress;
    }

    public void setFurnaceProgress(int value) {
        furnaceProgress = value;
    }

    public int getPhotosynthesisProgress() {
        return photosynthesisProgress;
    }

    public void setPhotosynthesisProgress(int value) {
        photosynthesisProgress = value;
    }

    public int getConnectedCrystalId() {
        return connectedCrystalId;
    }

    public void setConnectedCrystalId(int entityId) {
        connectedCrystalId = entityId;
    }

    public void enqueueProjectileAbility(String abilityId) {
        if (abilityId != null) {
            projectileQueue.add(abilityId);
        }
    }

    public String pollProjectileAbility() {
        return projectileQueue.poll();
    }

    public void clearProjectileQueue() {
        projectileQueue.clear();
    }

    public void copyFrom(ChestCavityData other) {
        deserializeNBT(other.serializeNBT());
    }

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
        if (organs != null) {
            tag.setTag("Inventory", writeInventory());
        }
        return tag;
    }

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

        organs = null;
        organInstances = null;
        if (tag.hasKey("Inventory", Constants.NBT.TAG_LIST)) {
            readInventory(tag.getTagList("Inventory", Constants.NBT.TAG_COMPOUND));
        }

        runtime = ChestCavityRuntime.rebuild(this, ChestCavityTypeUtil.getChestCavityType(this));
        runtimeDirty = false;
        organVersion++;
        runtimeVersion++;
        scoreVersion++;
    }

    private int readInt(NBTTagCompound tag, String key, int fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_INT) ? tag.getInteger(key) : fallback;
    }

    private float readFloat(NBTTagCompound tag, String key, float fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_FLOAT) ? tag.getFloat(key) : fallback;
    }

    private NBTTagList writeInventory() {
        NBTTagList list = new NBTTagList();
        if (organs == null) {
            return list;
        }
        ensureOrganStorageMatchesLayout();
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
        organs = null;
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound stackTag = list.getCompoundTagAt(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < getSlotCount()) {
                ensureOrganStorage();
                organs.set(slot, new ItemStack(stackTag));
                clearOrganInstanceCache();
            }
        }
    }

    void setOrganInternal(int slot, ItemStack stack) {
        validateSlot(slot);
        ensureOrganStorageMatchesLayout();
        ItemStack normalized = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        ItemStack current = organs == null ? ItemStack.EMPTY : organs.get(slot);
        if (sameStack(current, normalized)) {
            return;
        }
        if (!normalized.isEmpty()) {
            ensureOrganStorage();
        }
        if (organs == null) {
            return;
        }
        organs.set(slot, normalized);
        clearOrganInstance(slot);
        clearOrganStorageIfEmpty();
        organVersion++;
        markRuntimeDirty();
    }

    public void refreshRuntimeIfDirty() {
        if (runtimeDirty) {
            setRuntimeCommitted(ChestCavityRuntime.rebuild(this, ChestCavityTypeUtil.getChestCavityType(this)));
        }
    }

    public void openChestCavity() {
        ChestCavityMutations.open(this);
    }

    public void destroyOrgansWithScore(String scoreId) {
        ChestCavityMutations.destroyOrgansWithScore(this, scoreId);
    }

    public void syncOwner() {
        if (owner != null && !owner.world.isRemote) {
            ChestCavityNetwork.sendChestCavitySync(owner);
        }
    }

    void markRuntimeDirty() {
        runtimeDirty = true;
    }

    void setOpenedRaw(boolean opened) {
        if (this.opened != opened) {
            this.opened = opened;
            markRuntimeDirty();
        }
    }

    void setCompatibilityIdRaw(UUID compatibilityId) {
        this.compatibilityId = compatibilityId;
    }

    void invalidateOrganInstancesRaw() {
        clearOrganInstanceCache();
        markRuntimeDirty();
    }

    void publishOrganChange(int slot, ItemStack oldStack, ItemStack newStack) {
        if (owner == null) {
            return;
        }
        ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(this);
        if (oldStack != null && !oldStack.isEmpty()) {
            OrganData oldData = resolveOrganData(type, oldStack);
            CrTChestCavityEvents.publishOrganUnequipped(owner, slot, oldStack, oldData != null && oldData.isPseudoOrgan());
        }
        if (newStack != null && !newStack.isEmpty()) {
            OrganData newData = resolveOrganData(type, newStack);
            CrTChestCavityEvents.publishOrganEquipped(owner, slot, newStack, newData != null && newData.isPseudoOrgan());
        }
    }

    OrganData resolveOrganData(ChestCavityType type, ItemStack stack) {
        return resolveOrganInstance(type, stack).getData();
    }

    private EntityEnderCrystal getConnectedCrystal() {
        int crystalId = getConnectedCrystalId();
        if (crystalId < 0 || owner == null || owner.world == null) {
            return null;
        }
        Entity entityById = owner.world.getEntityByID(crystalId);
        return entityById instanceof EntityEnderCrystal && entityById.isEntityAlive()
                ? (EntityEnderCrystal) entityById
                : null;
    }

    void disconnectCrystal() {
        EntityEnderCrystal crystal = getConnectedCrystal();
        if (crystal != null) {
            crystal.setBeamTarget(null);
        }
        setConnectedCrystalId(-1);
    }

    private boolean sameStack(ItemStack left, ItemStack right) {
        if (left == null || left.isEmpty()) {
            return right == null || right.isEmpty();
        }
        if (right == null || right.isEmpty()) {
            return false;
        }
        return left.getCount() == right.getCount()
                && ItemStack.areItemsEqual(left, right)
                && ItemStack.areItemStackTagsEqual(left, right);
    }

    private void validateSlot(int slot) {
        int slotCount = getSlotCount();
        if (slot < 0 || slot >= slotCount) {
            throw new IndexOutOfBoundsException("Organ slot " + slot + " outside 0-" + (slotCount - 1));
        }
    }

    void ensureOrganStorage() {
        int slotCount = getSlotCount();
        if (organs == null) {
            organs = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        } else if (organs.size() != slotCount) {
            resizeOrganStorage(slotCount, ChestCavityTypeUtil.getChestLayout(this).getMigrationStrategy());
        }
    }

    private int getLayoutSlotCount() {
        return ChestCavityTypeUtil.getChestLayout(this).getSlotCount();
    }

    private void ensureOrganStorageMatchesLayout() {
        if (organs != null && organs.size() != getSlotCount()) {
            resizeOrganStorage(getSlotCount(), ChestCavityTypeUtil.getChestLayout(this).getMigrationStrategy());
        }
    }

    private void resizeOrganStorage(int newSlotCount, LayoutMigrationStrategy strategy) {
        if (organs == null) {
            return;
        }
        LayoutMigrationStrategy effectiveStrategy = strategy == null ? LayoutMigrationStrategy.KEEP_BY_INDEX : strategy;
        NonNullList<ItemStack> oldOrgans = organs;
        NonNullList<ItemStack> newOrgans = NonNullList.withSize(newSlotCount, ItemStack.EMPTY);
        if (effectiveStrategy != LayoutMigrationStrategy.CLEAR) {
            int copiedSlots = Math.min(oldOrgans.size(), newSlotCount);
            for (int slot = 0; slot < copiedSlots; slot++) {
                ItemStack stack = oldOrgans.get(slot);
                newOrgans.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
        }
        if (oldOrgans.size() > newSlotCount && effectiveStrategy != LayoutMigrationStrategy.CLEAR) {
            for (int slot = newSlotCount; slot < oldOrgans.size(); slot++) {
                handleOverflowOrgan(oldOrgans.get(slot), effectiveStrategy);
            }
        }
        organs = newOrgans;
        clearOrganInstanceCache();
        clearOrganStorageIfEmpty();
        organVersion++;
        markRuntimeDirty();
    }

    private void ensureOrganInstanceCache() {
        int slotCount = getSlotCount();
        if (organInstances == null || organInstances.length != slotCount) {
            organInstances = new OrganInstance[slotCount];
        }
    }

    private void clearOrganInstance(int slot) {
        if (organInstances != null && slot >= 0 && slot < organInstances.length) {
            organInstances[slot] = null;
        }
    }

    private void clearOrganInstanceCache() {
        organInstances = null;
    }

    private OrganInstance resolveOrganInstance(ChestCavityType type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return OrganInstance.empty();
        }
        OrganData data = type == null ? null : type.catchExceptionalOrgan(stack);
        if (data == null) {
            data = OrganData.fromStack(stack);
        }
        int compatibilityLevel = data == null || data.isPseudoOrgan()
                ? 1
                : OrganCompatibilityUtil.getCompatibilityLevel(this, stack);
        return OrganInstance.of(stack, data, compatibilityLevel);
    }

    private void handleOverflowOrgan(ItemStack stack, LayoutMigrationStrategy strategy) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (strategy == LayoutMigrationStrategy.MOVE_TO_PLAYER && owner instanceof EntityPlayer) {
            ItemStack remainder = stack.copy();
            if (((EntityPlayer) owner).inventory.addItemStackToInventory(remainder)) {
                return;
            }
        }
        if (owner != null && owner.world != null && !owner.world.isRemote
                && (strategy == LayoutMigrationStrategy.DROP_OVERFLOW || strategy == LayoutMigrationStrategy.MOVE_TO_PLAYER)) {
            owner.entityDropItem(stack.copy(), 0.0F);
        }
    }

    private void clearOrganStorageIfEmpty() {
        if (organs == null || opened) {
            return;
        }
        for (ItemStack stack : organs) {
            if (!stack.isEmpty()) {
                return;
            }
        }
        organs = null;
    }

    private final class OrganItemHandler implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            validateSlot(slot);
            if (!ChestCavityTypeUtil.canPlaceOrgan(ChestCavityData.this, slot, stack)) {
                return;
            }
            ChestCavityMutations.setOrgan(ChestCavityData.this, slot, stack);
        }

        @Override
        public int getSlots() {
            return getSlotCount();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            ensureOrganStorageMatchesLayout();
            ItemStack stack = organs == null ? ItemStack.EMPTY : organs.get(slot);
            return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
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

            ensureOrganStorageMatchesLayout();
            ItemStack existing = organs == null ? ItemStack.EMPTY : organs.get(slot);
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
                ChestCavityMutations.setOrgan(ChestCavityData.this, slot, result);
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

            ensureOrganStorageMatchesLayout();
            ItemStack existing = organs == null ? ItemStack.EMPTY : organs.get(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int extracted = Math.min(amount, existing.getCount());
            ItemStack result = existing.copy();
            result.setCount(extracted);

            if (!simulate) {
                ItemStack remaining = existing.copy();
                remaining.shrink(extracted);
                ChestCavityMutations.setOrgan(ChestCavityData.this, slot, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }

            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            return ChestCavityTypeUtil.getSlotLimit(ChestCavityData.this, slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return ChestCavityTypeUtil.canPlaceOrgan(ChestCavityData.this, slot, stack);
        }
    }
}
