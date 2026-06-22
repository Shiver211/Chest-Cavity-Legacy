package com.shiver.chestcavity.client.abilitywheel;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存能力轮盘在客户端渲染过程中的瞬时状态。
 */
final class AbilityWheelState {

    private String selectedAbility;
    private String hoveredAbility;
    private boolean wheelOpenLastFrame;
    private boolean wasMouseDown;
    private double pointerX;
    private double pointerY = -AbilityWheelConstants.RADIUS;
    private long lastFrameTime;
    private final Map<String, Double> hoverProgress = new HashMap<String, Double>();
    private double wheelAlphaProgress;
    private double currentPointerX;
    private double currentPointerY = -AbilityWheelConstants.RADIUS;

    /**
     * 返回当前选中的能力标识。
     *
     * @return 当前选中的能力标识。
     */
    String getSelectedAbility() {
        return selectedAbility;
    }

    /**
     * 设置当前选中的能力标识。
     *
     * @param selectedAbility 能力标识。
     */
    void setSelectedAbility(String selectedAbility) {
        this.selectedAbility = selectedAbility;
    }

    /**
     * 返回当前悬停中的能力标识。
     *
     * @return 当前悬停的能力标识。
     */
    String getHoveredAbility() {
        return hoveredAbility;
    }

    /**
     * 设置当前悬停中的能力标识。
     *
     * @param hoveredAbility 能力标识。
     */
    void setHoveredAbility(String hoveredAbility) {
        this.hoveredAbility = hoveredAbility;
    }

    /**
     * 判断轮盘在上一帧是否处于打开状态。
     *
     * @return `true` 表示上一帧已打开。
     */
    boolean wasOpenLastFrame() {
        return wheelOpenLastFrame;
    }

    /**
     * 返回目标指针的 X 坐标。
     *
     * @return 指针 X 坐标。
     */
    double getPointerX() {
        return pointerX;
    }

    /**
     * 返回目标指针的 Y 坐标。
     *
     * @return 指针 Y 坐标。
     */
    double getPointerY() {
        return pointerY;
    }

    /**
     * 设置目标指针位置。
     *
     * @param pointerX 目标 X。
     * @param pointerY 目标 Y。
     */
    void setPointer(double pointerX, double pointerY) {
        this.pointerX = pointerX;
        this.pointerY = pointerY;
    }

    /**
     * 返回平滑插值后的当前指针 X 坐标。
     *
     * @return 当前指针 X 坐标。
     */
    double getCurrentPointerX() {
        return currentPointerX;
    }

    /**
     * 返回平滑插值后的当前指针 Y 坐标。
     *
     * @return 当前指针 Y 坐标。
     */
    double getCurrentPointerY() {
        return currentPointerY;
    }

    /**
     * 设置平滑插值后的当前指针位置。
     *
     * @param currentPointerX 当前 X。
     * @param currentPointerY 当前 Y。
     */
    void setCurrentPointer(double currentPointerX, double currentPointerY) {
        this.currentPointerX = currentPointerX;
        this.currentPointerY = currentPointerY;
    }

    /**
     * 返回轮盘当前的透明度缩放值。
     *
     * @return 透明度缩放值。
     */
    double getAlphaScale() {
        return Math.min(1.0D, wheelAlphaProgress);
    }

    /**
     * 返回指定能力当前的悬停动画进度。
     *
     * @param ability 能力标识。
     * @return 悬停动画进度。
     */
    double getHoverProgress(String ability) {
        return hoverProgress.containsKey(ability) ? hoverProgress.get(ability) : 0.0D;
    }

    /**
     * 确保当前选中能力仍然存在于可用能力列表中。
     *
     * @param abilities 当前可用能力列表。
     */
    void ensureSelectedAbilityAvailable(List<String> abilities) {
        if (!abilities.contains(selectedAbility) && !abilities.isEmpty()) {
            selectedAbility = abilities.get(0);
        }
    }

    /**
     * 打开轮盘，并初始化动画与指针状态。
     *
     * @param abilities 当前可用能力列表。
     */
    void open(List<String> abilities) {
        wheelOpenLastFrame = true;
        wheelAlphaProgress = 0.0D;
        lastFrameTime = Minecraft.getSystemTime();
        AbilityWheelLayout.resetPointer(this, abilities);
    }

    /**
     * 关闭轮盘，并在必要时把鼠标焦点还给游戏。
     *
     * @param minecraft 客户端实例。
     */
    void close(Minecraft minecraft) {
        if (wheelOpenLastFrame) {
            wheelOpenLastFrame = false;
            if (minecraft.currentScreen == null) {
                minecraft.setIngameFocus();
            }
        }
        wasMouseDown = false;
        wheelAlphaProgress = 0.0D;
    }

    /**
     * 当玩家点击鼠标左键时，把悬停能力设置为选中能力。
     */
    void selectHoveredAbilityOnClick() {
        boolean isMouseDown = Mouse.isButtonDown(0);
        if (isMouseDown && !wasMouseDown && hoveredAbility != null) {
            selectedAbility = hoveredAbility;
        }
        wasMouseDown = isMouseDown;
    }

    /**
     * 根据经过时间推进透明度、悬停高亮和指针插值动画。
     *
     * @param abilities 当前可用能力列表。
     */
    void updateAnimations(List<String> abilities) {
        long currentTime = Minecraft.getSystemTime();
        if (lastFrameTime == 0L) {
            lastFrameTime = currentTime;
        }
        double delta = (currentTime - lastFrameTime) / 1000.0D;
        lastFrameTime = currentTime;

        if (delta > 0.1D) {
            delta = 0.1D;
        }

        wheelAlphaProgress = Math.min(1.0D, wheelAlphaProgress + delta * 8.0D);

        for (String ability : abilities) {
            double current = getHoverProgress(ability);
            if (ability.equals(hoveredAbility)) {
                current = Math.min(1.0D, current + delta * 12.0D);
            } else {
                current = Math.max(0.0D, current - delta * 12.0D);
            }
            hoverProgress.put(ability, current);
        }

        double lerpFactor = Math.min(1.0D, delta * 20.0D);
        currentPointerX += (pointerX - currentPointerX) * lerpFactor;
        currentPointerY += (pointerY - currentPointerY) * lerpFactor;
    }
}
