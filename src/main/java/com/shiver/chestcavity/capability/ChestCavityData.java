package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.data.DataLoaders;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * 胸腔能力的默认数据实现，负责保存器官槽位、器官分数和运行期状态。
 */
public class ChestCavityData implements IChestCavity {

    public static final int DEFAULT_SLOT_COUNT = 27;

    private EntityLivingBase owner;
    private boolean opened;
    private UUID compatibilityId = UUID.randomUUID();
    private NonNullList<ItemStack> organs = NonNullList.withSize(DEFAULT_SLOT_COUNT, ItemStack.EMPTY);
    private final Map<String, Float> organScores = new HashMap<>();
    private final Map<String, Float> oldOrganScores = new HashMap<>();
    private final IItemHandlerModifiable organInventory = new OrganItemHandler();

    private int heartBleedTimer;
    private int bloodPoisonTimer;
    private int liverTimer;
    private float metabolismRemainder;
    private float lungRemainder;
    private int furnaceProgress;
    private int photosynthesisProgress;
    private int connectedCrystalId = -1;
    private final Queue<String> projectileQueue = new LinkedList<>();
    private boolean scoresDirty = true;
    private int cleanDataVersion = -1;
    private boolean attributeModifiersDirty = true;
    private int lastAttributeRefreshTick = Integer.MIN_VALUE;

    /**
     * 返回当前胸腔数据所属的实体。
     *
     * @return 胸腔数据的持有者。
     */
    @Override
    public EntityLivingBase getOwner() {
        return owner;
    }

    /**
     * 设置当前胸腔数据所属的实体。
     *
     * @param owner 胸腔数据的持有者。
     */
    @Override
    public void setOwner(EntityLivingBase owner) {
        this.owner = owner;
        if (owner != null && compatibilityId == null) {
            compatibilityId = owner.getUniqueID();
        }
    }

    /**
     * 判断胸腔是否处于被打开状态。
     *
     * @return `true` 表示胸腔已打开。
     */
    @Override
    public boolean isOpened() {
        return opened;
    }

    /**
     * 设置胸腔的打开状态。
     *
     * @param opened 是否已打开。
     */
    @Override
    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    /**
     * 返回当前胸腔兼容性快照对应的唯一标识。
     *
     * @return 兼容性标识。
     */
    @Override
    public UUID getCompatibilityId() {
        if (compatibilityId == null) {
            compatibilityId = owner == null ? UUID.randomUUID() : owner.getUniqueID();
        }
        return compatibilityId;
    }

    /**
     * 设置当前胸腔兼容性快照对应的唯一标识。
     *
     * @param compatibilityId 兼容性标识。
     */
    @Override
    public void setCompatibilityId(UUID compatibilityId) {
        this.compatibilityId = compatibilityId;
    }

    /**
     * 返回胸腔内部拥有的槽位数量。
     *
     * @return 槽位总数。
     */
    @Override
    public int getSlotCount() {
        return organs.size();
    }

    /**
     * 返回当前保存的全部器官列表。
     *
     * @return 器官列表。
     */
    @Override
    public NonNullList<ItemStack> getOrgans() {
        return organs;
    }

    /**
     * 返回用于操作器官槽位的物品栏包装器。
     *
     * @return 器官物品栏接口。
     */
    @Override
    public IItemHandlerModifiable getOrganInventory() {
        return organInventory;
    }

    /**
     * 返回指定槽位中的器官物品。
     *
     * @param slot 槽位索引。
     * @return 槽位中的器官物品。
     */
    @Override
    public ItemStack getOrgan(int slot) {
        return organs.get(slot);
    }

    /**
     * 设置指定槽位中的器官物品，并触发必要的重算。
     *
     * @param slot 槽位索引。
     * @param stack 要放入槽位的器官物品。
     */
    @Override
    public void setOrgan(int slot, ItemStack stack) {
        setOrganInternal(slot, stack, true);
    }

    /**
     * 返回当前生效的全部器官分数字典。
     *
     * @return 器官分数字典。
     */
    @Override
    public Map<String, Float> getOrganScores() {
        return organScores;
    }

    /**
     * 返回上一次同步或结算时保存的器官分数字典。
     *
     * @return 旧器官分数字典。
     */
    @Override
    public Map<String, Float> getOldOrganScores() {
        return oldOrganScores;
    }

