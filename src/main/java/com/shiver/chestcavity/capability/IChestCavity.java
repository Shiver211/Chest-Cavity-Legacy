package com.shiver.chestcavity.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Map;
import java.util.UUID;

public interface IChestCavity {

    EntityLivingBase getOwner();

    void setOwner(EntityLivingBase owner);

    boolean isOpened();

    void setOpened(boolean opened);

    UUID getCompatibilityId();

    void setCompatibilityId(UUID compatibilityId);

    int getSlotCount();

    NonNullList<ItemStack> getOrgans();

    IItemHandlerModifiable getOrganInventory();

    ItemStack getOrgan(int slot);

    void setOrgan(int slot, ItemStack stack);

    Map<ResourceLocation, Float> getOrganScores();

    Map<ResourceLocation, Float> getOldOrganScores();

    float getOrganScore(ResourceLocation id);

    float getOldOrganScore(ResourceLocation id);

    void setOrganScore(ResourceLocation id, float value);

    void addOrganScore(ResourceLocation id, float value);

    void clearOrganScores();

    void replaceOrganScores(Map<ResourceLocation, Float> scores);

    void copyCurrentScoresToOld();

    int getHeartBleedTimer();

    void setHeartBleedTimer(int value);

    int getBloodPoisonTimer();

    void setBloodPoisonTimer(int value);

    int getLiverTimer();

    void setLiverTimer(int value);

    float getMetabolismRemainder();

    void setMetabolismRemainder(float value);

    float getLungRemainder();

    void setLungRemainder(float value);

    int getFurnaceProgress();

    void setFurnaceProgress(int value);

    int getPhotosynthesisProgress();

    void setPhotosynthesisProgress(int value);

    void copyFrom(IChestCavity other);

    NBTTagCompound serializeNBT();

    void deserializeNBT(NBTTagCompound tag);
}
