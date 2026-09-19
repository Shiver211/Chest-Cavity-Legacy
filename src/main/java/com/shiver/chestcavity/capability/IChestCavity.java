package com.shiver.chestcavity.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.UUID;

/**
 * 定义胸腔能力数据的访问、同步与序列化约定。
 */
public interface IChestCavity {

    /**
     * 返回当前胸腔数据所属的实体。
     *
     * @return 胸腔数据的持有者。
     */
    EntityLivingBase getOwner();

    /**
     * 设置当前胸腔数据所属的实体。
     *
     * @param owner 胸腔数据的持有者。
     */
    void setOwner(EntityLivingBase owner);

    /**
     * 判断胸腔是否处于被打开状态。
     *
     * @return `true` 表示胸腔已打开。
     */
    boolean isOpened();

    /**
     * 设置胸腔的打开状态。
     *
     * @param opened 是否已打开。
     */
    void setOpened(boolean opened);

    /**
     * 返回当前胸腔兼容性快照对应的唯一标识。
     *
     * @return 兼容性标识。
     */
    UUID getCompatibilityId();

    /**
     * 设置当前胸腔兼容性快照对应的唯一标识。
     *
     * @param compatibilityId 兼容性标识。
     */
    void setCompatibilityId(UUID compatibilityId);

    /**
     * 返回胸腔内部拥有的槽位数量。
     *
     * @return 槽位总数。
     */
    int getSlotCount();

    /**
     * 调整胸腔内部拥有的槽位数量。
     *
     * @param newSize 目标槽位数量。
     */
    default void setSlotCount(int newSize) {
    }

    /**
     * 确保胸腔内部槽位数量与目标大小一致。
     *
     * @param targetSize 目标槽位数量。
     */
    default void ensureSlotCount(int targetSize) {
        if (targetSize > 0 && targetSize != getSlotCount()) {
            setSlotCount(targetSize);
        }
    }

    /**
     * 返回当前胸腔界面的网格列数。
     *
     * @return 列数。
     */
    default int getColumns() {
        return 9;
    }

    /**
     * 返回当前胸腔界面的网格行数。
     *
     * @return 行数。
     */
    default int getRows() {
        return 3;
    }

    /**
     * 判断当前胸腔是否设置了实体级的独立自定义网格尺寸。
     *
     * @return `true` 表示当前胸腔拥有独立尺寸，不跟随类型默认设置。
     */
    default boolean hasCustomDimensions() {
        return false;
    }

    /**
     * 为当前实体设置独立的胸腔网格尺寸。
     *
     * @param columns 网格列数。
     * @param rows 网格行数。
     */
    default void setDimensions(int columns, int rows) {
    }

    /**
     * 清除实体的独立网格尺寸设置，恢复回所属胸腔类型的默认尺寸。
     */
    default void resetDimensions() {
    }

    /**
     * 返回胸腔当前保存的全部器官列表的只读视图。
     * 如需修改槽位器官，请使用 {@link #setOrgan(int, ItemStack)} 或 {@link #getOrganInventory()}。
     *
     * @return 器官列表只读视图。
     */
    NonNullList<ItemStack> getOrgans();

    /**
     * 返回用于操作器官槽位的物品栏包装器。
     *
     * @return 器官物品栏接口。
     */
    IItemHandlerModifiable getOrganInventory();

    /**
     * 返回指定槽位中的器官物品。
     *
     * @param slot 槽位索引。
     * @return 槽位中的器官物品。
     */
    ItemStack getOrgan(int slot);

    /**
     * 设置指定槽位中的器官物品。
     *
     * @param slot 槽位索引。
     * @param stack 要放入槽位的器官物品。
     */
    void setOrgan(int slot, ItemStack stack);

    /**
     * 返回当前生效的全部器官分数的只读视图。
     * 如需修改分数，请使用 {@link #setOrganScore(String, float)} 或 {@link #replaceOrganScores(Map)}。
     *
     * @return 器官分数字典只读视图。
     */
    Map<String, Float> getOrganScores();

    /**
     * 返回当前生效的全部器官分数的只读视图。
     *
     * @return 器官分数字典只读视图。
     */
    default Map<String, Float> getOrganScoresView() {
        return getOrganScores();
    }

    /**
     * 返回上一次同步或结算时保存的器官分数字典只读视图。
     *
     * @return 旧器官分数字典只读视图。
     */
    Map<String, Float> getOldOrganScores();

    /**
     * 返回上一次同步或结算时保存的器官分数字典只读视图。
     *
     * @return 旧器官分数字典只读视图。
     */
    default Map<String, Float> getOldOrganScoresView() {
        return getOldOrganScores();
    }