    /**
     * 返回指定分数项当前的器官分值。
     *
     * @param id 分数标识。
     * @return 当前分值。
     */
    @Override
    public float getOrganScore(String id) {
        Float value = organScores.get(id);
        return value == null ? 0.0F : value;
    }

    /**
     * 返回指定分数项旧快照中的器官分值。
     *
     * @param id 分数标识。
     * @return 旧分值。
     */
    @Override
    public float getOldOrganScore(String id) {
        Float value = oldOrganScores.get(id);
        return value == null ? 0.0F : value;
    }

    /**
     * 直接设置指定分数项的器官分值。
     *
     * @param id 分数标识。
     * @param value 要写入的分值。
     */
    @Override
    public void setOrganScore(String id, float value) {
        organScores.put(id, value);
        markScoresDirty();
    }

    /**
     * 为指定分数项累加器官分值。
     *
     * @param id 分数标识。
     * @param value 要累加的分值。
     */
    @Override
    public void addOrganScore(String id, float value) {
        setOrganScore(id, getOrganScore(id) + value);
    }

    /**
     * 清空当前已计算的全部器官分数。
     */
    @Override
    public void clearOrganScores() {
        organScores.clear();
        markScoresDirty();
    }

    /**
     * 用新的分数字典整体替换当前器官分数。
     *
     * @param scores 新的器官分数字典。
     */
    @Override
    public void replaceOrganScores(Map<String, Float> scores) {
        organScores.clear();
        organScores.putAll(scores);
        attributeModifiersDirty = true;
    }

    /**
     * 将当前器官分数复制到旧分数快照中。
     */
    @Override
    public void copyCurrentScoresToOld() {
        oldOrganScores.clear();
        oldOrganScores.putAll(organScores);
    }

    /**
     * 返回无心脏出血效果使用的累计计时器。
     *
     * @return 心脏出血计时器。
     */
    @Override
    public int getHeartBleedTimer() {
        return heartBleedTimer;
    }

    /**
     * 设置无心脏出血效果使用的累计计时器。
     *
     * @param value 新的计时值。
     */
    @Override
    public void setHeartBleedTimer(int value) {
        heartBleedTimer = value;
    }

    /**
     * 返回过滤不足导致血液中毒的累计计时器。
     *
     * @return 血液中毒计时器。
     */
    @Override
    public int getBloodPoisonTimer() {
        return bloodPoisonTimer;
    }

    /**
     * 设置过滤不足导致血液中毒的累计计时器。
     *
     * @param value 新的计时值。
     */
    @Override
    public void setBloodPoisonTimer(int value) {
        bloodPoisonTimer = value;
    }

    /**
     * 返回与肝脏逻辑共用的计时器。
     *
     * @return 肝脏计时器。
     */
    @Override
    public int getLiverTimer() {
        return liverTimer;
    }

    /**
     * 设置与肝脏逻辑共用的计时器。
     *
     * @param value 新的计时值。
     */
    @Override
    public void setLiverTimer(int value) {
        liverTimer = value;
    }

    /**
     * 返回代谢计算中保留下来的小数余量。
     *
     * @return 代谢余量。
     */
    @Override
    public float getMetabolismRemainder() {
        return metabolismRemainder;
    }

    /**
     * 设置代谢计算中保留下来的小数余量。
     *
     * @param value 代谢余量。
     */
    @Override
    public void setMetabolismRemainder(float value) {
        metabolismRemainder = value;
    }

    /**
     * 返回呼吸计算中保留下来的小数余量。
     *
     * @return 呼吸余量。
     */
    @Override
    public float getLungRemainder() {
        return lungRemainder;
    }

    /**
     * 设置呼吸计算中保留下来的小数余量。
     *
     * @param value 呼吸余量。
     */
    @Override
    public void setLungRemainder(float value) {
        lungRemainder = value;
    }

    /**
     * 返回内置熔炉能力当前累计的进度。
     *
     * @return 熔炉进度。
     */
    @Override
    public int getFurnaceProgress() {
        return furnaceProgress;
    }

    /**
     * 设置内置熔炉能力当前累计的进度。
     *
     * @param value 熔炉进度。
     */
    @Override
    public void setFurnaceProgress(int value) {
        furnaceProgress = value;
    }

    /**
     * 返回光合作用能力当前累计的进度。
     *
     * @return 光合作用进度。
     */
    @Override
    public int getPhotosynthesisProgress() {
        return photosynthesisProgress;
    }

