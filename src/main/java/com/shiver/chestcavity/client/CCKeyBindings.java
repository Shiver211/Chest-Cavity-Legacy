package com.shiver.chestcavity.client;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public final class CCKeyBindings {

    private static final String CATEGORY = "category." + Tags.MOD_ID + ".organ_abilities";
    private static final ResourceLocation UTILITY_ABILITIES_ID = id("utility_abilities");
    private static final ResourceLocation ATTACK_ABILITIES_ID = id("attack_abilities");

    private static final ResourceLocation[] UTILITY_ABILITIES = {
            CCOrganScores.BUOYANT,
            CCOrganScores.FURNACE_POWERED,
            CCOrganScores.IRON_REPAIR,
            CCOrganScores.GRAZING,
            CCOrganScores.SILK
    };

    private static final ResourceLocation[] ATTACK_ABILITIES = {
            CCOrganScores.CREEPY,
            CCOrganScores.DRAGON_BREATH,
            CCOrganScores.DRAGON_BOMBS,
            CCOrganScores.FORCEFUL_SPIT,
            CCOrganScores.PYROMANCY,
            CCOrganScores.GHASTLY,
            CCOrganScores.SHULKER_BULLETS
    };

    public static KeyBinding utilityAbilities;
    public static KeyBinding attackAbilities;
    public static KeyBinding buoyantExhale;
    public static KeyBinding creepy;
    public static KeyBinding dragonBreath;
    public static KeyBinding dragonBombs;
    public static KeyBinding forcefulSpit;
    public static KeyBinding furnacePowered;
    public static KeyBinding ironRepair;
    public static KeyBinding pyromancy;
    public static KeyBinding ghastly;
    public static KeyBinding grazing;
    public static KeyBinding shulkerBullets;
    public static KeyBinding silk;

    private static boolean registered;
    private static int utilityAbilityIndex;
    private static int attackAbilityIndex;

    private CCKeyBindings() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        utilityAbilities = register(UTILITY_ABILITIES_ID, Keyboard.KEY_V);
        attackAbilities = register(ATTACK_ABILITIES_ID, Keyboard.KEY_R);
        buoyantExhale = register(CCOrganScores.BUOYANT, Keyboard.KEY_NONE);
        creepy = register(CCOrganScores.CREEPY, Keyboard.KEY_NONE);
        dragonBreath = register(CCOrganScores.DRAGON_BREATH, Keyboard.KEY_NONE);
        dragonBombs = register(CCOrganScores.DRAGON_BOMBS, Keyboard.KEY_NONE);
        forcefulSpit = register(CCOrganScores.FORCEFUL_SPIT, Keyboard.KEY_NONE);
        furnacePowered = register(CCOrganScores.FURNACE_POWERED, Keyboard.KEY_NONE);
        ironRepair = register(CCOrganScores.IRON_REPAIR, Keyboard.KEY_NONE);
        pyromancy = register(CCOrganScores.PYROMANCY, Keyboard.KEY_NONE);
        ghastly = register(CCOrganScores.GHASTLY, Keyboard.KEY_NONE);
        grazing = register(CCOrganScores.GRAZING, Keyboard.KEY_NONE);
        shulkerBullets = register(CCOrganScores.SHULKER_BULLETS, Keyboard.KEY_NONE);
        silk = register(CCOrganScores.SILK, Keyboard.KEY_NONE);

        FMLCommonHandler.instance().bus().register(new CCKeyBindings());
        registered = true;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }

        utilityAbilityIndex = drainGroup(utilityAbilities, UTILITY_ABILITIES, utilityAbilityIndex);
        attackAbilityIndex = drainGroup(attackAbilities, ATTACK_ABILITIES, attackAbilityIndex);
        drain(buoyantExhale, CCOrganScores.BUOYANT);
        drain(creepy, CCOrganScores.CREEPY);
        drain(dragonBreath, CCOrganScores.DRAGON_BREATH);
        drain(dragonBombs, CCOrganScores.DRAGON_BOMBS);
        drain(forcefulSpit, CCOrganScores.FORCEFUL_SPIT);
        drain(furnacePowered, CCOrganScores.FURNACE_POWERED);
        drain(ironRepair, CCOrganScores.IRON_REPAIR);
        drain(pyromancy, CCOrganScores.PYROMANCY);
        drain(ghastly, CCOrganScores.GHASTLY);
        drain(grazing, CCOrganScores.GRAZING);
        drain(shulkerBullets, CCOrganScores.SHULKER_BULLETS);
        drain(silk, CCOrganScores.SILK);
    }

    private static KeyBinding register(ResourceLocation id, int defaultKey) {
        KeyBinding keyBinding = new KeyBinding("key." + id.getNamespace() + "." + id.getPath(), defaultKey, CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    private static int drainGroup(KeyBinding keyBinding, ResourceLocation[] abilityIds, int index) {
        while (keyBinding.isPressed()) {
            ChestCavityNetwork.sendHotkeyActivation(abilityIds[index]);
            index = (index + 1) % abilityIds.length;
        }
        return index;
    }

    private static void drain(KeyBinding keyBinding, ResourceLocation abilityId) {
        while (keyBinding.isPressed()) {
            ChestCavityNetwork.sendHotkeyActivation(abilityId);
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
