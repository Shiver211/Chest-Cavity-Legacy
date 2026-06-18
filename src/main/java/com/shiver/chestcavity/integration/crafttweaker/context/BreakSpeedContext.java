package com.shiver.chestcavity.integration.crafttweaker.context;

import com.shiver.chestcavity.integration.crafttweaker.runtime.ScriptDataRuntime;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BreakSpeedContext {

    private final EntityPlayer player;
    private final ResourceLocation scoreId;
    private final float value;
    private final float baseValue;
    private final ScriptDataRuntime scriptData;
    private final IBlockState blockState;
    private final BlockPos pos;
    private final ItemStack tool;
    private final float originalSpeed;
    private float currentSpeed;

    public BreakSpeedContext(EntityPlayer player, ResourceLocation scoreId, float value, float baseValue, ScriptDataRuntime scriptData, IBlockState blockState, BlockPos pos, ItemStack tool, float originalSpeed) {
        this.player = player;
        this.scoreId = scoreId;
        this.value = value;
        this.baseValue = baseValue;
        this.scriptData = scriptData;
        this.blockState = blockState;
        this.pos = pos;
        this.tool = tool;
        this.originalSpeed = originalSpeed;
        this.currentSpeed = originalSpeed;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public World getWorld() {
        return player == null ? null : player.world;
    }

    public ResourceLocation getScoreId() {
        return scoreId;
    }

    public float getValue() {
        return value;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public float getExtraValue() {
        return value - baseValue;
    }

    public ScriptDataRuntime getScriptData() {
        return scriptData;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ItemStack getTool() {
        return tool;
    }

    public float getOriginalSpeed() {
        return originalSpeed;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }
}