    /**
     * 设置光合作用能力当前累计的进度。
     *
     * @param value 光合作用进度。
     */
    @Override
    public void setPhotosynthesisProgress(int value) {
        photosynthesisProgress = value;
    }

    /**
     * 返回当前连接的末地水晶实体 ID。
     *
     * @return 末地水晶实体 ID。
     */
    @Override
    public int getConnectedCrystalId() {
        return connectedCrystalId;
    }

    /**
     * 设置当前连接的末地水晶实体 ID。
     *
     * @param entityId 末地水晶实体 ID。
     */
    @Override
    public void setConnectedCrystalId(int entityId) {
        connectedCrystalId = entityId;
    }

    /**
     * 将等待发射的投射物能力加入队列。
     *
     * @param abilityId 能力标识。
     */
    @Override
    public void enqueueProjectileAbility(String abilityId) {
        if (abilityId != null) {
            projectileQueue.add(abilityId);
        }
    }

    /**
     * 取出一个等待发射的投射物能力标识。
     *
     * @return 队列头部的能力标识；如果没有则返回 `null`。
     */
    @Override
    public String pollProjectileAbility() {
        return projectileQueue.poll();
    }

    /**
     * 清空等待发射的投射物能力队列。
     */
    @Override
    public void clearProjectileQueue() {
        projectileQueue.clear();
    }

    /**
     * 从另一个胸腔数据实例复制全部状态。
     *
     * @param other 用作数据源的胸腔实例。
     */
    @Override
    public void copyFrom(IChestCavity other) {
        deserializeNBT(other.serializeNBT());
    }

    /**
     * 将当前胸腔数据序列化为 NBT。
     *
     * @return 序列化后的 NBT 数据。
     */
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

