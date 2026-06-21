package com.shiver.chestcavity.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    String getSelectedAbility() {
        return selectedAbility;
    }

    void setSelectedAbility(String selectedAbility) {
        this.selectedAbility = selectedAbility;
    }

    String getHoveredAbility() {
        return hoveredAbility;
    }

    void setHoveredAbility(String hoveredAbility) {
        this.hoveredAbility = hoveredAbility;
    }

    boolean wasOpenLastFrame() {
        return wheelOpenLastFrame;
    }

    double getPointerX() {
        return pointerX;
    }

    double getPointerY() {
        return pointerY;
    }

    void setPointer(double pointerX, double pointerY) {
        this.pointerX = pointerX;
        this.pointerY = pointerY;
    }

    double getCurrentPointerX() {
        return currentPointerX;
    }

    double getCurrentPointerY() {
        return currentPointerY;
    }

    void setCurrentPointer(double currentPointerX, double currentPointerY) {
        this.currentPointerX = currentPointerX;
        this.currentPointerY = currentPointerY;
    }

    double getAlphaScale() {
        return Math.min(1.0D, wheelAlphaProgress);
    }

    double getHoverProgress(String ability) {
        return hoverProgress.containsKey(ability) ? hoverProgress.get(ability) : 0.0D;
    }

    void ensureSelectedAbilityAvailable(List<String> abilities) {
        if (!abilities.contains(selectedAbility) && !abilities.isEmpty()) {
            selectedAbility = abilities.get(0);
        }
    }

    void open(List<String> abilities) {
        wheelOpenLastFrame = true;
        wheelAlphaProgress = 0.0D;
        lastFrameTime = Minecraft.getSystemTime();
        AbilityWheelLayout.resetPointer(this, abilities);
    }

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

    void selectHoveredAbilityOnClick() {
        boolean isMouseDown = Mouse.isButtonDown(0);
        if (isMouseDown && !wasMouseDown && hoveredAbility != null) {
            selectedAbility = hoveredAbility;
        }
        wasMouseDown = isMouseDown;
    }

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
