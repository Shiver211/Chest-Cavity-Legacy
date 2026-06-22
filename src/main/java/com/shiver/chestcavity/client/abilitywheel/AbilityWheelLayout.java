package com.shiver.chestcavity.client.abilitywheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * 负责能力轮盘的指针定位和选区计算。
 */
final class AbilityWheelLayout {

    /**
     * 工具类，不允许外部实例化。
     */
    private AbilityWheelLayout() {
    }

    /**
     * 根据当前选中能力把指针重置到对应扇区。
     *
     * @param state 轮盘状态。
     * @param abilities 当前可用能力列表。
     */
    static void resetPointer(AbilityWheelState state, List<String> abilities) {
        if (abilities.isEmpty()) {
            state.setPointer(0.0D, -AbilityWheelConstants.RADIUS);
            state.setCurrentPointer(0.0D, -AbilityWheelConstants.RADIUS);
            return;
        }

        int index = state.getSelectedAbility() == null ? 0 : abilities.indexOf(state.getSelectedAbility());
        if (index < 0) {
            index = 0;
        }

        double angle = -Math.PI / 2.0D + index * AbilityWheelConstants.TWO_PI / abilities.size();
        double pointerX = Math.cos(angle) * (AbilityWheelConstants.RADIUS - 20);
        double pointerY = Math.sin(angle) * (AbilityWheelConstants.RADIUS - 20);
        state.setPointer(pointerX, pointerY);
        state.setCurrentPointer(pointerX, pointerY);
    }

    /**
     * 按鼠标绝对位置更新轮盘指针，并限制在轮盘半径范围内。
     *
     * @param state 轮盘状态。
     * @param minecraft 客户端实例。
     * @param resolution 当前缩放分辨率。
     * @param centerX 轮盘中心 X。
     * @param centerY 轮盘中心 Y。
     */
    static void updatePointerAbsolute(AbilityWheelState state, Minecraft minecraft,
                                      ScaledResolution resolution, int centerX, int centerY) {
        int mouseX = Mouse.getX() * resolution.getScaledWidth() / minecraft.displayWidth;
        int mouseY = resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight() / minecraft.displayHeight - 1;

        double pointerX = mouseX - centerX;
        double pointerY = mouseY - centerY;

        double length = Math.sqrt(pointerX * pointerX + pointerY * pointerY);
        if (length < AbilityWheelConstants.INNER_RADIUS) {
            if (length < 1.0D) {
                state.setPointer(0.0D, -AbilityWheelConstants.INNER_RADIUS);
            } else {
                state.setPointer(pointerX / length * AbilityWheelConstants.INNER_RADIUS,
                        pointerY / length * AbilityWheelConstants.INNER_RADIUS);
            }
            return;
        }
        if (length > AbilityWheelConstants.RADIUS) {
            state.setPointer(pointerX / length * AbilityWheelConstants.RADIUS,
                    pointerY / length * AbilityWheelConstants.RADIUS);
            return;
        }
        state.setPointer(pointerX, pointerY);
    }

    /**
     * 根据当前指针位置确定它指向的能力。
     *
     * @param state 轮盘状态。
     * @param abilities 当前可用能力列表。
     * @return 指针对应的能力标识。
     */
    static String getAbilityAtPointer(AbilityWheelState state, List<String> abilities) {
        if (state.getPointerX() * state.getPointerX() + state.getPointerY() * state.getPointerY()
                < AbilityWheelConstants.INNER_RADIUS * AbilityWheelConstants.INNER_RADIUS) {
            if (state.getHoveredAbility() != null) {
                return state.getHoveredAbility();
            }
            return state.getSelectedAbility() == null ? abilities.get(0) : state.getSelectedAbility();
        }

        double step = AbilityWheelConstants.TWO_PI / abilities.size();
        double normalized = normalizeAngle(Math.atan2(state.getPointerY(), state.getPointerX())
                + Math.PI / 2.0D + step / 2.0D);
        return abilities.get((int) (normalized / step) % abilities.size());
    }

    /**
     * 把任意角度归一化到 `[0, 2π)` 范围。
     *
     * @param angle 原始角度。
     * @return 归一化后的角度。
     */
    private static double normalizeAngle(double angle) {
        angle %= AbilityWheelConstants.TWO_PI;
        return angle < 0.0D ? angle + AbilityWheelConstants.TWO_PI : angle;
    }
}