    /**
     * 从 NBT 中恢复当前胸腔数据。
     *
     * @param tag 序列化后的 NBT 数据。
     */
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
        markScoresDirty();
    }

    /**
     * 判断当前器官分数是否需要按指定数据版本重新计算。
     *
     * @param dataVersion 当前已加载的数据版本。
     * @return `true` 表示需要重新计算。
     */
    public boolean needsScoreRecalculation(int dataVersion) {
        return scoresDirty || cleanDataVersion != dataVersion;
    }

    /**
     * 将器官分数和属性修正标记为脏状态。
     */
    public void markScoresDirty() {
        scoresDirty = true;
        attributeModifiersDirty = true;
    }

    /**
     * 将器官分数标记为已清洁，并记录当前数据版本。
     *
     * @param dataVersion 本次分数对应的数据版本。
     */
    public void markScoresClean(int dataVersion) {
        scoresDirty = false;
        cleanDataVersion = dataVersion;
        attributeModifiersDirty = true;
    }

    /**
     * 强制标记属性修正需要重新刷新。
     */
    public void markAttributeModifiersDirty() {
        attributeModifiersDirty = true;
    }

    /**
     * 判断本 tick 是否应该刷新属性修正器。
     *
     * @param ticksExisted 实体当前已存在的 tick 数。
     * @param intervalTicks 两次强制刷新之间允许的最大间隔。
     * @return `true` 表示本次应刷新属性修正器。
     */
    public boolean shouldRefreshAttributeModifiers(int ticksExisted, int intervalTicks) {
        if (attributeModifiersDirty || lastAttributeRefreshTick == Integer.MIN_VALUE
                || ticksExisted < lastAttributeRefreshTick
                || ticksExisted - lastAttributeRefreshTick >= intervalTicks) {
            attributeModifiersDirty = false;
            lastAttributeRefreshTick = ticksExisted;
            return true;
        }
        return false;
    }

    /**
     * 从 NBT 中读取整数值，不存在时返回默认值。
     *
     * @param tag 来源 NBT。
     * @param key 键名。
     * @param fallback 默认值。
     * @return 读取到的整数值。
     */
    private int readInt(NBTTagCompound tag, String key, int fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_INT) ? tag.getInteger(key) : fallback;
    }

    /**
     * 从 NBT 中读取浮点值，不存在时返回默认值。
     *
     * @param tag 来源 NBT。
     * @param key 键名。
     * @param fallback 默认值。
     * @return 读取到的浮点值。
     */
    private float readFloat(NBTTagCompound tag, String key, float fallback) {
        return tag.hasKey(key, Constants.NBT.TAG_FLOAT) ? tag.getFloat(key) : fallback;
    }

    /**
     * 将当前器官槽位写出为 NBT 列表。
     *
     * @return 序列化后的器官物品列表。
     */
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

    /**
     * 从 NBT 列表恢复器官槽位内容。
     *
     * @param list 序列化后的器官物品列表。
     */
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

    /**
     * 将器官分数字典写出为 NBT 列表。
     *
     * @param scores 要写出的分数字典。
     * @return 序列化后的分数列表。
     */
    private NBTTagList writeScores(Map<String, Float> scores) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, Float> entry : scores.entrySet()) {
            NBTTagCompound scoreTag = new NBTTagCompound();
            scoreTag.setString("Id", entry.getKey());
            scoreTag.setFloat("Value", entry.getValue());
            list.appendTag(scoreTag);
        }
        return list;
    }

    /**
     * 从 NBT 列表恢复器官分数字典。
     *
     * @param list 序列化后的分数列表。
     * @param scores 要写入的分数字典。
     */
    private void readScores(NBTTagList list, Map<String, Float> scores) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound scoreTag = list.getCompoundTagAt(i);
            scores.put(scoreTag.getString("Id"), scoreTag.getFloat("Value"));
        }
    }

    /**
     * 在内部设置指定槽位中的器官，并按需触发重算。
     *
     * @param slot 槽位索引。
     * @param stack 要放入的器官物品。
     * @param recalculate 是否立刻重算器官分数。
     */
    private void setOrganInternal(int slot, ItemStack stack, boolean recalculate) {
        validateSlot(slot);
        organs.set(slot, stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        markScoresDirty();
        if (recalculate) {
            ChestCavityHelper.recalculateOrganScores(this);
            markScoresClean(DataLoaders.getDataVersion());
        }
    }

    /**
     * 校验槽位索引是否位于合法范围内。
     *
     * @param slot 要校验的槽位索引。
     */
    private void validateSlot(int slot) {
        if (slot < 0 || slot >= organs.size()) {
            throw new IndexOutOfBoundsException("Organ slot " + slot + " outside 0-" + (organs.size() - 1));
        }
    }

    /**
     * 将胸腔槽位包装为 Forge 物品栏接口，便于统一插入和提取器官。
     */
    private final class OrganItemHandler implements IItemHandlerModifiable {

        /**
         * 直接设置指定槽位中的物品，并触发胸腔分数刷新。
         *
         * @param slot 槽位索引。
         * @param stack 要放入的物品。
         */
        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            validateSlot(slot);
            if (ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot) && stack != null && !stack.isEmpty()) {
                return;
            }
            ChestCavityHelper.setOrganAndRecalculate(ChestCavityData.this, slot, stack);
        }

        /**
         * 返回该物品栏暴露出的槽位数量。
         *
         * @return 槽位数量。
         */
        @Override
        public int getSlots() {
            return DEFAULT_SLOT_COUNT;
        }

        /**
         * 返回指定槽位中的物品副本。
         *
         * @param slot 槽位索引。
         * @return 槽位中的物品副本。
         */
        @Override
        public ItemStack getStackInSlot(int slot) {
            validateSlot(slot);
            ItemStack stack = organs.get(slot);
            return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        }

        /**
         * 尝试向指定槽位插入物品。
         *
         * @param slot 槽位索引。
         * @param stack 要插入的物品。
         * @param simulate 是否仅模拟插入。
         * @return 插入后剩余的物品。
         */
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

        /**
         * 尝试从指定槽位提取物品。
         *
         * @param slot 槽位索引。
         * @param amount 希望提取的数量。
         * @param simulate 是否仅模拟提取。
         * @return 实际提取出的物品。
         */
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

        /**
         * 返回指定槽位允许存放的最大物品数量。
         *
         * @param slot 槽位索引。
         * @return 最大堆叠数量。
         */
        @Override
        public int getSlotLimit(int slot) {
            validateSlot(slot);
            if (ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot)) {
                return 0;
            }
            return 64;
        }

        /**
         * 判断指定物品是否允许放入该槽位。
         *
         * @param slot 槽位索引。
         * @param stack 要检查的物品。
         * @return `true` 表示该物品可放入。
         */
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            validateSlot(slot);
            return !ChestCavityHelper.isSlotForbidden(ChestCavityData.this, slot);
        }
    }
}
