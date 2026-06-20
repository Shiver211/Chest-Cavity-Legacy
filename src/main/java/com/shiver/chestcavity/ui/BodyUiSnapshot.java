package com.shiver.chestcavity.ui;

import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.SlotRule;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public final class BodyUiSnapshot {

    private static final String STATE_TAG = "State";

    private BodyUiSnapshot() {
    }

    public static NBTTagCompound create(ChestCavityData chestCavity) {
        NBTTagCompound tag = new NBTTagCompound();
        if (chestCavity == null) {
            return tag;
        }
        chestCavity.refreshRuntimeIfDirty();
        ChestLayoutDef layout = ChestCavityTypeUtil.getChestLayout(chestCavity);
        ChestCavityRuntime runtime = chestCavity.getRuntime();

        tag.setTag(STATE_TAG, chestCavity.serializeNBT());
        tag.setString("LayoutId", layout.getId().toString());
        tag.setInteger("SlotCount", layout.getSlotCount());
        tag.setLong("OrganVersion", chestCavity.getOrganVersion());
        tag.setLong("RuntimeVersion", chestCavity.getRuntimeVersion());
        tag.setLong("ScoreVersion", chestCavity.getScoreVersion());
        tag.setTag("Slots", writeSlots(chestCavity, layout));
        tag.setTag("SlotRules", writeRules(layout));
        tag.setTag("Scores", writeScores(runtime));
        return tag;
    }

    public static NBTTagCompound getState(NBTTagCompound snapshot) {
        return snapshot != null && snapshot.hasKey(STATE_TAG, Constants.NBT.TAG_COMPOUND)
                ? snapshot.getCompoundTag(STATE_TAG)
                : new NBTTagCompound();
    }

    private static NBTTagList writeSlots(ChestCavityData chestCavity, ChestLayoutDef layout) {
        NBTTagList slots = new NBTTagList();
        for (int slot = 0; slot < layout.getSlotCount(); slot++) {
            ItemStack stack = chestCavity.getOrgan(slot);
            if (stack.isEmpty()) {
                continue;
            }
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Slot", slot);
            stack.writeToNBT(tag);
            slots.appendTag(tag);
        }
        return slots;
    }

    private static NBTTagList writeRules(ChestLayoutDef layout) {
        NBTTagList rules = new NBTTagList();
        for (Map.Entry<Integer, SlotRule> entry : layout.getSlotRules().entrySet()) {
            SlotRule rule = entry.getValue();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Slot", entry.getKey());
            tag.setBoolean("Forbidden", rule.isForbidden());
            tag.setInteger("MinStackSize", rule.getMinStackSize());
            tag.setInteger("MaxStackSize", rule.getMaxStackSize());
            rules.appendTag(tag);
        }
        return rules;
    }

    private static NBTTagCompound writeScores(ChestCavityRuntime runtime) {
        NBTTagCompound scores = new NBTTagCompound();
        for (Map.Entry<String, Float> entry : runtime.getScoreValues().entrySet()) {
            scores.setFloat(entry.getKey(), entry.getValue());
        }
        return scores;
    }
}
