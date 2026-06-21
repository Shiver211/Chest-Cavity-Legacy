package com.shiver.chestcavity.client.abilitywheel;

import com.shiver.chestcavity.client.CCKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public final class CCAbilityWheel {

    private static final AbilityWheelState STATE = new AbilityWheelState();

    private final AbilityWheelRenderer wheelRenderer = new AbilityWheelRenderer();
    private final ScoreSummaryRenderer scoreSummaryRenderer = new ScoreSummaryRenderer();

    public static String getSelectedAbility() {
        return STATE.getSelectedAbility();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.showDebugInfo) {
            return;
        }

        if (!CCKeyBindings.abilityWheel.isKeyDown()) {
            STATE.close(minecraft);
            return;
        }

        ScaledResolution resolution = event.getResolution();
        int centerX = resolution.getScaledWidth() / 2;
        int centerY = resolution.getScaledHeight() / 2;
        List<String> availableAbilities = AbilityWheelAbilities.getAvailableAbilities(minecraft);
        STATE.ensureSelectedAbilityAvailable(availableAbilities);

        if (!STATE.wasOpenLastFrame()) {
            if (minecraft.currentScreen == null) {
                minecraft.mouseHelper.ungrabMouseCursor();
                minecraft.inGameHasFocus = false;
            }
            STATE.open(availableAbilities);
        } else {
            AbilityWheelLayout.updatePointerAbsolute(STATE, minecraft, resolution, centerX, centerY);
        }

        STATE.updateAnimations(availableAbilities);
        double alphaScale = STATE.getAlphaScale();

        if (availableAbilities.isEmpty()) {
            STATE.setSelectedAbility(null);
            wheelRenderer.drawEmpty(minecraft, centerX, centerY, alphaScale);
            scoreSummaryRenderer.draw(minecraft.fontRenderer, centerX, centerY, minecraft, resolution, alphaScale);
            return;
        }

        STATE.setHoveredAbility(AbilityWheelLayout.getAbilityAtPointer(STATE, availableAbilities));
        STATE.selectHoveredAbilityOnClick();

        wheelRenderer.drawAbilities(minecraft, centerX, centerY, availableAbilities, STATE, alphaScale);
        scoreSummaryRenderer.draw(minecraft.fontRenderer, centerX, centerY, minecraft, resolution, alphaScale);
    }
}