    /**
     * 返回指定分数项当前的器官分值。
     *
     * @param id 分数标识。
     * @return 当前分值。
     */
    float getOrganScore(String id);

    /**
     * 返回指定分数项旧快照中的器官分值。
     *
     * @param id 分数标识。
     * @return 旧分值。
     */
    float getOldOrganScore(String id);

    /**
     * 直接设置指定分数项的器官分值。
     *
     * @param id 分数标识。
     * @param value 要写入的分值。
     */
    void setOrganScore(String id, float value);

    /**
     * 为指定分数项累加器官分值。
     *
     * @param id 分数标识。
     * @param value 要累加的分值。
     */
    void addOrganScore(String id, float value);

    /**
     * 清空当前已计算的全部器官分数。
     */
    void clearOrganScores();

    /**
     * 用新的分数字典整体替换当前器官分数。
     *
     * @param scores 新的器官分数字典。
     */
    void replaceOrganScores(Map<String, Float> scores);

    /**
     * 将当前器官分数复制到旧分数快照中。
     */
    void copyCurrentScoresToOld();

    /**
     * 判断当前器官分数是否相对于旧快照发生了变化。
     *
     * @return `true` 表示分数已变化。
     */
    default boolean hasScoreChanges() {
        return !getOldOrganScores().equals(getOrganScores());
    }

    /**
     * 返回无心脏出血效果使用的累计计时器。
     *
     * @return 心脏出血计时器。
     */
    int getHeartBleedTimer();

    /**
     * 设置无心脏出血效果使用的累计计时器。
     *
     * @param value 新的计时值。
     */
    void setHeartBleedTimer(int value);

    /**
     * 返回过滤不足导致血液中毒的累计计时器。
     *
     * @return 血液中毒计时器。
     */
    int getBloodPoisonTimer();

    /**
     * 设置过滤不足导致血液中毒的累计计时器。
     *
     * @param value 新的计时值。
     */
    void setBloodPoisonTimer(int value);

    /**
     * 返回与肝脏相关逻辑共用的计时器。
     *
     * @return 肝脏计时器。
     */
    int getLiverTimer();

    /**
     * 设置与肝脏相关逻辑共用的计时器。
     *
     * @param value 新的计时值。
     */
    void setLiverTimer(int value);

    /**
     * 返回代谢计算中保留下来的小数余量。
     *
     * @return 代谢余量。
     */
    float getMetabolismRemainder();

    /**
     * 设置代谢计算中保留下来的小数余量。
     *
     * @param value 代谢余量。
     */
    void setMetabolismRemainder(float value);

    /**
     * 返回呼吸计算中保留下来的小数余量。
     *
     * @return 呼吸余量。
     */
    float getLungRemainder();

    /**
     * 设置呼吸计算中保留下来的小数余量。
     *
     * @param value 呼吸余量。
     */
    void setLungRemainder(float value);

    /**
     * 返回内置熔炉能力当前累计的进度。
     *
     * @return 熔炉进度。
     */
    int getFurnaceProgress();

    /**
     * 设置内置熔炉能力当前累计的进度。
     *
     * @param value 熔炉进度。
     */
    void setFurnaceProgress(int value);

    /**
     * 返回光合作用能力当前累计的进度。
     *
     * @return 光合作用进度。
     */
    int getPhotosynthesisProgress();

    /**
     * 设置光合作用能力当前累计的进度。
     *
     * @param value 光合作用进度。
     */
    void setPhotosynthesisProgress(int value);

    /**
     * 返回当前连接的末地水晶实体 ID。
     *
     * @return 末地水晶实体 ID。
     */
    int getConnectedCrystalId();

    /**
     * 设置当前连接的末地水晶实体 ID。
     *
     * @param entityId 末地水晶实体 ID。
     */
    void setConnectedCrystalId(int entityId);

    /**
     * 将等待发射的投射物能力加入队列。
     *
     * @param abilityId 能力标识。
     */
    void enqueueProjectileAbility(String abilityId);

    /**
     * 取出一个等待发射的投射物能力标识。
     *
     * @return 队列头部的能力标识；如果没有则返回 `null`。
     */
    String pollProjectileAbility();

    /**
     * 清空等待发射的投射物能力队列。
     */
    void clearProjectileQueue();

    /**
     * 从另一个胸腔数据实例复制全部状态。
     *
     * @param other 用作数据源的胸腔实例。
     */
    void copyFrom(IChestCavity other);

    /**
     * 将当前胸腔数据序列化为 NBT。
     *
     * @return 序列化后的 NBT 数据。
     */
    NBTTagCompound serializeNBT();

    /**
     * 从 NBT 中恢复当前胸腔数据。
     *
     * @param tag 序列化后的 NBT 数据。
     */
    void deserializeNBT(NBTTagCompound tag);
}
